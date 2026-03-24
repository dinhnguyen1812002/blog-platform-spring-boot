package com.Nguyen.blogplatform.service.post;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import com.Nguyen.blogplatform.exception.ForbiddenException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.mapper.PostMapper;
import com.Nguyen.blogplatform.model.Post;
import com.Nguyen.blogplatform.model.Rating;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.PostSummaryResponse;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.RatingRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.repository.specification.PostSpecification;
import com.Nguyen.blogplatform.service.auth.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * Service for public post management (listing, viewing, liking, rating).
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final RatingRepository ratingRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostMapper postMapper;

    /**
     * Increments the view count of a post.
     */
    @Transactional
    public void incrementViewCount(String postId) {
        postRepository.incrementViewCount(postId);
    }

    /**
     * Toggles a like on a post for the current user.
     */
    @Transactional
    public boolean toggleLike(String postId) {
        Post post = findPostById(postId);
        User user = getCurrentUserOrThrow();
        boolean isLiked = post.getLikes().contains(user);

        if (isLiked) {
            post.getLikes().remove(user);
        } else {
            post.getLikes().add(user);
        }
        postRepository.save(post);
        return !isLiked;
    }

    /**
     * Rates a post for the current user.
     */
    @Transactional
    public Integer ratePost(String postId, Integer score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }

        Post post = findPostById(postId);
        User user = getCurrentUserOrThrow();
        
        Rating rating = ratingRepository.findByPostAndUser(post, user)
                .orElseGet(() -> {
                    Rating newRating = new Rating(score, post, user);
                    post.addRating(newRating);
                    return newRating;
                });

        rating.setScore(score);
        ratingRepository.save(rating);
        return score;
    }

    /**
     * Gets a paged list of published posts with filters.
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getFilteredPosts(String categorySlug, String tagSlug, Pageable pageable) {
        Specification<Post> spec = PostSpecification.isPublished();

        if (categorySlug != null && !categorySlug.isBlank()) {
            spec = spec.and(PostSpecification.hasCategorySlug(categorySlug));
        }

        if (tagSlug != null && !tagSlug.isBlank()) {
            spec = spec.and(PostSpecification.hasTagSlug(tagSlug));
        }

        Page<Post> posts = postRepository.findAll(spec, pageable);
        User currentUser = getCurrentUser();
        List<PostSummaryResponse> content = postMapper.toPostSummaryList(posts.getContent(), currentUser, bookmarkRepository);
        
        return new PageImpl<>(content, pageable, posts.getTotalElements());
    }

    /**
     * Gets featured posts.
     */
    @Transactional(readOnly = true)
    public List<PostSummaryResponse> getFeaturedPosts(Pageable pageable) {
        Page<Post> posts = postRepository.findByFeaturedTrueAndVisibilityAndPublishedAtBeforeOrderByPublishedAtDesc(
                PublishStatus.PUBLISHED, LocalDateTime.now(), pageable);
        return postMapper.toPostSummaryList(posts.getContent(), getCurrentUser(), bookmarkRepository);
    }

    /**
     * Gets a post by its slug, checking privacy rules.
     */
    @Transactional
    public PostResponse getPostBySlug(String slug) {
        Post post = (Post) postRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Post not found with slug: " + slug));
        
        enforcePrivacy(post);
        incrementViewCount(post.getId());
        
        return postMapper.toPostResponseWithComments(post, getCurrentUser(), bookmarkRepository);
    }

    /**
     * Toggles the featured status of a post (Admin only usually).
     */
    @Transactional
    public PostResponse toggleFeatured(String postId) {
        Post post = findPostById(postId);
        post.setFeatured(!post.getFeatured());
        Post updatedPost = postRepository.save(post);
        return postMapper.toPostResponse(updatedPost, getCurrentUser(), Set.of());
    }

    /**
     * Gets a post by its ID and returns a PostResponse.
     */
    @Transactional(readOnly = true)
    public PostResponse getPostResponseById(String postId) {
        Post post = findPostById(postId);
        enforcePrivacy(post);
        return postMapper.toPostResponseWithComments(post, getCurrentUser(), bookmarkRepository);
    }

    private Post findPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetailsImpl userDetails) {
            return userRepository.findById(userDetails.getId()).orElse(null);
        }
        return null;
    }

    private User getCurrentUserOrThrow() {
        User user = getCurrentUser();
        if (user == null) {
            throw new ForbiddenException("Authentication required");
        }
        return user;
    }

    /**
     * Enforces privacy rules for post access.
     */
    private void enforcePrivacy(Post post) {
        // A post is considered public if its visibility is PUBLISHED and its publishedAt time has passed (or is null)
        boolean isPubliclyAvailable = post.isPublished();

        if (isPubliclyAvailable) {
            return;
        }

        User currentUser = getCurrentUser();
        boolean isAuthor = currentUser != null && post.getAuthor() != null &&
                currentUser.getId().equals(post.getAuthor().getId());
        
        boolean isAdmin = currentUser != null && currentUser.getRoles().stream()
                .anyMatch(role -> role.getName().name().contains("ADMIN"));

        // Authors and Admins can see non-published posts
        if (isAuthor || isAdmin) {
            return;
        }

        // Handle specific visibility types for non-authors
        if (PublishStatus.PRIVATE.equals(post.getVisibility())) {
            throw new ForbiddenException("This post is private");
        }
        
        if (PublishStatus.SCHEDULED.equals(post.getVisibility())) {
            throw new NotFoundException("Post not yet published");
        }

        throw new NotFoundException("Post not found");
    }
}
