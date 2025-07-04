package com.microcommerce.clients.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String email) {
        super("Un compte avec l'email '" + email + "' existe déjà");
    }
}
