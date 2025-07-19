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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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


        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        var author = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found with id: " + authorId));
        var categories = getCategoriesFromIds(postRequest.getCategories());
        var tags = getTagsFromIds(postRequest.getTags());
        String thumbnailUrl = null;
//        if (thumbnail != null && !thumbnail.isEmpty()) {
//            try {
//                thumbnailUrl = fileStorageService.saveThumbnail(thumbnail);
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to upload image: " + e.getMessage());
//            }
//        }
        var post = Post.builder()
                .title(postRequest.getTitle())
                .slug(SlugUtil.createSlug(postRequest.getTitle()))
                .content(Objects.requireNonNullElse(postRequest.getContent(), ""))
                .thumbnail(postRequest.getThumbnail())
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

        // Send newsletter to subscribers asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                newsletterService.sendNewsletterForNewPost(savedPost);
            } catch (Exception e) {
                // Log error but don't fail the post creation
                System.err.println("Failed to send newsletter for post: " + savedPost.getTitle() + " - " + e.getMessage());
            }
        });

        return savedPost;
    }

    @Transactional
    public PostResponse updatePost(String postId, PostRequest postRequest, MultipartFile imageFile) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("Post not found with id: " + postId));
        validateAuthorization(post);
        String thumbnail = postRequest.getThumbnail();
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                thumbnail = fileStorageService.saveThumbnail(imageFile);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload image: " + e.getMessage());
            }
        }

        post.setTitle(postRequest.getTitle());
        post.setContent(Objects.requireNonNullElse(postRequest.getContent(), ""));
        post.setThumbnail(Objects.requireNonNullElse(thumbnail, post.getThumbnail()));
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
                .thumbnail(post.getThumbnail())
                .categories(post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()))
                .tags(post.getTags().stream().map(Tags::getName).collect(Collectors.toSet()))
                .commentCount(post.getComments().size())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(post.getRatings().stream().mapToDouble(Rating::getScore).average().orElse(0.0))
                .build();
    }
}