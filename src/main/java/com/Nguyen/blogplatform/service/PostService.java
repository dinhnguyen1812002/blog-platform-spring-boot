package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.mapper.PostMapper;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.repository.PostSpecification;
import com.Nguyen.blogplatform.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {
    private static final long CACHE_EXPIRY_MS = 300_000; // 5 minutes

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final CategoryRepository categoryRepository;
    private final RatingRepository ratingRepository;
    private final NotificationService notificationService;
    private final BookmarkRepository savedPostRepository;
    private final CommentServices commentServices;
    private final PostMapper postMapper;

    private final Map<String, PostResponse> postCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

    @Transactional
    public void incrementViewCount(String postId) {
        postRepository.incrementViewCount(postId);
        invalidateCache(postId);
    }

    @Transactional
    public boolean toggleLike(String postId) {
        var post = findPostById(postId);
        var user = getUser();
        var isLiked = post.getLike().contains(user);

        post.getLike().removeIf(u -> u.equals(user));
        if (!isLiked) {
            post.getLike().add(user);
        }
        postRepository.save(post);
        invalidateCache(postId);
        return !isLiked;
    }

    @Transactional
    public Integer ratePost(String postId, Integer score) {
        if (score < 1 || score > 5) {
            throw new IllegalArgumentException("Score must be between 1 and 5");
        }

        var post = findPostById(postId);
        var user = getUser();
        var rating = ratingRepository.findByPostAndUser(post, user)
                .orElseGet(() -> {
                    var newRating = new Rating(score, post, user);
                    post.getRatings().add(newRating);
                    return newRating;
                });

        rating.setScore(score);
        ratingRepository.save(rating);
        invalidateCache(postId);
        return score;
    }

    public Page<PostResponse> getListPost(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(post -> postMapper.toPostResponse(post, getCurrentUser(), savedPostRepository));
    }


    /**
     * Retrieves filtered and paginated posts based on category slug, tag slug, and pageable (including sorting).
     *
     * @param categorySlug Optional category slug to filter by.
     * @param tagSlug Optional tag slug to filter by.
     * @param pageable Pageable object for pagination and sorting.
     * @return Page of PostResponse objects.
     */
    public Page<PostResponse> getFilteredPosts(String categorySlug, String tagSlug, Pageable pageable) {
        Specification<Post> spec = Specification.where(null);

        if (categorySlug != null && !categorySlug.isEmpty()) {
            spec = spec.and(PostSpecification.hasCategorySlug(categorySlug));
        }

        if (tagSlug != null && !tagSlug.isEmpty()) {
            spec = spec.and(PostSpecification.hasTagSlug(tagSlug));
        }

        return postRepository.findAll(spec, pageable)
                .map(post -> postMapper.toPostResponse(post, getCurrentUser(), savedPostRepository));
    }

    public List<PostResponse> getFeaturedPosts(Pageable pageable) {
        return postRepository.findByFeaturedTrueOrderByCreatedAtDesc(pageable)
                .stream()
                .map(post -> postMapper.toPostResponse(post, getCurrentUser(), savedPostRepository))
                .toList();
    }

    public PostResponse getPostResponseById(String postId) {
        var post = findPostById(postId);
        return postMapper.toPostResponse(post, getCurrentUser(), savedPostRepository);
    }

    @Transactional
    public PostResponse getPostBySlug(String slug) {
        // Step 1: Find the post by its slug.
        var post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Post not found with slug: " + slug));

        // Step 2: Increment the view count in the database.
        // The existing incrementViewCount method also invalidates the cache, which is fine.
        incrementViewCount(post.getId());

        // Step 3: Refetch the post to get the updated view count.
        // This is necessary because the increment is a direct database update,
        // and the 'post' object we currently have is stale.
        var updatedPost = postRepository.findById(post.getId())
                .orElseThrow(() -> new NotFoundException("Could not refetch post with id: " + post.getId()));

        // Step 4: Map the updated post to a response DTO and return it.
        // No caching is performed here to ensure the view count is incremented on every call.
        return postMapper.toPostResponseWithComments(updatedPost, getCurrentUser(), savedPostRepository);
    }

    public List<PostResponse> getPostsByCategory(Long categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return postRepository.findByCategoriesContaining(category)
                .stream()
                .map(post -> postMapper.toPostResponse(post, getCurrentUser(),  savedPostRepository))
                .toList();
    }

    public List<PostResponse> searchPosts(String title, Long categoryId) {
        Specification<Post> spec = Specification.where(PostSpecification.hasTitle(title))
                .and(PostSpecification.hasCategoryId(categoryId));
        return postRepository.findAll(spec)
                .stream()
                .map(post -> postMapper.toPostResponse(post, getCurrentUser(), savedPostRepository))
                .toList();
    }

    public List<Post> getAllPost() {
        return postRepository.findAll();
    }

    private Post findPostById(String postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
    }

    private User getCurrentUser() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }

        var jwt = (String) authentication.getCredentials();
        if (jwt == null) {
            return null;
        }

        var userId = jwtUtils.getUserIdFromJwtToken(jwt);
        if (userId == null) {
            System.out.println("User id is null");
        }
        return userRepository.findById(userId).orElse(null);
    }

    private User getUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findById(userDetails.getId()).orElse(null);
    }

    private Set<Category> getCategoriesFromIds(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new InvalidCategoryException("A post must have at least one category");
        }
        return categoryIds.stream()
                .map(id -> categoryRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Category not found with id: " + id)))
                .collect(Collectors.toSet());
    }

    private void cachePost(String key, PostResponse response) {
        postCache.put(key, response);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    private void invalidateCache(String postId) {
        postCache.entrySet().removeIf(entry -> entry.getKey().equals(postId) || entry.getValue().getId().equals(postId));
    }

    private boolean isCacheValid(String key) {
        var timestamp = cacheTimestamps.get(key);
        return timestamp != null && System.currentTimeMillis() - timestamp < CACHE_EXPIRY_MS && postCache.containsKey(key);
    }
}