package com.Nguyen.blogplatform.domain.post.application;


import com.Nguyen.blogplatform.shared.security.SecurityUtils;
import com.Nguyen.blogplatform.shared.util.HtmlSanitizer;
import com.Nguyen.blogplatform.domain.notification.application.NotificationService;
import com.Nguyen.blogplatform.domain.notification.dto.PublicArticleNotification;
import com.Nguyen.blogplatform.domain.post.domain.model.Category;
import com.Nguyen.blogplatform.domain.post.domain.model.Post;
import com.Nguyen.blogplatform.domain.post.domain.model.Tags;
import com.Nguyen.blogplatform.domain.post.dto.PostRequest;
import com.Nguyen.blogplatform.domain.post.dto.PostResponse;
import com.Nguyen.blogplatform.domain.post.dto.PostSummaryResponse;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.BookmarkRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.CategoryRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.PostRepository;
import com.Nguyen.blogplatform.domain.post.infrastructure.repository.TagRepository;
import com.Nguyen.blogplatform.domain.user.domain.model.User;
import com.Nguyen.blogplatform.domain.user.infrastructure.repository.UserRepository;
import com.Nguyen.blogplatform.shared.enums.PublishStatus;
import com.Nguyen.blogplatform.shared.exception.ForbiddenException;
import com.Nguyen.blogplatform.shared.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.shared.exception.NotFoundException;
import com.Nguyen.blogplatform.shared.mapper.PostMapper;
import com.Nguyen.blogplatform.shared.util.ExcerptUtil;
import com.Nguyen.blogplatform.shared.util.SlugUtil;
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

        var content = postMapper.toPostSummaryList(postsPage.getContent(), getCurrentUser(), bookmarkRepository);
        return new org.springframework.data.domain.PageImpl<>(content, pageable, postsPage.getTotalElements());
    }

    /**
     * Creates a new post.
     */
    @Transactional
    public PostResponse createPost(PostRequest request) {
        String username = getCurrentUsername();
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found: " + username));

        // Titles need not be unique; only the slug (URL identity) is.
        String slug = generateUniqueSlug(request.title(), null);

        Set<Category> categories = getCategoriesFromIds(request.categories());
        Set<Tags> tags = getTagsFromIds(request.tags());

        PublishStatus visibility = request.visibility() != null ? request.visibility() : PublishStatus.DRAFT;
        LocalDateTime publishedAt = resolvePublishedAt(visibility, request);

        String content = HtmlSanitizer.sanitizePostContent(request.content());

        String excerpt = request.excerpt();
        if (excerpt == null || excerpt.isBlank()) {
            excerpt = ExcerptUtil.excerpt(content);
        }

        Post post = Post.builder()
                .title(request.title())
                .slug(slug)
                .excerpt(excerpt)
                .content(content)
                .thumbnail(request.thumbnail())
                .author(author)
                .categories(categories)
                .tags(tags)
                // Only admins may feature posts; authors can not self-promote
                .featured(isCurrentUserAdmin() && Boolean.TRUE.equals(request.featured()))
                .visibility(visibility)
                .isPublish(PublishStatus.PUBLISHED.equals(visibility))
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

        // First real publication date. A SCHEDULED post's publishedAt is only the planned time.
        LocalDateTime firstPublishedAt =
                PublishStatus.SCHEDULED.equals(post.getVisibility()) ? null : post.getPublishedAt();

        // Slug is the post's URL identity: once published it never changes, so shared links
        // keep working. Unpublished drafts follow their title.
        if (firstPublishedAt == null && !post.getTitle().equals(request.title())) {
            post.setSlug(generateUniqueSlug(request.title(), post.getId()));
        }
        post.setTitle(request.title());

        String content = HtmlSanitizer.sanitizePostContent(request.content());

        String excerpt = request.excerpt();
        if (excerpt == null || excerpt.isBlank()) {
            excerpt = ExcerptUtil.excerpt(content);
        }
        post.setExcerpt(excerpt);

        post.setContent(content);
        post.setThumbnail(request.thumbnail());
        post.setCategories(getCategoriesFromIds(request.categories()));
        post.setTags(getTagsFromIds(request.tags()));
        // Only admins may change featured; for authors the current value is kept
        if (isCurrentUserAdmin() && request.featured() != null) {
            post.setFeatured(request.featured());
        }
        
        if (request.visibility() != null) {
            PublishStatus visibility = request.visibility();
            switch (visibility) {
                // Editing never re-publishes: keep the first publication date
                case PUBLISHED -> post.setPublishedAt(
                        firstPublishedAt != null ? firstPublishedAt : resolvePublishedAt(visibility, request));
                case SCHEDULED -> {
                    if (firstPublishedAt != null) {
                        throw new IllegalArgumentException("A post that was already published cannot be scheduled again");
                    }
                    post.setPublishedAt(resolvePublishedAt(visibility, request));
                }
                // Unpublishing (DRAFT/PRIVATE) keeps the historical first publication date
                default -> post.setPublishedAt(firstPublishedAt);
            }
            post.setVisibility(visibility);
            post.setIsPublish(PublishStatus.PUBLISHED.equals(visibility));
            post.setScheduledPublishAt(PublishStatus.SCHEDULED.equals(visibility) ? request.scheduledPublishAt() : null);
        }

        Post updatedPost = postRepository.save(post);

        // Notify followers only on the very first publication, not on edits or re-publishes
        if (firstPublishedAt == null && updatedPost.isPublished()) {
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
        // Authors manage their own posts; admins manage every post
        if (!post.getAuthor().getUsername().equals(currentUsername) && !SecurityUtils.isCurrentUserAdmin()) {
            throw new ForbiddenException("You don't have permission to modify this post");
        }
    }

    private String getCurrentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    /**
     * Builds a unique slug from the title. Empty results (emoji-only titles) fall back to
     * "post-xxxxxxxx"; collisions get a short random suffix.
     */
    private String generateUniqueSlug(String title, String excludePostId) {
        String base = SlugUtil.toSlug(title);
        if (base == null || base.isBlank()) {
            base = "post";
        }
        String slug = base;
        int attempts = 0;
        while (isSlugTaken(slug, excludePostId)) {
            if (++attempts > 5) {
                throw new IllegalStateException("Could not generate a unique slug");
            }
            slug = base + "-" + UUID.randomUUID().toString().substring(0, 8);
        }
        if ("post".equals(slug)) {
            slug = "post-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return slug;
    }

    private boolean isSlugTaken(String slug, String excludePostId) {
        return excludePostId == null
                ? postRepository.existsBySlug(slug)
                : postRepository.existsBySlugAndIdNot(slug, excludePostId);
    }

    private boolean isCurrentUserAdmin() {
        return SecurityUtils.isCurrentUserAdmin();
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
