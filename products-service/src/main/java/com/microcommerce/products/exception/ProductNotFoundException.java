package com.microcommerce.products.exception;

public class ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
    public ProductNotFoundException(Long productId) {
        super("Produit non trouvé avec l'ID: " + productId);
    }
    
    public ProductNotFoundException(String field, String value) {
        super("Produit non trouvé avec " + field + ": " + value);
    }
}
