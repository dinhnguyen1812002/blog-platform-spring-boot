package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Utils.SlugUtil;
import com.Nguyen.blogplatform.exception.InvalidCategoryException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.*;
import com.Nguyen.blogplatform.payload.request.PostRequest;
import com.Nguyen.blogplatform.payload.response.CommentResponse;
import com.Nguyen.blogplatform.payload.response.PostResponse;
import com.Nguyen.blogplatform.payload.response.UserResponse;
import com.Nguyen.blogplatform.repository.*;
import com.Nguyen.blogplatform.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.Nguyen.blogplatform.Utils.ExcerptUtil.excerpt;

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

    private final Map<String, PostResponse> postCache = new ConcurrentHashMap<>();
    private final Map<String, Long> cacheTimestamps = new ConcurrentHashMap<>();

//    public Post createPost(PostRequest request) {
//        var slug = SlugUtil.createSlug(request.getTitle());
//        var author = getCurrentUser();
//        var categories = getCategoriesFromIds(request.getCategories());
//
//        var post = Post.builder()
//                .title(request.getTitle())
//                .slug(slug)
//                .content(request.getContent())
//                .imageUrl(request.getImageUrl())
//                .user(author)
//                .createdAt(new Date())
//                .categories(categories)
//                .view(0L)
//                .featured(Objects.requireNonNullElse(request.getFeatured(), false))
//                .build();
//
//        var savedPost = postRepository.save(post);
//        notificationService.sendGlobalNotification("New post: " + post.getTitle());
//        return savedPost;
//    }

    @Transactional
    public void incrementViewCount(String postId) {
        postRepository.incrementViewCount(postId);
        invalidateCache(postId);
    }

    @Transactional
    public boolean toggleLike(String postId) {
        var post = findPostById(postId);
        var user = getCurrentUser();
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
        var user = getCurrentUser();
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
                .map(this::toPostResponse);
    }

    public List<PostResponse> getLatestPosts(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    public List<PostResponse> getFeaturedPosts(Pageable pageable) {
        return postRepository.findByFeaturedTrueOrderByCreatedAtDesc(pageable)
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    public PostResponse getPostById(String postId) {
        var post = findPostById(postId);
        incrementViewCount(postId);
        return toPostResponseWithComments(post);
    }

    public PostResponse getPostBySlug(String slug) {
        var cacheKey = "slug:" + slug;
        if (isCacheValid(cacheKey)) {
            return postCache.get(cacheKey);
        }

        var post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Post not found with slug: " + slug));
        incrementViewCount(post.getId());
        var response = toPostResponseWithComments(post);
        cachePost(cacheKey, response);
        return response;
    }

    public List<PostResponse> getPostsByCategory(Long categoryId) {
        var category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found with id: " + categoryId));
        return postRepository.findByCategoriesContaining(category)
                .stream()
                .map(this::toPostResponse)
                .toList();
    }

    public List<PostResponse> searchPosts(String title, Long categoryId) {
        Specification<Post> spec = Specification.where(PostSpecification.hasTitle(title))
                .and(PostSpecification.hasCategoryId(categoryId));
        return postRepository.findAll(spec)
                .stream()
                .map(this::toPostResponse)
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
        var userId = jwtUtils.getUserIdFromJwtToken(jwt);
        return userRepository.findById(userId).orElse(null);
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

    private PostResponse toPostResponse(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .user(createUserResponse(post.getUser()))
                .title(post.getTitle())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .content(excerpt(post.getContent()))
                .imageUrl(post.getImageUrl())
                .categories(post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()))
                .tags(post.getTags().stream().map(Tags::getName).collect(Collectors.toSet()))
                .commentCount(post.getComments().size())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(calculateAverageRating(post))
                .isLikedByCurrentUser(isLikedByCurrentUser(post))
                .userRating(getUserRating(post))
                .build();
    }

    private PostResponse toPostResponseWithComments(Post post) {
        return PostResponse.builder()
                .id(post.getId())
                .user(createUserResponse(post.getUser()))
                .title(post.getTitle())
                .slug(post.getSlug())
                .createdAt(post.getCreatedAt())
                .featured(post.getFeatured())
                .content(post.getContent())
                .imageUrl(post.getImageUrl())
                .categories(post.getCategories().stream().map(Category::getCategory).collect(Collectors.toSet()))
                .comments(post.getComments().stream().map(this::toCommentResponse).toList())
                .viewCount(post.getView())
                .likeCount((long) post.getLike().size())
                .averageRating(calculateAverageRating(post))
                .isLikedByCurrentUser(isLikedByCurrentUser(post))
                .userRating(getUserRating(post))
                .build();
    }

    private UserResponse createUserResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail());
    }

    private double calculateAverageRating(Post post) {
        return post.getRatings().stream()
                .mapToDouble(Rating::getScore)
                .average()
                .orElse(0.0);
    }

    private boolean isLikedByCurrentUser(Post post) {
        var currentUser = getCurrentUser();
        return currentUser != null && post.getLike().contains(currentUser);
    }

    private Integer getUserRating(Post post) {
        var currentUser = getCurrentUser();
        return currentUser != null ?
                post.getRatings().stream()
                        .filter(r -> r.getUser().equals(currentUser))
                        .map(Rating::getScore)
                        .findFirst()
                        .orElse(null) : null;
    }

    private CommentResponse toCommentResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getUser().getUsername()
        );
    }

    private boolean isCacheValid(String key) {
        var timestamp = cacheTimestamps.get(key);
        return timestamp != null && System.currentTimeMillis() - timestamp < CACHE_EXPIRY_MS && postCache.containsKey(key);
    }

    private void cachePost(String key, PostResponse response) {
        postCache.put(key, response);
        cacheTimestamps.put(key, System.currentTimeMillis());
    }

    private void invalidateCache(String postId) {
        postCache.entrySet().removeIf(entry -> entry.getKey().equals(postId) || entry.getValue().getId().equals(postId));
    }
}