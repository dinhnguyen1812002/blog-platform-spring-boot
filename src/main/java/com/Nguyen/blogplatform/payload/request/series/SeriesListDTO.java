package com.Nguyen.blogplatform.payload.request.series;

import java.time.LocalDateTime;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class SeriesListDTO {

    private String id;
    private String title;
    private String slug;
    private String description;
    private String thumbnail;
    private String username;
    private String userAvatar;
    private Boolean isActive;
    private Boolean isCompleted;
    private Integer totalPosts;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
