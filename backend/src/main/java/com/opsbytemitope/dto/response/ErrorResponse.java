package com.opsbytemitope.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response body for all API error scenarios.
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldError> fieldErrors;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Represents a single field-level validation error.
     */
    @Data
    @Builder
    public static class FieldError {
        private String field;
        private Object rejectedValue;
        private String message;
    }
}
