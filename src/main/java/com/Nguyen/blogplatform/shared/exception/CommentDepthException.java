package com.Nguyen.blogplatform.shared.exception;



public class CommentDepthException extends RuntimeException {
    public CommentDepthException(String message) {
        super(message);
    }
}