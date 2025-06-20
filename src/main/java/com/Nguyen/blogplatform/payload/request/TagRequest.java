package com.Nguyen.blogplatform.payload.request;

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
