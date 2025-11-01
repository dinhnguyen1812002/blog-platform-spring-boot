package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import com.Nguyen.blogplatform.exception.ConflictException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.SocialMediaLink;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.UserProfileUpdateRequest;
import com.Nguyen.blogplatform.payload.response.AvatarUploadResponse;
import com.Nguyen.blogplatform.payload.response.PostSummaryResponse;
import com.Nguyen.blogplatform.payload.response.PublicProfileResponse;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final BookmarkRepository savedPostRepository;
    private final CommentRepository commentRepository;
    private final SocialMediaLinkRepository socialMediaLinkRepository;
    private final ProfilePlaceholderService profilePlaceholderService;
    private final com.Nguyen.blogplatform.storage.AvatarStorageService avatarStorageService;

    public UserProfileResponse getUserProfile(UserDetailsImpl userDetails) {
        // Get user from database to get additional info
        User user = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Extract roles from authorities
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        // Get social media links (only non-null URLs)
        Map<ESocialMediaPlatform, String> socialMediaLinks = user.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null) // Chỉ lấy các link có URL
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing, // Xử lý trường hợp trùng platform (lấy giá trị đầu tiên)
                        HashMap::new
                ));

        // Get user statistics
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);
        Long commentsCount = commentRepository.countByUser(user);

        // Process custom profile markdown if it exists
        String rawMarkdown = user.getCustomProfileMarkdown();
        String processedMarkdown = null;
        if (rawMarkdown != null && !rawMarkdown.isBlank()) {
            processedMarkdown = profilePlaceholderService.processPlaceholders(rawMarkdown, user);
        }

        return UserProfileResponse.builder()
                .id(userDetails.getId())
                .username(userDetails.getUsername())
                .email(userDetails.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .socialMediaLinks(socialMediaLinks) // Thêm social media links
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .customProfileMarkdown(processedMarkdown)
                .build();
    }

    public UserProfileResponse getUserProfileById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Extract roles from user entity
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().toString()) // Giả sử Role có phương thức getName() trả về enum hoặc String
                .collect(Collectors.toList());

        // Get social media links (only non-null URLs)
        Map<ESocialMediaPlatform, String> socialMediaLinks = user.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null) // Chỉ lấy các link có URL
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing, // Xử lý trường hợp trùng platform
                        HashMap::new
                ));

        // Get user statistics
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);
        Long commentsCount = commentRepository.countByUser(user);

        // Process custom profile markdown if it exists
        String rawMarkdown = user.getCustomProfileMarkdown();
        String processedMarkdown = null;
        if (rawMarkdown != null && !rawMarkdown.isBlank()) {
            processedMarkdown = profilePlaceholderService.processPlaceholders(rawMarkdown, user);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .socialMediaLinks(socialMediaLinks) // Thêm social media links
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .customProfileMarkdown(processedMarkdown)
                .build();
    }


    public UserProfileResponse updateUserProfile(String userId, UserProfileUpdateRequest request) throws ConflictException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // Kiểm tra username và email trùng lặp
        boolean usernameChanged = false;
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ConflictException("Username is already taken");
            }
            user.setUsername(request.getUsername());
            usernameChanged = true;
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("Email is already taken");
            }
            user.setEmail(request.getEmail());
        }

        // Cập nhật các trường khác
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getWebsite() != null) {
            user.setWebsite(request.getWebsite());
        }

        // Regenerate slug if username changed or slug is blank
        if (usernameChanged || user.getSlug() == null || user.getSlug().isBlank()) {
            String base = com.Nguyen.blogplatform.util.SlugUtil.toSlug(user.getUsername());
            String unique = ensureUniqueSlug(base, user.getId());
            user.setSlug(unique);
        }

        // Xử lý social media links
        if (request.getSocialMediaLinks() != null) {
            // Xóa tất cả liên kết hiện tại
            socialMediaLinkRepository.deleteAll(user.getSocialMediaLinks());
            user.getSocialMediaLinks().clear();

            // Validate and add new links
            for (Map.Entry<ESocialMediaPlatform, String> entry : request.getSocialMediaLinks().entrySet()) {
                String url = entry.getValue();
                if (url != null && !url.trim().isEmpty()) {
                    // Validate URL format
                    if (!isValidUrl(url)) {
                        throw new IllegalArgumentException("Invalid URL format for " + entry.getKey() + ": " + url);
                    }
                    SocialMediaLink link = new SocialMediaLink();
                    link.setUser(user);
                    link.setPlatform(entry.getKey());
                    link.setUrl(url);
                    user.getSocialMediaLinks().add(link);
                }
            }
        }

        // Update custom user information (markdown)
        if (request.getCustomInformation() != null) {
            user.setCustomProfileMarkdown(request.getCustomInformation());
        }

        // Lưu user
        userRepository.save(user);

        // Lấy danh sách roles
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());

        // Lấy social media links
        Map<ESocialMediaPlatform, String> socialMediaLinks = user.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null)
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));


        // Lấy thống kê
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);
        Long commentsCount = commentRepository.countByUser(user);

        // Process custom profile markdown if it exists
        String rawMarkdown = user.getCustomProfileMarkdown();
        String processedMarkdown = null;
        if (rawMarkdown != null && !rawMarkdown.isBlank()) {
            processedMarkdown = profilePlaceholderService.processPlaceholders(rawMarkdown, user);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .socialMediaLinks(socialMediaLinks)
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .customProfileMarkdown(processedMarkdown)
                .build();
    }

    public UserProfileResponse getUserProfileByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found with username: " + username));

        // Extract roles from user entity
        List<String> roles = user.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());

        // Get social media links (only non-null URLs)
        Map<ESocialMediaPlatform, String> socialMediaLinks = user.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null)
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        // Get user statistics
        Long postsCount = postRepository.countByUser(user);
        Long savedPostsCount = savedPostRepository.countByUser(user);
        Long commentsCount = commentRepository.countByUser(user);

        // Process custom profile markdown if it exists
        String rawMarkdown = user.getCustomProfileMarkdown();
        String processedMarkdown = null;
        if (rawMarkdown != null && !rawMarkdown.isBlank()) {
            processedMarkdown = profilePlaceholderService.processPlaceholders(rawMarkdown, user);
        }

        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .avatar(user.getAvatar())
                .roles(roles)
                .socialMediaLinks(socialMediaLinks)
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .customProfileMarkdown(processedMarkdown)
                .build();
    }

    @Transactional
    public void updateUserAvatar(String userId, String avatarUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));
        user.setAvatar(avatarUrl);
        userRepository.save(user);
    }

    @Transactional
    public UserProfileResponse updateUserProfileMarkdown(String userId, String markdownContent) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found with id: " + userId));

        // Save the raw markdown content
        user.setCustomProfileMarkdown(markdownContent);
        User updatedUser = userRepository.save(user);

        // Process placeholders before returning response
        String processedContent = null;
        if (markdownContent != null && !markdownContent.isBlank()) {
            processedContent = profilePlaceholderService.processPlaceholders(markdownContent, updatedUser);
        }

        // Extract roles
        List<String> roles = updatedUser.getRoles().stream()
                .map(role -> role.getName().toString())
                .collect(Collectors.toList());

        // Get social media links
        Map<ESocialMediaPlatform, String> socialMediaLinks = updatedUser.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null)
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        // Get user statistics
        Long postsCount = postRepository.countByUser(updatedUser);
        Long savedPostsCount = savedPostRepository.countByUser(updatedUser);
        Long commentsCount = commentRepository.countByUser(updatedUser);

        return UserProfileResponse.builder()
                .id(updatedUser.getId())
                .username(updatedUser.getUsername())
                .email(updatedUser.getEmail())
                .avatar(updatedUser.getAvatar())
                .roles(roles)
                .socialMediaLinks(socialMediaLinks)
                .postsCount(postsCount)
                .savedPostsCount(savedPostsCount)
                .commentsCount(commentsCount)
                .customProfileMarkdown(processedContent)
                .build();
    }

    private boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        // Simple URL validation regex
        String urlRegex = "^(https?://)([\\da-z\\.-]+)\\.([a-z\\.]{2,6})([/\\w \\.-]*)*/?$";
        Pattern pattern = Pattern.compile(urlRegex, Pattern.CASE_INSENSITIVE);
        return pattern.matcher(url.trim()).matches();
    }

    private String ensureUniqueSlug(String base, String currentUserId) {
        if (base == null || base.isBlank()) base = "user";
        String candidate = base;
        int suffix = 1;
        while (true) {
            var existing = userRepository.findBySlug(candidate);
            if (existing.isEmpty() || existing.get().getId().equals(currentUserId)) {
                return candidate;
            }
            suffix++;
            candidate = base + "-" + suffix;
        }
    }

    @Transactional
    public AvatarUploadResponse uploadAvatar(String userId, MultipartFile file) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        String old = user.getAvatar();
        String storedPublicUrl = avatarStorageService.store(file, userId);
        user.setAvatar(storedPublicUrl);
        userRepository.save(user);
        if (old != null && !old.isBlank()) {
            avatarStorageService.delete(old);
        }
        return AvatarUploadResponse.builder()
                .url(storedPublicUrl)
                .build();
    }

    public PublicProfileResponse getPublicProfileBySlug(String slug) {
        User user = userRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("User not found"));
        long postCount = postRepository.countByUser(user);
        var featured = postRepository.findTop5ByUserAndFeaturedTrueOrderByCreatedAtDesc(user);
        var featuredDtos = featured.stream()
                .map(p -> PostSummaryResponse.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .excerpt(p.getExcerpt())
                        .slug(p.getSlug())
                        .thumbnail(p.getThumbnail())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();

        Map<ESocialMediaPlatform, String> socialMediaLinks = user.getSocialMediaLinks().stream()
                .filter(link -> link.getUrl() != null)
                .collect(Collectors.toMap(
                        SocialMediaLink::getPlatform,
                        SocialMediaLink::getUrl,
                        (existing, replacement) -> existing,
                        HashMap::new
                ));

        return PublicProfileResponse.builder()
                .username(user.getUsername())
                .slug(user.getSlug())
                .avatar(user.getAvatar())
                .bio(user.getBio())
                .socialMediaLinks(socialMediaLinks)
                .website(user.getWebsite())
                .customInformation(user.getCustomProfileMarkdown())
                .postCount(postCount)
                .featuredPosts(featuredDtos)
                .build();
    }
}
