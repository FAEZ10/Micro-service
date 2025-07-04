package com.microcommerce.clients.exception;

public class ClientNotFoundException extends RuntimeException {
    
    public ClientNotFoundException(String message) {
        super(message);
    }
    
    public ClientNotFoundException(Long clientId) {
        super("Client avec l'ID " + clientId + " introuvable");
    }
    
    public ClientNotFoundException(String field, String value) {
        super("Client avec " + field + " '" + value + "' introuvable");
    }
}
