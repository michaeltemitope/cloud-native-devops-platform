package com.opsbytemitope.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Standard API response envelope for all endpoints.
 *
 * @param <T> the type of payload data
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    /**
     * Creates a successful response with data.
     *
     * @param data the response payload
     * @param <T>  the payload type
     * @return wrapped success response
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    /**
     * Creates a successful response with a message and data.
     *
     * @param message the success message
     * @param data    the response payload
     * @param <T>     the payload type
     * @return wrapped success response
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    /**
     * Creates an error response.
     *
     * @param message the error message
     * @param <T>     the payload type
     * @return wrapped error response
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
}
