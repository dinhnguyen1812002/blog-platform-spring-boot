package com.Nguyen.blogplatform.payload.request.series;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeriesSearchDTO {
    private String keyword;
    private String userId;
    private Boolean isActive;
    private Boolean isCompleted;
    private String sortBy = "createdAt"; // createdAt, viewCount, totalPosts
    private String sortDirection = "DESC"; // ASC, DESC
    private Integer page = 0;
    private Integer size = 10;
}
