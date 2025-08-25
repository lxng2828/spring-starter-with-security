package com.vtlong.my_spring_boot_project.exception;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.vtlong.my_spring_boot_project.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ErrorResponse> handleAppException(
                        AppException ex, HttpServletRequest request) {

                ErrorCode errorCode = ex.getErrorCode();
                HttpStatus httpStatus = determineHttpStatus(errorCode);

                // Enhanced logging with more context
                logger.warn("Application exception for {}: {} - {} - Status: {} - User-Agent: {}",
                                request.getRequestURI(),
                                errorCode.getCode(),
                                ex.getMessage(),
                                httpStatus.value(),
                                request.getHeader("User-Agent"));

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(httpStatus.value())
                                .error(errorCode.getCode())
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(httpStatus).body(errorResponse);
        }

        private HttpStatus determineHttpStatus(ErrorCode errorCode) {
                switch (errorCode) {
                        case USER_NOT_FOUND:
                        case ROLE_NOT_FOUND:
                        case RESOURCE_NOT_FOUND:
                                return HttpStatus.NOT_FOUND;
                        case USER_ALREADY_EXISTS:
                        case ROLE_ALREADY_EXISTS:
                                return HttpStatus.CONFLICT;
                        case USER_INVALID_INPUT:
                        case VALIDATION_ERROR:
                        case BAD_REQUEST:
                                return HttpStatus.BAD_REQUEST;
                        case USER_UNAUTHORIZED:
                                return HttpStatus.UNAUTHORIZED;
                        case METHOD_NOT_ALLOWED:
                                return HttpStatus.METHOD_NOT_ALLOWED;
                        case REQUEST_TIMEOUT:
                                return HttpStatus.REQUEST_TIMEOUT;
                        case TOO_MANY_REQUESTS:
                                return HttpStatus.TOO_MANY_REQUESTS;
                        case SERVICE_UNAVAILABLE:
                                return HttpStatus.SERVICE_UNAVAILABLE;
                        case INTERNAL_SERVER_ERROR:
                        default:
                                return HttpStatus.INTERNAL_SERVER_ERROR;
                }
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                logger.error("Validation error for {}: {}", request.getRequestURI(), errorMessage);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Validation Error")
                                .message("Invalid request parameters")
                                .path(request.getRequestURI())
                                .details(errors)
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ErrorResponse> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest request) {

                List<String> errors = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                logger.error("Constraint violation for {}: {}", request.getRequestURI(), errorMessage);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Constraint Violation")
                                .message("Invalid request data")
                                .path(request.getRequestURI())
                                .details(errors)
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {

                logger.error("JSON parsing error for {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Invalid JSON")
                                .message("Request body format is invalid")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ErrorResponse> handleMissingParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {

                logger.error("Missing parameter for {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Missing Parameter")
                                .message("Required parameter '" + ex.getParameterName() + "' is missing")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ErrorResponse> handleException(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

                logger.error("Type mismatch for {}: {}", request.getRequestURI(), ex.getMessage());

                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Type Mismatch")
                                .message("Parameter '" + ex.getName() + "' should be of type " + typeName)
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ErrorResponse> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

                logger.warn("Method not supported for {}: {} - Supported methods: {}",
                                request.getRequestURI(), ex.getMethod(), ex.getSupportedMethods());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                                .error("Method Not Allowed")
                                .message("HTTP method '" + ex.getMethod() + "' is not supported for this endpoint")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {

                logger.error("Illegal argument for {}: {}", request.getRequestURI(), ex.getMessage());

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.BAD_REQUEST.value())
                                .error("Bad Request")
                                .message(ex.getMessage())
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.badRequest().body(errorResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ErrorResponse> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                logger.error("Unexpected error occurred for {}: ", request.getRequestURI(), ex);

                ErrorResponse errorResponse = ErrorResponse.builder()
                                .timestamp(LocalDateTime.now())
                                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                                .error("Internal Server Error")
                                .message("An unexpected error occurred. Please try again later.")
                                .path(request.getRequestURI())
                                .build();

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
}
