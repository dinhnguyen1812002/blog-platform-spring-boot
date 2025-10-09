package com.Nguyen.blogplatform.payload.request.series;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSeriesPostDTO {
    @NotEmpty(message = "Post ID is required")
    private String postId;

    @NotEmpty(message = "New order index is required")
    private Integer newOrderIndex;
}
