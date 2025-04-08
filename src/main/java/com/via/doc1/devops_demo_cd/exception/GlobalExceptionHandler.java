package com.via.doc1.devops_demo_cd.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice // Makes this class handle exceptions globally
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", "")); // Get requested path

        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Add handlers for other exceptions (e.g., ValidationException, DataIntegrityViolationException) if needed
    /*
    @ExceptionHandler(MethodArgumentNotValidException.class) // Example for validation
    public ResponseEntity<Object> handleValidationExceptions(
            MethodArgumentNotValidException ex, WebRequest request) {
        // ... build error response for validation errors ...
        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
    }
    */

     @ExceptionHandler(Exception.class) // Generic handler for other unexpected errors
     public ResponseEntity<Object> handleGenericException(
             Exception ex, WebRequest request) {

         Map<String, Object> body = new LinkedHashMap<>();
         body.put("timestamp", LocalDateTime.now());
         body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
         body.put("error", "Internal Server Error");
         body.put("message", "An unexpected error occurred: " + ex.getMessage()); // Avoid exposing too much detail in prod
         body.put("path", request.getDescription(false).replace("uri=", ""));

         // Log the full stack trace for debugging
         // log.error("Unhandled exception:", ex);

         return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
     }
}