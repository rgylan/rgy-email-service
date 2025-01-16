package com.rgy.email.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles Bean Validation errors for @Valid annotated request bodies.
     * <p>
     * - WebExchangeBindException is thrown in WebFlux when validation fails.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidationException(WebExchangeBindException ex) {
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("error", "Validation Failed");
        responseBody.put("details", ex.getFieldErrors().stream()
                .map(fieldError -> {
                    // Build a user-friendly error message
                    return String.format("%s: %s (rejected value: %s)",
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            fieldError.getRejectedValue());
                })
                .collect(Collectors.toList()));
        return Mono.just(ResponseEntity
                .badRequest()
                .body(responseBody));
    }

    /**
     * Handles any scenario where request data is malformed,
     * such as JSON parse errors, missing request body, etc.
     */
    @ExceptionHandler(ServerWebInputException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<Map<String, String>>> handleServerWebInputException(ServerWebInputException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Bad Request");
        errorMap.put("message", ex.getReason());
        return Mono.just(ResponseEntity.badRequest().body(errorMap));
    }

    @ExceptionHandler(AllProvidersFailedException.class)
    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    public Mono<Map<String, String>> handleAllProvidersFailedException(AllProvidersFailedException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Service Unavailable");
        errorMap.put("message", ex.getMessage());
        return Mono.just(errorMap);
    }

    /**
     * Example: catch a more generic error and return a 500 or custom status.
     * In a production app, you might handle fallback or downstream provider errors here.
     */
    @ExceptionHandler(ResponseStatusException.class)
    public Mono<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", ex.getStatusCode().toString());
        errorMap.put("message", ex.getReason() != null ? ex.getReason() : "Error occurred");
        return Mono.just(errorMap);
    }

    /**
     * A fallback for unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<Map<String, String>> handleGenericException(Exception ex) {
        Map<String, String> errorMap = new HashMap<>();
        errorMap.put("error", "Internal Server Error");
        errorMap.put("message", ex.getMessage());
        // In real usage, log the exception or trace it properly
        return Mono.just(errorMap);
    }
}

