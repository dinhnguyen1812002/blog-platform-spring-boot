package com.Nguyen.blogplatform.payload.response;

import lombok.Getter;
import lombok.Setter;
import java.util.Date;

@Setter
@Getter
public class CommentResponse {

    private String id;
    private String comment;
    private Date createdAt;
    private String authorName;

    public CommentResponse() {
    }

    public CommentResponse(String id, String comment, Date createdAt, String authorName) {
        this.id = id;
        this.comment = comment;
        this.createdAt = createdAt;
        this.authorName = authorName;
    }

}
