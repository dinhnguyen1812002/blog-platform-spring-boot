package com.Nguyen.blogplatform.payload.request.series;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReorderSeriesPostDTO {
    @NotBlank(message = "Post ID is required")
    private String postId;

    @NotNull(message = "New order index is required")
    private Integer newOrderIndex;
}
