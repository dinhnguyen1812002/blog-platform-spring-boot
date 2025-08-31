package com.Nguyen.blogplatform.payload.request;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


import java.time.LocalDateTime;
import java.util.*;

@Getter
@Setter
public class PostRequest {
    private String authorName;

    @NotEmpty(message = "Title is required")
    @Size(min = 5, max = 200, message = "Title must be between 5 and 200 characters")
    private String title;

    private String excerpt;
    private Date createdAt;
    private Boolean featured;

    @NotEmpty(message = "Content is required")
    @Size(min = 10, message = "Content must be at least 10 characters")
    private String content;

    private String thumbnail;
    private Set<Long> categories = new HashSet<>();
    private Set<UUID> tags = new HashSet<>();
    private LocalDateTime public_date;

}
