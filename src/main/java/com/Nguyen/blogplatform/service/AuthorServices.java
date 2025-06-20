package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Utils.SlugUtil;
import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.exception.UnauthorizedException;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.CategoryRepository;
import com.Nguyen.blogplatform.repository.PostRepository;
import com.Nguyen.blogplatform.repository.TagRepository;
import com.Nguyen.blogplatform.repository.UserRepository;
import com.Nguyen.blogplatform.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
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

    public List<PostResponse> getPostsForCurrentUser(int page, int size) {
        var username = getCurrentUsername();
        var pageable = PageRequest.of(page, size);
        return postRepository.findByUser(username, pageable)
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    @Transactional
    public Post newPost(PostRequest postRequest, String authorId) throws BadRequestException {
        if (postRequest == null || postRequest.getTitle() == null || postRequest.getTitle().isBlank()) {
            throw new BadRequestException("Invalid input data");
        }

        var author = userRepository.findById(authorId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));
        var categories = getCategoriesFromIds(postRequest.getCategories());
        var tags = getTagsFromIds(postRequest.getTags());

        var post = Post.builder()
                .title(postRequest.getTitle())
                .slug(SlugUtil.createSlug(postRequest.getTitle()))
                .content(Objects.requireNonNullElse(postRequest.getContent(), ""))
                .imageUrl(Objects.requireNonNullElse(postRequest.getImageUrl(), ""))
                .user(author)
                .createdAt(Objects.requireNonNullElse(postRequest.getCreatedAt(), new Date()))
                .categories(categories)
                .tags(tags)
                .featured(DEFAULT_FEATURED)
                .view(0L)
                .build();

        var savedPost = postRepository.save(post);
        notificationService.sendPostNotification(savedPost.getId(), "New post created: " + postRequest.getTitle());
        notificationService.sendGlobalNotification("New post available: " + savedPost.getTitle());
        return savedPost;
    }

    @Transactional
    public PostResponse updatePost(String postId, PostRequest postRequest) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        validateAuthorization(post);

        post.setTitle(postRequest.getTitle());
        post.setContent(Objects.requireNonNullElse(postRequest.getContent(), ""));
        post.setImageUrl(Objects.requireNonNullElse(postRequest.getImageUrl(), ""));
        post.setCategories(getCategoriesFromIds(postRequest.getCategories()));
        if (postRequest.getTags() != null) {
            post.setTags(getTagsFromIds(postRequest.getTags()));
        }

        return toPostResponse(postRepository.save(post));
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
        return tagIds == null || tagIds.isEmpty() ? new HashSet<>() :
                tagIds.stream()
                        .map(id -> tagRepository.findById(id)
                                .orElseThrow(() -> new NotFoundException("Tag not found with id: " + id)))
                        .collect(Collectors.toSet());
    }

    private PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .user(new UserResponse(post.getUser().getId(), post.getUser().getUsername(), post.getUser().getEmail()))
                .title(post.getTitle())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .categories(post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()))
                .tags(post.getTags().stream().map(Tags::getName).collect(Collectors.toSet()))
                .commentCount(post.getComments().size())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(post.getRatings().stream().mapToDouble(Rating::getScore).average().orElse(0.0))
                .build();
    }
}