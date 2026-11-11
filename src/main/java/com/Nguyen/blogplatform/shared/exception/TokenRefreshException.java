package com.Nguyen.blogplatform.shared.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class TokenRefreshException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * @param token ignored — kept for source compatibility. The raw token is a
     *              credential and must never end up in messages or logs.
     */
    public TokenRefreshException(String token, String message) {
        super(message);
    }
}