package com.Nguyen.blogplatform.service;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import com.Nguyen.blogplatform.exception.ConflictException;
import com.Nguyen.blogplatform.exception.NotFoundException;
import com.Nguyen.blogplatform.model.SocialMediaLink;
import com.Nguyen.blogplatform.model.User;
import com.Nguyen.blogplatform.payload.request.UserProfileUpdateRequest;
import com.Nguyen.blogplatform.payload.response.UserProfileResponse;
import com.Nguyen.blogplatform.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new ConflictException("Username is already taken");
            }
            user.setUsername(request.getUsername());
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


        // Xử lý social media links
        if (request.getSocialMediaLinks() != null) {
            // Xóa tất cả liên kết hiện tại
            socialMediaLinkRepository.deleteAll(user.getSocialMediaLinks());
            user.getSocialMediaLinks().clear();

            // Thêm các liên kết mới (validation đã được thực hiện bởi @ValidSocialMediaLinks)
            request.getSocialMediaLinks().entrySet().stream()
                    .filter(entry -> entry.getValue() != null && !entry.getValue().trim().isEmpty())
                    .forEach(entry -> {
                        SocialMediaLink link = new SocialMediaLink();
                        link.setUser(user);
                        link.setPlatform(entry.getKey());
                        link.setUrl(entry.getValue());
                        user.getSocialMediaLinks().add(link);
                    });
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
}
