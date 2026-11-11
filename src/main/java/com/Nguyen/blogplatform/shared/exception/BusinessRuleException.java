package com.Nguyen.blogplatform.shared.exception;

/**
 * The request is well-formed but violates a business rule
 * (e.g. liking an unpublished post, rating your own post). Maps to HTTP 422.
 */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
