package com.auca.contractsystem.exception;

import com.auca.contractsystem.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(AucaApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleAucaApi(AucaApiException e) {
        log.error("AUCA API error: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
            .body(ApiResponse.error("AUCA service error: " + e.getMessage()));
    }

    @ExceptionHandler(ContractException.class)
    public ResponseEntity<ApiResponse<Void>> handleContract(ContractException e) {
        return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ApiResponse.error(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(MethodArgumentNotValidException e) {
        String errors = e.getBindingResult().getFieldErrors().stream()
            .map(FieldError::getDefaultMessage).collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(ApiResponse.error(errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        log.error("Unexpected error: {}", e.getMessage(), e);
        return ResponseEntity.internalServerError().body(ApiResponse.error("An unexpected error occurred"));
    }
}
