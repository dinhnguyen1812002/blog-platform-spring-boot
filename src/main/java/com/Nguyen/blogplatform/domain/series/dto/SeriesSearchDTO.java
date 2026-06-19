package com.Nguyen.blogplatform.domain.series.dto;



import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeriesSearchDTO {

    private String keyword;
    private String userId;
    private Boolean isActive;
    private Boolean isCompleted;

    @Builder.Default
    private String sortBy = "createdAt"; // createdAt, viewCount, totalPosts

    @Builder.Default
    private String sortDirection = "DESC"; // ASC, DESC

    @Builder.Default
    private Integer page = 0;

    @Builder.Default
    private Integer size = 10;
}
