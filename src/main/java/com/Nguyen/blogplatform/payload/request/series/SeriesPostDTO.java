package com.Nguyen.blogplatform.payload.request.series;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class SeriesPostDTO {
    private String postId;
    private String title;
    private String slug;
    private String excerpt;
    private String thumbnail;
    private Integer orderIndex;
    private LocalDateTime addedAt;
    private LocalDateTime publicDate;
}
