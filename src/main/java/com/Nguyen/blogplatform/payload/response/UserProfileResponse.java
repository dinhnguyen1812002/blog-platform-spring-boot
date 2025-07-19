package com.Nguyen.blogplatform.payload.response;

import lombok.*;

import java.util.List;

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
    private List<String> roles;

    // Additional user profile information
    private Long postsCount;
    private Long savedPostsCount;
    private Long commentsCount;
}
