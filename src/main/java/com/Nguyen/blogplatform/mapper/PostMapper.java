package com.Nguyen.blogplatform.mapper;

import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.response.CategoryResponse;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.TagResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.BookmarkRepository;
import com.Nguyen.blogplatform.service.CommentServices;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

import static com.Nguyen.blogplatform.Utils.ExcerptUtil.excerpt;

@Component
@RequiredArgsConstructor
public class PostMapper {
    private final CommentServices commentServices;

    public PostResponse toPostResponse(Post post, User currentUser, BookmarkRepository savedPostRepository) {
        return PostResponse.builder()
                .id(post.getId())
                .user(createUserResponse(post.getUser()))
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
//                .content(excerpt(post.getContent()))

                .thumbnail(post.getThumbnail())
                .categories(post.getCategories().stream()
                        .map(category -> new CategoryResponse(
                                category.getId(),
                                category.getCategory(),
                                category.getBackgroundColor()))
                        .collect(Collectors.toSet()))
                .tags(post.getTags().stream()
                        .map(tag -> new TagResponse(
                                tag.getUuid(),
                                tag.getName(),
                                tag.getSlug(),
                                tag.getDescription(),
                                tag.getColor()))
                        .collect(Collectors.toSet()))
                .commentCount(post.getComments().size())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(calculateAverageRating(post))
                .isLikedByCurrentUser(isLikedByCurrentUser(post, currentUser))
                .isSavedByCurrentUser(isSavedByCurrentUser(post, currentUser, savedPostRepository))
                .userRating(getUserRating(post, currentUser))
                .public_date(post.getPublic_date())
                .is_publish(post.getIs_publish())
                .build();
    }

    public PostResponse toPostResponseWithComments(Post post, User currentUser, BookmarkRepository savedPostRepository) {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<CommentResponse> commentPage = commentServices.getTopLevelComments(post.getId(), pageable);
        List<CommentResponse> comments = commentPage.getContent();

        return PostResponse.builder()
                .id(post.getId())
                .user(createUserResponse(post.getUser()))
                .title(post.getTitle())
                .excerpt(post.getExcerpt())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .content(post.getContent())
                .thumbnail(post.getThumbnail())
                .categories(post.getCategories().stream()
                        .map(category -> new CategoryResponse(
                                category.getId(),
                                category.getCategory(),
                                category.getBackgroundColor()))
                        .collect(Collectors.toSet()))
                .tags(post.getTags().stream()
                        .map(tag -> new TagResponse(
                                tag.getUuid(),
                                tag.getName(),
                                tag.getSlug(),
                                tag.getDescription(),
                                tag.getColor()))
                        .collect(Collectors.toSet()))
                .comments(comments)
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(calculateAverageRating(post))
                .isLikedByCurrentUser(isLikedByCurrentUser(post, currentUser))
                .isSavedByCurrentUser(isSavedByCurrentUser(post, currentUser, savedPostRepository))
                .userRating(getUserRating(post, currentUser))
                .build();
    }

    public UserResponse createUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    public double calculateAverageRating(Post post) {
        return post.getRatings().stream()
                .mapToDouble(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    public boolean isLikedByCurrentUser(Post post, User currentUser) {
        return currentUser != null && post.getLike().contains(currentUser);
    }

    public Integer getUserRating(Post post, User currentUser) {
        return currentUser != null ?
                post.getRatings().stream()
                        .filter(r -> r.getUser().equals(currentUser))
                        .map(Rating::getScore)
                        .findFirst()
                        .orElse(null) : null;
    }

    public boolean isSavedByCurrentUser(Post post, User currentUser, BookmarkRepository savedPostRepository) {
        try {
            return currentUser != null && savedPostRepository.existsByUserAndPost(currentUser, post);
        } catch (Exception e) {
            return false;
        }
    }
}