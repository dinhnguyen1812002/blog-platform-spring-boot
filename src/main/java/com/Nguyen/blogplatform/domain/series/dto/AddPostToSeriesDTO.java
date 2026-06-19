package com.Nguyen.blogplatform.domain.series.dto;




import com.Nguyen.blogplatform.domain.post.domain.model.Post;
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
