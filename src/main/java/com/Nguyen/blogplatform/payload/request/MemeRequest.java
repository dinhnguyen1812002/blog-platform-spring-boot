package com.Nguyen.blogplatform.payload.request;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Data
@Getter
@Setter
public class MemeRequest {
    private String name;
    private String description;
    private String memeUrl;
    private String userId;
}