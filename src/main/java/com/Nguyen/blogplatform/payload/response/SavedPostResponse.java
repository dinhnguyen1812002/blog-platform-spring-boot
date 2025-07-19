package com.Nguyen.blogplatform.payload.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedPostResponse {
    private String id;
    private PostResponse post;
    private UserResponse user;
    private String notes;
    private LocalDateTime savedAt;
}
