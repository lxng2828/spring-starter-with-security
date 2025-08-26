package com.vtlong.my_spring_boot_project.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.vtlong.my_spring_boot_project.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ApiResponse<Object>> handleAppException(
                        AppException ex, HttpServletRequest request) {

                ErrorCode errorCode = ex.getErrorCode();
                HttpStatus httpStatus = determineHttpStatus(errorCode);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                httpStatus.value(),
                                errorCode.getCode());

                return ResponseEntity.status(httpStatus).body(apiResponse);
        }

        @ExceptionHandler(AccessDeniedException.class)
        public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(
                        AccessDeniedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "You do not have permission to access this resource",
                                HttpStatus.FORBIDDEN.value(),
                                "Access Denied");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
        }

        @ExceptionHandler(AuthorizationDeniedException.class)
        public ResponseEntity<ApiResponse<Object>> handleAuthorizationDeniedException(
                        AuthorizationDeniedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Access permission denied",
                                HttpStatus.FORBIDDEN.value(),
                                "Authorization Denied");

                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(apiResponse);
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
        public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                List<String> errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Invalid request parameters",
                                HttpStatus.BAD_REQUEST.value(),
                                errors);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(ConstraintViolationException.class)
        public ResponseEntity<ApiResponse<Object>> handleConstraintViolation(
                        ConstraintViolationException ex, HttpServletRequest request) {

                List<String> errors = ex.getConstraintViolations()
                                .stream()
                                .map(ConstraintViolation::getMessage)
                                .collect(Collectors.toList());

                String errorMessage = String.join(", ", errors);

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Invalid request data",
                                HttpStatus.BAD_REQUEST.value(),
                                errors);

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(HttpMessageNotReadableException.class)
        public ResponseEntity<ApiResponse<Object>> handleHttpMessageNotReadable(
                        HttpMessageNotReadableException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Request body format is invalid",
                                HttpStatus.BAD_REQUEST.value(),
                                "Invalid JSON");

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(MissingServletRequestParameterException.class)
        public ResponseEntity<ApiResponse<Object>> handleMissingParameter(
                        MissingServletRequestParameterException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Required parameter '" + ex.getParameterName() + "' is missing",
                                HttpStatus.BAD_REQUEST.value(),
                                "Missing Parameter");

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(MethodArgumentTypeMismatchException.class)
        public ResponseEntity<ApiResponse<Object>> handleException(
                        MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

                Class<?> requiredType = ex.getRequiredType();
                String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "Parameter '" + ex.getName() + "' should be of type " + typeName,
                                HttpStatus.BAD_REQUEST.value(),
                                "Type Mismatch");

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
        public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(
                        HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "HTTP method '" + ex.getMethod() + "' is not supported for this endpoint",
                                HttpStatus.METHOD_NOT_ALLOWED.value(),
                                "Method Not Allowed");

                return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(apiResponse);
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(
                        IllegalArgumentException ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                ex.getMessage(),
                                HttpStatus.BAD_REQUEST.value(),
                                "Bad Request");

                return ResponseEntity.badRequest().body(apiResponse);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Object>> handleGenericException(
                        Exception ex, HttpServletRequest request) {

                ApiResponse<Object> apiResponse = ApiResponse.error(
                                "An unexpected error occurred. Please try again later.",
                                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                                "Internal Server Error");

                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
        }
}
