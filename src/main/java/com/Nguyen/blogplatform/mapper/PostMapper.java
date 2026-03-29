package com.Nguyen.blogplatform.mapper;

import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.response.*;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.service.comment.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Stateless mapper: Post entity ↔ response DTOs.
 *
 * Rules:
 *  - No business logic — only field mapping and lightweight derivations.
 *  - currentUser may be null (anonymous visitor) — every method handles this safely.
 *  - BookmarkRepository is accepted as a parameter rather than injected so the
 *    caller controls when a DB call happens (easier to mock in tests too).
 */
@Component
@RequiredArgsConstructor
public class PostMapper {

    private static final int DEFAULT_COMMENT_PAGE_SIZE = 10;

    private final CommentService commentService;

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Full post detail — comments list is intentionally empty here.
     * Use {@link #toPostResponseWithComments} when you need the first page of comments.
     */
    public PostResponse toPostResponse(Post post, User currentUser, Set<String> bookmarkedPostIds) {
        if (post == null) return null;
        return buildPostResponse(post, currentUser, bookmarkedPostIds, Collections.emptyList());
    }

    /** Summary card — no content body, no comments. */
    public PostSummaryResponse toPostSummaryResponse(Post post, User currentUser,
                                                     Set<String> bookmarkedPostIds) {
        if (post == null) return null;

        return new PostSummaryResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getThumbnail(),
                post.getCreatedAt(),
                post.getPublishedAt(),
                post.getFeatured(),
                post.getVisibility(),
                post.getViewCount(),
                likeCount(post),
                averageRating(post),
                toUserResponse(post.getAuthor()),
                mapCategories(post.getCategories()),
                mapTags(post.getTags()),
                post.getComments().size(),
                isLikedBy(post, currentUser),
                isBookmarked(post.getId(), bookmarkedPostIds)
        );
    }

    /** Batch summary list — resolves bookmarks in a single query. */
    public List<PostSummaryResponse> toPostSummaryList(Collection<Post> posts, User currentUser,
                                                       BookmarkRepository bookmarkRepository) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();

        Set<String> bookmarkedIds = resolveBookmarks(currentUser, posts, bookmarkRepository);

        return posts.stream()
                .map(p -> toPostSummaryResponse(p, currentUser, bookmarkedIds))
                .collect(Collectors.toList());
    }

    /**
     * Full post detail + first page of top-level comments.
     * Bookmark is resolved with a single existsBy call since we have one post.
     */
    public PostResponse toPostResponseWithComments(Post post, User currentUser,
                                                   BookmarkRepository bookmarkRepository) {
        if (post == null) return null;

        List<CommentResponse> comments = fetchFirstCommentPage(post.getId());
        Set<String> bookmarkedIds     = resolveBookmarkSingle(currentUser, post, bookmarkRepository);

        return buildPostResponse(post, currentUser, bookmarkedIds, comments);
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getSlug(),
                user.getAvatar());
    }

    // -------------------------------------------------------------------------
    // Private — core builder
    // -------------------------------------------------------------------------

    private PostResponse buildPostResponse(Post post, User currentUser,
                                           Set<String> bookmarkedPostIds,
                                           List<CommentResponse> comments) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getExcerpt(),
                post.getContent(),
                post.getThumbnail(),
                post.getCreatedAt(),
                post.getUpdatedAt(),
                post.getPublishedAt(),
                post.getFeatured(),
                post.getVisibility(),
                post.getScheduledPublishAt(),
                post.getViewCount(),
                likeCount(post),
                averageRating(post),
                toUserResponse(post.getAuthor()),
                mapCategories(post.getCategories()),
                mapTags(post.getTags()),
                isLikedBy(post, currentUser),
                isBookmarked(post.getId(), bookmarkedPostIds),
                userRating(post, currentUser),
                post.getComments().size(),
                comments
        );
    }

    // -------------------------------------------------------------------------
    // Private — derived values
    // -------------------------------------------------------------------------

    private long likeCount(Post post) {
        return post.getLikes() == null ? 0L : post.getLikes().size();
    }

    private double averageRating(Post post) {
        if (post.getRatings() == null || post.getRatings().isEmpty()) return 0.0;
        return post.getRatings().stream()
                .mapToDouble(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    private boolean isLikedBy(Post post, User currentUser) {
        if (currentUser == null || post.getLikes() == null) return false;
        return post.getLikes().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));
    }

    private boolean isBookmarked(String postId, Set<String> bookmarkedPostIds) {
        return bookmarkedPostIds != null && bookmarkedPostIds.contains(postId);
    }

    private Integer userRating(Post post, User currentUser) {
        if (currentUser == null || post.getRatings() == null) return null;
        return post.getRatings().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(currentUser.getId()))
                .map(Rating::getScore)
                .findFirst()
                .orElse(null);
    }

    // -------------------------------------------------------------------------
    // Private — collection mappers
    // -------------------------------------------------------------------------

    private Set<CategoryResponse> mapCategories(Set<Category> categories) {
        if (categories == null) return Collections.emptySet();
        return categories.stream()
                .map(c -> new CategoryResponse(c.getId(), c.getCategory(), c.getBackgroundColor()))
                .collect(Collectors.toSet());
    }

    private Set<TagResponse> mapTags(Set<Tags> tags) {
        if (tags == null) return Collections.emptySet();
        return tags.stream()
                .map(t -> new TagResponse(
                        t.getUuid(), t.getName(), t.getSlug(), t.getDescription(), t.getColor()))
                .collect(Collectors.toSet());
    }

    // -------------------------------------------------------------------------
    // Private — bookmark resolution helpers
    // -------------------------------------------------------------------------

    /** Single-query batch bookmark lookup for a collection of posts. */
    private Set<String> resolveBookmarks(User currentUser, Collection<Post> posts,
                                         BookmarkRepository repo) {
        if (currentUser == null || repo == null) return Collections.emptySet();
        return repo.findBookmarkedPostIds(currentUser, posts);
    }

    /** Single existsBy check when we only have one post. */
    private Set<String> resolveBookmarkSingle(User currentUser, Post post,
                                              BookmarkRepository repo) {
        if (currentUser == null || repo == null) return Collections.emptySet();
        return repo.existsByUserAndPost(currentUser, post)
                ? Set.of(post.getId())
                : Collections.emptySet();
    }

    // -------------------------------------------------------------------------
    // Private — comment fetching
    // -------------------------------------------------------------------------

    private List<CommentResponse> fetchFirstCommentPage(String postId) {
        Pageable pageable = PageRequest.of(0, DEFAULT_COMMENT_PAGE_SIZE,
                Sort.by("createdAt").descending());
        return commentService.getTopLevelComments(postId, pageable).getContent();
    }
}