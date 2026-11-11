package com.Nguyen.blogplatform.shared.exception;



import com.Nguyen.blogplatform.domain.notification.application.TelegramNotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String INTERNAL_ERROR = "An internal error occurred. Please try again later.";

    private static Map<String, Object> body(String message) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", message);
        body.put("message", message);
        return body;
    }

    @Autowired
    private TelegramNotificationService telegramNotificationService;

    // Handler for NotFoundException
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(NotFoundException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        // Send formatted error to Telegram
        telegramNotificationService.sendErrorNotification(
                "NotFoundException",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    // Handler for InvalidCategoryException
    @ExceptionHandler(InvalidCategoryException.class)
    public ResponseEntity<Map<String, String>> handleInvalidCategoryException(InvalidCategoryException ex,
            WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        // Send formatted error to Telegram
        telegramNotificationService.sendErrorNotification(
                "InvalidCategoryException",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    // Handler for UnauthorizedException
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, String>> handleUnauthorizedException(UnauthorizedException ex,
            WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        // Send formatted error to Telegram
        telegramNotificationService.sendErrorNotification(
                "UnauthorizedException",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    // Handler for IllegalStateException
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIllegalStateException(IllegalStateException ex, WebRequest request) {
        String message;
        if (ex.getMessage().contains("Ambiguous handler methods mapped")) {
            message = "Ambiguous endpoint mapping. Please check the API documentation.";
        } else {
            message = "An unexpected error occurred.";
        }

        // Send formatted error to Telegram
        telegramNotificationService.sendErrorNotification(
                "IllegalStateException",
                message,
                request.getDescription(false));

        return new ErrorResponse(message);
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<Map<String, Object>> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body(ex.getMessage()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, String>> handleConflictException(ConflictException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenException(ForbiddenException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        response.put("error", ex.getMessage());

        telegramNotificationService.sendErrorNotification(
                "ForbiddenException",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    // ── Security: must not fall through to the generic 500 handler ─────────
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body("Access denied"));
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body("Authentication required"));
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<Map<String, Object>> handleTokenRefresh(TokenRefreshException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body(ex.getMessage()));
    }

    // ── Client errors ───────────────────────────────────────────────────────
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(body(ex.getMessage()));
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(e -> fieldErrors.putIfAbsent(e.getField(), e.getDefaultMessage()));
        Map<String, Object> response = body("Validation failed");
        response.put("errors", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    // @ExceptionHandler(MethodArgumentNotValidException.class)
    // public ResponseEntity<Map<String, String>>
    // handleValidationExceptions(MethodArgumentNotValidException ex) {
    // Map<String, String> errors = new HashMap<>();
    // for (FieldError error : ex.getBindingResult().getFieldErrors()) {
    // errors.put(error.getField(), error.getDefaultMessage());
    // }
    // return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    // }

    @ExceptionHandler(org.springframework.transaction.TransactionSystemException.class)
    public ResponseEntity<Map<String, String>> handleTransactionException(org.springframework.transaction.TransactionSystemException ex, WebRequest request) {
        Map<String, String> response = new HashMap<>();
        Throwable cause = ex.getRootCause();
        String message = (cause != null) ? cause.getMessage() : ex.getMessage();
        log.error("Transaction failed", ex);
        // Root cause may contain SQL/constraint details: keep it server-side only
        response.put("error", "Transaction failed");

        telegramNotificationService.sendErrorNotification(
                "TransactionSystemException",
                message,
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(org.springframework.web.server.ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(Map.of("error", ex.getReason() == null ? "Request failed" : ex.getReason()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleGeneralException(Exception ex, WebRequest request) {
        log.error("Unhandled exception at {}", request.getDescription(false), ex);
        Map<String, String> response = new HashMap<>();
        response.put("error", INTERNAL_ERROR);
        response.put("message", INTERNAL_ERROR);

        telegramNotificationService.sendErrorNotification(
                "GeneralException",
                ex.getMessage(),
                request.getDescription(false));

        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
