package com.Nguyen.blogplatform.payload.request.series;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateSeriesDTO {
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    @Size(min = 10, message = "Description must have at least 10 characters")
    private String description;

    private String thumbnail;

    private Boolean isActive;

    private Boolean isCompleted;
}
