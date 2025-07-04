package com.microcommerce.clients.exception;

public class InvalidCredentialsException extends RuntimeException {
    
    public InvalidCredentialsException() {
        super("Email ou mot de passe incorrect");
    }
    
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
