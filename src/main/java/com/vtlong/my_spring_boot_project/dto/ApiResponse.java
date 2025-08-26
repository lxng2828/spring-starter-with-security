package com.vtlong.my_spring_boot_project.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private LocalDateTime timestamp;
    private Integer status;
    private Boolean success;
    private String message;
    private T data;
    private Object error;
    private String path;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .success(true)
                .message("Thành công")
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message, HttpServletRequest request) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .path(request != null ? request.getRequestURI() : null)
                .build();
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .success(true)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, HttpServletRequest request) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .success(true)
                .message(message)
                .path(request != null ? request.getRequestURI() : null)
                .build();
    }

    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(200)
                .success(true)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer status, HttpServletRequest request) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .success(false)
                .message(message)
                .path(request != null ? request.getRequestURI() : null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer status) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .success(false)
                .message(message)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer status, Object error, HttpServletRequest request) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .success(false)
                .message(message)
                .error(error)
                .path(request != null ? request.getRequestURI() : null)
                .build();
    }

    public static <T> ApiResponse<T> error(String message, Integer status, Object error) {
        return ApiResponse.<T>builder()
                .timestamp(LocalDateTime.now())
                .status(status)
                .success(false)
                .message(message)
                .error(error)
                .build();
    }
}
