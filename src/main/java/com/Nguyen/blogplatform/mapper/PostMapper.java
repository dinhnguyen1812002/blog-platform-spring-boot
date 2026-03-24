package com.Nguyen.blogplatform.mapper;

import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.response.*;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.service.comment.CommentServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
 * Mapper for converting between Post entities and DTOs.
 */
@Component
@RequiredArgsConstructor
public class PostMapper {
    private final CommentServices commentServices;

    /**
     * Converts a Post entity to a PostResponse record.
     */
    public PostResponse toPostResponse(Post post, User currentUser, Set<String> bookmarkedPostIds) {
        if (post == null) return null;

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
                (long) post.getLikes().size(),
                calculateAverageRating(post),
                createUserResponse(post.getAuthor()),
                mapCategories(post.getCategories()),
                mapTags(post.getTags()),
                isLikedByCurrentUser(post, currentUser),
                bookmarkedPostIds != null && bookmarkedPostIds.contains(post.getId()),
                getUserRating(post, currentUser),
                post.getComments().size(),
                Collections.emptyList() // Comments are populated separately for detail view
        );
    }

    /**
     * Converts a Post entity to a PostSummaryResponse record.
     */
    public PostSummaryResponse toPostSummaryResponse(Post post, User currentUser, Set<String> bookmarkedPostIds) {
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
                (long) post.getLikes().size(),
                calculateAverageRating(post),
                createUserResponse(post.getAuthor()),
                mapCategories(post.getCategories()),
                mapTags(post.getTags()),
                post.getComments().size(),
                isLikedByCurrentUser(post, currentUser),
                bookmarkedPostIds != null && bookmarkedPostIds.contains(post.getId())
        );
    }

    public List<PostSummaryResponse> toPostSummaryList(Collection<Post> posts, User currentUser,
                                                       BookmarkRepository bookmarkRepository) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> bookmarkedPostIds = (currentUser != null && bookmarkRepository != null)
                ? bookmarkRepository.findBookmarkedPostIds(currentUser, posts)
                : Collections.emptySet();

        return posts.stream()
                .map(post -> toPostSummaryResponse(post, currentUser, bookmarkedPostIds))
                .toList();
    }

    public PostResponse toPostResponseWithComments(Post post, User currentUser,
                                                   BookmarkRepository bookmarkRepository) {
        if (post == null) return null;

        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<CommentResponse> commentPage = commentServices.getTopLevelComments(post.getId(), pageable);
        List<CommentResponse> comments = commentPage.getContent();

        Set<String> bookmarkedPostIds = (currentUser != null && bookmarkRepository != null)
                ? (bookmarkRepository.existsByUserAndPost(currentUser, post) ? Set.of(post.getId()) : Collections.emptySet())
                : Collections.emptySet();

        PostResponse baseResponse = toPostResponse(post, currentUser, bookmarkedPostIds);

        return new PostResponse(
                baseResponse.id(), baseResponse.title(), baseResponse.slug(), baseResponse.excerpt(),
                baseResponse.content(), baseResponse.thumbnail(), baseResponse.createdAt(), baseResponse.updatedAt(),
                baseResponse.publishedAt(), baseResponse.featured(), baseResponse.visibility(),
                baseResponse.scheduledPublishAt(), baseResponse.viewCount(), baseResponse.likeCount(),
                baseResponse.averageRating(), baseResponse.author(), baseResponse.categories(),
                baseResponse.tags(), baseResponse.isLikedByCurrentUser(), baseResponse.isSavedByCurrentUser(),
                baseResponse.userRating(), baseResponse.commentCount(), comments
        );
    }

    public UserResponse createUserResponse(User user) {
        if (user == null) return null;
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getSlug(), user.getAvatar());
    }

    private Set<CategoryResponse> mapCategories(Set<Category> categories) {
        if (categories == null) return Collections.emptySet();
        return categories.stream()
                .map(category -> new CategoryResponse(
                        category.getId(),
                        category.getCategory(),
                        category.getBackgroundColor()))
                .collect(Collectors.toSet());
    }

    private Set<TagResponse> mapTags(Set<Tags> tags) {
        if (tags == null) return Collections.emptySet();
        return tags.stream()
                .map(tag -> new TagResponse(
                        tag.getUuid(),
                        tag.getName(),
                        tag.getSlug(),
                        tag.getDescription(),
                        tag.getColor()))
                .collect(Collectors.toSet());
    }

    public double calculateAverageRating(Post post) {
        if (post.getRatings() == null) return 0.0;
        return post.getRatings().stream()
                .mapToDouble(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    public boolean isLikedByCurrentUser(Post post, User currentUser) {
        if (currentUser == null || post.getLikes() == null) {
            return false;
        }
        return post.getLikes().stream()
                .anyMatch(u -> u.getId().equals(currentUser.getId()));
    }

    public Integer getUserRating(Post post, User currentUser) {
        if (currentUser == null || post.getRatings() == null) {
            return null;
        }
        return post.getRatings().stream()
                .filter(r -> r.getUser() != null && r.getUser().getId().equals(currentUser.getId()))
                .map(Rating::getScore)
                .findFirst()
                .orElse(null);
    }
}
