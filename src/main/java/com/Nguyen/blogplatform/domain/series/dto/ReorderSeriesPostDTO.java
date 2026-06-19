package com.Nguyen.blogplatform.domain.series.dto;



import com.Nguyen.blogplatform.domain.post.domain.model.Post;
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
