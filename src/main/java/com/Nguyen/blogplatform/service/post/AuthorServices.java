package com.Nguyen.blogplatform.service.post;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import com.Nguyen.blogplatform.exception.ForbiddenException;
import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.mapper.PostMapper;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.PostSummaryResponse;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.service.notification.NotificationService;
import com.Nguyen.blogplatform.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for authoring and managing posts by their authors.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorServices {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final BookmarkRepository bookmarkRepository;
    private final PostMapper postMapper;

    private final NotificationService notificationService;

    /**
     * Gets posts for the current authenticated author with filters.
     */
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getPostsForCurrentUser(
            int page, int size, String keyword, String categoryName, String tagName, String sortDirection) {
        
        String username = getCurrentUsername();
        Sort sort = "asc".equalsIgnoreCase(sortDirection) ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Post> postsPage = postRepository.findByAuthorWithFilters(
                username, keyword, categoryName, tagName, pageable);

        return postsPage.map(post -> postMapper.toPostSummaryResponse(post, getCurrentUser(), Set.of()));
    }

    /**
     * Creates a new post.
     */
    @Transactional
    public PostResponse createPost(PostRequest request) {
        String username = getCurrentUsername();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        if (postRepository.existsByTitleIgnoreCase(request.title())) {
            throw new IllegalArgumentException("Title already exists");
        }

        String slug = SlugUtil.toSlug(request.title());
        if (postRepository.existsBySlug(slug)) {
            slug = slug + "-" + UUID.randomUUID().toString().substring(0, 8);
        }

        Set<Category> categories = getCategoriesFromIds(request.categories());
        Set<Tags> tags = getTagsFromIds(request.tags());

        PublishStatus visibility = request.visibility() != null ? request.visibility() : PublishStatus.DRAFT;
        LocalDateTime publishedAt = resolvePublishedAt(visibility, request);

        Post post = Post.builder()
                .title(request.title())
                .slug(slug)
                .excerpt(request.excerpt())
                .content(request.content())
                .thumbnail(request.thumbnail())
                .author(author)
                .categories(categories)
                .tags(tags)
                .featured(request.featured() != null && request.featured())
                .visibility(visibility)
                .publishedAt(publishedAt)
                .scheduledPublishAt(PublishStatus.SCHEDULED.equals(visibility) ? request.scheduledPublishAt() : null)
                .build();

        Post savedPost = postRepository.save(post);

        // Send notification if published immediately
        if (PublishStatus.PUBLISHED.equals(visibility)) {
            sendPublishNotification(savedPost);
        }

        return postMapper.toPostResponse(savedPost, author, Set.of());
    }

    /**
     * Updates an existing post.
     */
    @Transactional
    public PostResponse updatePost(String postId, PostRequest request) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        validateOwnership(post);

        boolean previouslyPublished = post.isPublished();

        if (!post.getTitle().equalsIgnoreCase(request.title()) && postRepository.existsByTitleIgnoreCase(request.title())) {
            throw new IllegalArgumentException("Title already exists");
        }

        post.setTitle(request.title());
        post.setSlug(SlugUtil.toSlug(request.title()));
        post.setExcerpt(request.excerpt());
        post.setContent(request.content());
        post.setThumbnail(request.thumbnail());
        post.setCategories(getCategoriesFromIds(request.categories()));
        post.setTags(getTagsFromIds(request.tags()));
        post.setFeatured(request.featured() != null && request.featured());
        
        if (request.visibility() != null) {
            post.setVisibility(request.visibility());
            post.setPublishedAt(resolvePublishedAt(request.visibility(), request));
            post.setScheduledPublishAt(PublishStatus.SCHEDULED.equals(request.visibility()) ? request.scheduledPublishAt() : null);
        }

        Post updatedPost = postRepository.save(post);

        // Send notification if it just became published
        if (!previouslyPublished && updatedPost.isPublished()) {
            sendPublishNotification(updatedPost);
        }

        return postMapper.toPostResponse(updatedPost, post.getAuthor(), Set.of());
    }

    private void sendPublishNotification(Post post) {
        notificationService.createUserNotification(
                post.getAuthor().getId(),
                "POST_PUBLISHED",
                "Article Published",
                "Your article '" + post.getTitle() + "' is now public!"
        );
        
        // Also broadcast to others if public
        notificationService.broadcastArticlePublishedNotification(new PublicArticleNotification(
                post.getId(), post.getTitle(), post.getThumbnail(), post.getExcerpt(), post.getSlug(), post.getPublishedAt()
        ));
    }

    /**
     * Deletes a post.
     */
    @Transactional
    public void deletePost(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        validateOwnership(post);
        postRepository.delete(post);
    }

    /**
     * Gets detailed post information for the author.
     */
    @Transactional(readOnly = true)
    public PostResponse getPostDetail(String postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        
        validateOwnership(post);
        return postMapper.toPostResponseWithComments(post, post.getAuthor(), bookmarkRepository);
    }

    private LocalDateTime resolvePublishedAt(PublishStatus visibility, PostRequest request) {
        return switch (visibility) {
            case PUBLISHED -> request.publishedAt() != null ? request.publishedAt() : LocalDateTime.now();
            case SCHEDULED -> {
                if (request.scheduledPublishAt() == null) {
                    throw new IllegalArgumentException("scheduledPublishAt is required for SCHEDULED visibility");
                }
                if (request.scheduledPublishAt().isBefore(LocalDateTime.now())) {
                    throw new IllegalArgumentException("scheduledPublishAt must be in the future");
                }
                yield request.scheduledPublishAt();
            }
            default -> null;
        };
    }

    private void validateOwnership(Post post) {
        String currentUsername = getCurrentUsername();
        if (!post.getAuthor().getUsername().equals(currentUsername)) {
            throw new ForbiddenException("You don't have permission to modify this post");
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private User getCurrentUser() {
        return userRepository.findByUsername(getCurrentUsername()).orElse(null);
    }

    private Set<Category> getCategoriesFromIds(Set<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            throw new InvalidCategoryException("A post must have at least one category");
        }
        return new HashSet<>(categoryRepository.findAllById(categoryIds));
    }

    private Set<Tags> getTagsFromIds(Set<UUID> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptySet();
        }
        return new HashSet<>(tagRepository.findAllById(tagIds));
    }
}
