package com.Nguyen.blogplatform.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class TagResponse {
    private UUID uuid;
    private String name;
    private String slug;
    private String description;
    private String color;

    public TagResponse(UUID id, String name, String slug, String description, String color) {
        this.uuid = id;
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.color = color;
    }
}
