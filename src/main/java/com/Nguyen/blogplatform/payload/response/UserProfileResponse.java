package com.Nguyen.blogplatform.payload.response;

import com.Nguyen.blogplatform.Enum.ESocialMediaPlatform;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private String id;
    private String username;
    private String email;
    private String avatar;
    private String bio;
    private List<String> roles;
    private Map<ESocialMediaPlatform, String> socialMediaLinks;
    // Additional user profile information
    private Long postsCount;
    private Long savedPostsCount;
    private Long commentsCount;
    private String customProfileMarkdown;

}
