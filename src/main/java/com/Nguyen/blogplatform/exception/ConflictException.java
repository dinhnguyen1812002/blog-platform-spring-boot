package com.Nguyen.blogplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class ConflictException extends Throwable {
    public ConflictException(String message) {
        super(message);
    }
}
