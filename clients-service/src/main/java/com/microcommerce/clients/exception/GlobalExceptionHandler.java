package com.microcommerce.clients.exception;

import com.microcommerce.clients.dto.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.mapping.PropertyReferenceException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ClientNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClientNotFound(
            ClientNotFoundException ex, 
            HttpServletRequest request) {
        
        logger.warn("Client not found: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "CLIENT_NOT_FOUND",
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.NOT_FOUND.value()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleEmailAlreadyExists(
            EmailAlreadyExistsException ex, 
            HttpServletRequest request) {
        
        logger.warn("Email already exists: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "EMAIL_ALREADY_EXISTS",
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.CONFLICT.value()
        );
        
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex, 
            HttpServletRequest request) {
        
        logger.warn("Invalid credentials attempt from IP: {}", request.getRemoteAddr());
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_CREDENTIALS",
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.UNAUTHORIZED.value()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, 
            HttpServletRequest request) {
        
        List<String> details = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(FieldError::getDefaultMessage)
            .collect(Collectors.toList());
        
        logger.warn("Validation errors: {}", details);
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Erreurs de validation dans les données fournies",
            details,
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ErrorResponse> handlePropertyReference(
            PropertyReferenceException ex, 
            HttpServletRequest request) {
        
        logger.warn("Invalid sort property: {}", ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_SORT_PROPERTY",
            "Propriété de tri invalide. Utilisez des propriétés valides comme: id, email, firstName, lastName, createdAt",
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, 
            HttpServletRequest request) {
        
        logger.warn("Illegal argument: {}", ex.getMessage());
        
        // Vérifier si c'est une erreur de tri
        if (ex.getMessage() != null && ex.getMessage().contains("sort")) {
            ErrorResponse error = new ErrorResponse(
                "INVALID_SORT_PARAMETER",
                "Paramètre de tri invalide. Format attendu: sort=propriété,direction (ex: sort=lastName,asc)",
                request.getRequestURI(),
                HttpStatus.BAD_REQUEST.value()
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_ARGUMENT",
            ex.getMessage(),
            request.getRequestURI(),
            HttpStatus.BAD_REQUEST.value()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, 
            HttpServletRequest request) {
        
        logger.error("Unexpected error occurred", ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_SERVER_ERROR",
            "Une erreur interne s'est produite. Veuillez réessayer plus tard.",
            request.getRequestURI(),
            HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
