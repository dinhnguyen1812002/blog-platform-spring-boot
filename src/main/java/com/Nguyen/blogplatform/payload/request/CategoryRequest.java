package com.Nguyen.blogplatform.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {
    @NotNull
    @Size(max = 100)
    private String category;

    @Size(max = 7)
    private String backgroundColor;

    @Size(max = 500)
    private String description;

}
