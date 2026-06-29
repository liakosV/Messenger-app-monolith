package com.project.messenger.core;

import com.project.messenger.core.exception.*;
import com.project.messenger.dto.response.ErrorResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(
            AppObjectNotFoundException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value(),
                LocalDateTime.now()
        );

        log.warn("Resource not found: code={}, message={}, status={}",
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value());

        return new ResponseEntity<>(dto, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(AppObjectAlreadyExistsException.class)
    public ResponseEntity<ErrorResponseDto> handleAlreadyExistsException(
            AppObjectAlreadyExistsException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        log.warn("Resource already exists: code={}, message={}, status={}",
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(dto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidArgumentException(
            AppObjectInvalidArgumentException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );

        log.warn("Resource has invalid argument: code={}, message={}, status={}",
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value());

        return new ResponseEntity<>(dto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppObjectIllegalStateException.class)
    public ResponseEntity<ErrorResponseDto> handleIllegalStateException(
            AppObjectIllegalStateException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value(),
                LocalDateTime.now()
        );

        log.warn("Resource has illegal state: code={}, message={}, status={}",
                ex.getCode(),
                ex.getMessage(),
                HttpStatus.CONFLICT.value());

        return new ResponseEntity<>(dto, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AppObjectUnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(
            AppObjectUnauthorizedException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );

        log.warn("Resource unauthorize: code={}, message={}, status={}",
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value());

        return new ResponseEntity<>(dto, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AppObjectAccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(
            AppObjectAccessDeniedException ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value(),
                LocalDateTime.now()
        );

        log.warn("Resource access denied: code={}, message={}, status={}",
                ex.getCode().toUpperCase(),
                ex.getMessage(),
                HttpStatus.FORBIDDEN.value());

        return new ResponseEntity<>(dto, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleException(Exception ex) {

        ErrorResponseDto dto = new ErrorResponseDto(
                "INTERNAL_SERVER_ERROR",
                ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );

        log.error("Unexpected internal server error. type={}, message={}",
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                ex
        );

        return new ResponseEntity<>(dto, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));

        log.warn("Validation failed. errors: {}", errors);

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }
}
