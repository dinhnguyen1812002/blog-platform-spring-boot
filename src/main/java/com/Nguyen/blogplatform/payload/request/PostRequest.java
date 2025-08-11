package com.Nguyen.blogplatform.payload.request;

import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
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
}
