package com.Nguyen.blogplatform.domain.post.dto;



import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagRequest {
    private String name;
    private String slug;
    private String description;
    private String color;

}
