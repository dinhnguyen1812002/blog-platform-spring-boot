package com.Nguyen.blogplatform.payload.request;

import com.Nguyen.blogplatform.model.Category;
import com.Nguyen.blogplatform.model.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
@Getter
@Setter
public class PostRequest {
    private String authorName;
    private String title;
    private Date createdAt;
    private Boolean featured;
    private String content;
    private String imageUrl;
    private Set<Long> categories = new HashSet<>();

}
