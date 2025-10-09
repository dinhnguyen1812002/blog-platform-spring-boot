package com.Nguyen.blogplatform.payload.request.series;


import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddPostToSeriesDTO {
    @NotEmpty(message = "Post ID is required")
    private String postId;

    private Integer orderIndex; // Nếu null, sẽ thêm vào cuối
}
