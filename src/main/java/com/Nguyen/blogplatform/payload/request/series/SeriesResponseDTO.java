package com.Nguyen.blogplatform.payload.request.series;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeriesResponseDTO {
    private String id;
    private String title;
    private String slug;
    private String description;
    private String thumbnail;
    private String userId;
    private String username;
    private String userAvatar;
    private Boolean isActive;
    private Boolean isCompleted;
    private Integer totalPosts;
    private Long viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<SeriesPostDTO> posts;

}


