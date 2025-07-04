package com.microcommerce.products.exception;

public class SkuAlreadyExistsException extends RuntimeException {
    public SkuAlreadyExistsException(String message) {
        super(message);
    }
    
    public SkuAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
