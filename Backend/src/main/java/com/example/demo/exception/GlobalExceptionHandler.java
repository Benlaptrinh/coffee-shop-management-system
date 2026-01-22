package com.example.demo.exception;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * GlobalExceptionHandler
 *
 * Centralized exception handling for REST APIs.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    /**
     * Handles method argument validation errors.
     * @param ex ex
     * @param headers headers
     * @param status status
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        List<ApiError.ApiFieldError> fields = new ArrayList<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(f -> fields.add(new ApiError.ApiFieldError(f.getField(), f.getDefaultMessage())));
        ApiError err = buildError(HttpStatus.BAD_REQUEST, "Validation failed", resolvePath(request), fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles unreadable HTTP message errors.
     * @param ex ex
     * @param headers headers
     * @param status status
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        String msg = ex.getMostSpecificCause() == null ? ex.getMessage() : ex.getMostSpecificCause().getMessage();
        ApiError err = buildError(HttpStatus.BAD_REQUEST, msg, resolvePath(request), null);
        err.setError("Malformed JSON");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles missing request parameter errors.
     * @param ex ex
     * @param headers headers
     * @param status status
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                          @NonNull HttpHeaders headers,
                                                                          @NonNull HttpStatusCode status,
                                                                          @NonNull WebRequest request) {
        String msg = "Missing required parameter: " + ex.getParameterName();
        ApiError err = buildError(HttpStatus.BAD_REQUEST, msg, resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles unsupported HTTP method errors.
     * @param ex ex
     * @param headers headers
     * @param status status
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex,
                                                                         @NonNull HttpHeaders headers,
                                                                         @NonNull HttpStatusCode status,
                                                                         @NonNull WebRequest request) {
        String msg = "Method not supported: " + ex.getMethod();
        ApiError err = buildError(HttpStatus.METHOD_NOT_ALLOWED, msg, resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(err);
    }
    /**
     * Handles unsupported media type errors.
     * @param ex ex
     * @param headers headers
     * @param status status
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatusCode status,
                                                                     @NonNull WebRequest request) {
        String msg = "Unsupported media type: " + ex.getContentType();
        ApiError err = buildError(HttpStatus.UNSUPPORTED_MEDIA_TYPE, msg, resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE).body(err);
    }
    /**
     * Handles internal exception responses.
     * @param ex ex
     * @param body request body
     * @param headers headers
     * @param statusCode status code
     * @param request request payload
     * @return response entity
     */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                            @Nullable Object body,
                                                            @NonNull HttpHeaders headers,
                                                            @NonNull HttpStatusCode statusCode,
                                                            @NonNull WebRequest request) {
        if (body instanceof ApiError) {
            return new ResponseEntity<>(body, headers, statusCode);
        }
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        ApiError err = buildError(status, ex.getMessage(), resolvePath(request), null);
        return new ResponseEntity<>(err, headers, statusCode);
    }
    /**
     * Handles constraint violation errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest request) {
        List<ApiError.ApiFieldError> fields = new ArrayList<>();
        ex.getConstraintViolations()
            .forEach(cv -> fields.add(new ApiError.ApiFieldError(cv.getPropertyPath().toString(), cv.getMessage())));
        ApiError err = buildError(HttpStatus.BAD_REQUEST, "Validation failed", resolvePath(request), fields);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles argument type mismatch errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        String msg = "Invalid value for parameter '" + ex.getName() + "': " + ex.getValue();
        ApiError err = buildError(HttpStatus.BAD_REQUEST, msg, resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles illegal argument errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(err);
    }
    /**
     * Handles illegal state errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiError> handleIllegalState(IllegalStateException ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.CONFLICT, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }
    /**
     * Handles not found errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler({EntityNotFoundException.class, NoSuchElementException.class})
    public ResponseEntity<ApiError> handleNotFound(Exception ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.NOT_FOUND, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
    }
    /**
     * Handles data integrity violations.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.CONFLICT, "Data integrity violation", resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }
    /**
     * Handles access denied errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.FORBIDDEN, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
    }
    /**
     * Handles authentication errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        ApiError err = buildError(HttpStatus.UNAUTHORIZED, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
    }
    /**
     * Handles unexpected errors.
     * @param ex ex
     * @param request request payload
     * @return response entity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleAll(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error handling request {}", request.getRequestURI(), ex);
        ApiError err = buildError(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), resolvePath(request), null);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }

    private ApiError buildError(HttpStatus status, String message, String path, List<ApiError.ApiFieldError> fields) {
        ApiError err = new ApiError();
        err.setStatus(status.value());
        err.setError(status.getReasonPhrase());
        err.setMessage(message);
        err.setPath(path);
        err.setErrors(fields);
        return err;
    }

    private String resolvePath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    private String resolvePath(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
