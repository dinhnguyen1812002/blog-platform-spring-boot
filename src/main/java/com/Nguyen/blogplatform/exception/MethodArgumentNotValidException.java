package com.Nguyen.blogplatform.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MethodArgumentNotValidException extends Throwable {
    public MethodArgumentNotValidException(String message) {
        super(message);
    }
}
