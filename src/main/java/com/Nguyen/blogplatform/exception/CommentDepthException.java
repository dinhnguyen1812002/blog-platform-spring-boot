package com.Nguyen.blogplatform.exception;

public class CommentDepthException extends RuntimeException {
    public CommentDepthException(String message) {
        super(message);
    }
}