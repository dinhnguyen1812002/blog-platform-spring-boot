package com.Nguyen.blogplatform.shared.exception;



import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a business-level authentication or registration rule is violated
 * (e.g. duplicate username/email, weak password).
 *
 * Annotated with @ResponseStatus so Spring MVC automatically maps it to
 * HTTP 400 Bad Request without needing an explicit @ExceptionHandler.
 * Override with a @ControllerAdvice handler if you need a richer error body.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AuthException extends RuntimeException {

    public AuthException(String message) {
        super(message);
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
    }
}