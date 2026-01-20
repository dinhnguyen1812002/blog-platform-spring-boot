package com.Nguyen.blogplatform.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostSummaryResponse {
    private String id;
    private String title;
    private String excerpt;
    private String slug;
    private String thumbnail;
    private Date createdAt;
    private long views;
    private int likes;


}
