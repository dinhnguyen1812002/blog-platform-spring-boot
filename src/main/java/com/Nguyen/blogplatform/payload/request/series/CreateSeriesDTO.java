package com.Nguyen.blogplatform.payload.request.series;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateSeriesDTO {

    @NotEmpty(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @NotEmpty(message = "Slug is required")
    @Size(min = 5, max = 250, message = "Slug must be between 5 and 250 characters")
    private String slug;

    @Size(min = 10, message = "Description must have at least 10 characters")
    private String description;

    private String thumbnail;

    @Builder.Default
    private Boolean isActive = true;

    @Builder.Default
    private Boolean isCompleted = false;
}
