package com.Nguyen.blogplatform.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchItemDTO {
    private String title;
    private String slug;
    private String thumbnail;
    private String authorName;
    private Long view;
    private Long like;
}
