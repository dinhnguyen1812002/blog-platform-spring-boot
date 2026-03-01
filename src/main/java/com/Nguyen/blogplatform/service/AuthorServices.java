package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.PublishStatus;
import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.mapper.PostMapper;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.CategoryResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.TagResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.payload.response.notification.PublicArticleNotification;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.repository.specification.ArticleSpecifications;
import com.Nguyen.blogplatform.security.JwtUtils;
import com.Nguyen.blogplatform.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorServices {
    private static final boolean DEFAULT_FEATURED = false;

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final NotificationService notificationService;
    private final FileStorageService fileStorageService;
    private final NewsletterService newsletterService;
    private final NotificationRepository notificationRepository;
    private final PostMapper postMapper;
    private final BookmarkRepository savedPostRepository;

    public List<PostResponse> getPostsForCurrentUser(
            int page,
            int size,
            String keyword,
            String categoryName,
            String tagName,
            String sortDirection) {
        String username = getCurrentUsername();

        Sort sort = "asc".equalsIgnoreCase(sortDirection)
                ? Sort.by("createdAt").ascending()
                : Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        var postsPage = postRepository.findByUserWithFilters(
                username,
                keyword,
                categoryName,
                tagName,
                pageable);

        return postMapper.toPostResponseList(postsPage.getContent(), getCurrentUser(), savedPostRepository);
    }

    @Transactional
    public Post newPost(PostRequest postRequest, String authorId) throws BadRequestException {

        if (postRequest == null || postRequest.getTitle() == null || postRequest.getTitle().isBlank()) {
            throw new BadRequestException("Invalid input data");
        }
        if (postRepository.existsByTitleIgnoreCase(postRequest.getTitle())) {
            throw new BadRequestException("Title already exists");
        }
        String slug = SlugUtil.toSlug(postRequest.getTitle());
        if (postRepository.existsBySlug(slug)) {
            throw new BadRequestException("A post with a similar title/slug already exists");
        }

        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        var categories = getCategoriesFromIds(postRequest.getCategories());
        var tags = getTagsFromIds(postRequest.getTags());

        // Resolve visibility: prefer explicit visibility field, fall back to status for
        // backward compat
        PublishStatus resolvedVisibility = postRequest.getVisibility() != null
                ? postRequest.getVisibility()
                : mapStatusToVisibility(postRequest.getStatus());

        boolean isPublished = false;
        LocalDateTime scheduledPublishAt = null;
        LocalDateTime publicDate = null;

        switch (resolvedVisibility) {
            case PUBLISHED -> {
                isPublished = true;
                publicDate = LocalDateTime.now();
            }
            case SCHEDULED -> {
                LocalDateTime scheduleTime = postRequest.getScheduledPublishAt() != null
                        ? postRequest.getScheduledPublishAt()
                        : postRequest.getPublic_date();
                if (scheduleTime == null) {
                    throw new BadRequestException("scheduledPublishAt is required when visibility is SCHEDULED");
                }
                if (!scheduleTime.isAfter(LocalDateTime.now())) {
                    throw new BadRequestException("scheduledPublishAt must be in the future");
                }
                isPublished = false;
                scheduledPublishAt = scheduleTime;
                publicDate = scheduleTime;
            }
            case PRIVATE -> {
                isPublished = false;
            }
            case DRAFT -> {
                isPublished = false;
            }
        }

        var post = Post.builder()
                .title(postRequest.getTitle())
                .slug(SlugUtil.toSlug(postRequest.getTitle()))
                .excerpt(postRequest.getExcerpt())
                .content(Objects.requireNonNullElse(postRequest.getContent(), ""))
                .thumbnail(postRequest.getThumbnail())
                .user(author)
                .createdAt(new Date())
                .categories(categories)
                .tags(tags)
                .featured(Objects.requireNonNullElse(postRequest.getFeatured(), false))
                .view(0L)
                .visibility(resolvedVisibility)
                .scheduledPublishAt(scheduledPublishAt)
                .public_date(publicDate)
                .is_publish(isPublished)
                .build();

        var savedPost = postRepository.save(post);

        // Create notification payload
        // PublicArticleNotification notification = new PublicArticleNotification(
        // savedPost.getId(),
        // savedPost.getTitle(),
        // savedPost.getExcerpt(),
        // savedPost.getSlug(),
        // savedPost.getPublic_date()
        // );
        return savedPost;
    }

    public PostResponse getPostDetail(String postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        validateAuthorization(post);
        return toPostResponse(post);
    }

    @Transactional
    public PostResponse updatePost(String postId, PostRequest postRequest) throws BadRequestException {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        validateAuthorization(post);
        if (!post.getTitle().equalsIgnoreCase(postRequest.getTitle())
                && postRepository.existsByTitleIgnoreCase(postRequest.getTitle())) {
            throw new BadRequestException("Title already exists");
        }

        post.setTitle(postRequest.getTitle());
        post.setSlug(SlugUtil.toSlug(postRequest.getTitle()));
        post.setContent(Objects.requireNonNullElse(postRequest.getContent(), ""));
        if (postRequest.getExcerpt() != null) {
            post.setExcerpt(postRequest.getExcerpt());
        }
        if (postRequest.getThumbnail() != null && !postRequest.getThumbnail().trim().isEmpty()) {
            post.setThumbnail(postRequest.getThumbnail());
        }
        post.setCategories(getCategoriesFromIds(postRequest.getCategories()));
        if (postRequest.getTags() != null) {
            post.setTags(getTagsFromIds(postRequest.getTags()));
        }
        if (postRequest.getFeatured() != null) {
            post.setFeatured(postRequest.getFeatured());
        }

        // Resolve visibility
        PublishStatus resolvedVisibility = postRequest.getVisibility() != null
                ? postRequest.getVisibility()
                : mapStatusToVisibility(postRequest.getStatus());

        // Only update visibility if explicitly provided, otherwise keep current
        if (postRequest.getVisibility() != null || postRequest.getStatus() != null) {
            post.setVisibility(resolvedVisibility);
            switch (resolvedVisibility) {
                case PUBLISHED -> {
                    post.setIs_publish(true);
                    post.setScheduledPublishAt(null);
                    if (post.getPublic_date() == null) {
                        post.setPublic_date(LocalDateTime.now());
                    }
                }
                case SCHEDULED -> {
                    LocalDateTime scheduleTime = postRequest.getScheduledPublishAt() != null
                            ? postRequest.getScheduledPublishAt()
                            : postRequest.getPublic_date();
                    if (scheduleTime == null) {
                        throw new BadRequestException("scheduledPublishAt is required when visibility is SCHEDULED");
                    }
                    if (!scheduleTime.isAfter(LocalDateTime.now())) {
                        throw new BadRequestException("scheduledPublishAt must be in the future");
                    }
                    post.setIs_publish(false);
                    post.setScheduledPublishAt(scheduleTime);
                    post.setPublic_date(scheduleTime);
                }
                case PRIVATE, DRAFT -> {
                    post.setIs_publish(false);
                    post.setScheduledPublishAt(null);
                }
            }
        }

        var updatedPost = postRepository.save(post);
        return toPostResponse(updatedPost);
    }

    @Transactional
    public void deletePost(String postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        validateAuthorization(post);
        postRepository.delete(post);
    }

    private String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    private void validateAuthorization(Post post) {
        var currentUsername = getCurrentUsername();
        if (!post.getUser().getUsername().equals(currentUsername)) {
            throw new UnauthorizedException("You are not authorized to modify this post");
        }
    }

    private User getCurrentUser() {
        String username = getCurrentUsername();
        if (username == null)
            return null;
        return userRepository.findByUsername(username)
                .orElse(null);
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

    private Set<Tags> getTagsFromIds(Set<UUID> tagIds) {
        return tagIds == null || tagIds.isEmpty() ? new HashSet<>()
                : tagIds.stream()
                        .map(id -> tagRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Tag not found with id: " + id)))
                        .collect(Collectors.toSet());
    }

    public PostResponse toPostResponse(Post post) {
        Set<CategoryResponse> categories = post.getCategories().stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getCategory(),
                        category.getBackgroundColor()))
                .collect(Collectors.toSet());
        Set<TagResponse> tags = post.getTags().stream()
                .map(tag -> new TagResponse(
                        tag.getUuid(),
                        tag.getName(),
                        tag.getSlug(),
                        tag.getDescription(),
                        tag.getColor()))
                .collect(Collectors.toSet());
        Double averageRating = post.getRatings().stream().mapToDouble(Rating::getScore).average().orElse(0.0);

        return PostResponse.builder()
                .id(post.getId())
                .user(new UserResponse(post.getUser().getId(), post.getUser().getUsername(), post.getUser().getEmail(),
                        post.getUser().getSlug(),
                        post.getUser().getAvatar()))
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .categories(categories)
                .tags(tags)
                .commentCount(post.getComments().size())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(averageRating)
                .public_date(post.getPublic_date())
                .is_publish(post.getIs_publish())
                .visibility(post.getVisibility())
                .scheduledPublishAt(post.getScheduledPublishAt())
                .build();
    }

    /**
     * Maps the legacy status field to the new visibility enum.
     * Used for backward compatibility when clients send the old `status` field.
     */
    private PublishStatus mapStatusToVisibility(PublishStatus status) {
        if (status == null) {
            return PublishStatus.DRAFT;
        }
        return status;
    }
}