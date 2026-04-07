package com.onlinelearning.quiz.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> r = new HashMap<>(); Map<String, String> e = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(err -> e.put(((FieldError) err).getField(), err.getDefaultMessage()));
        r.put("timestamp", LocalDateTime.now()); r.put("status", 400); r.put("error", "Validation Failed"); r.put("details", e);
        return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
        Map<String, Object> r = new HashMap<>();
        r.put("timestamp", LocalDateTime.now()); r.put("status", 400); r.put("error", "Bad Request"); r.put("message", ex.getMessage());
        return new ResponseEntity<>(r, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> r = new HashMap<>();
        r.put("timestamp", LocalDateTime.now()); r.put("status", 500); r.put("error", "Internal Server Error"); r.put("message", "Something went wrong.");
        return new ResponseEntity<>(r, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
