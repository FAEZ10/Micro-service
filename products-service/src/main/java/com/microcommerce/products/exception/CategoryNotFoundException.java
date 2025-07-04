package com.microcommerce.products.exception;

public class CategoryNotFoundException extends RuntimeException {
    
    public CategoryNotFoundException(String message) {
        super(message);
    }
    
    public CategoryNotFoundException(Long categoryId) {
        super("Catégorie non trouvée avec l'ID: " + categoryId);
    }
    
    public CategoryNotFoundException(String field, String value) {
        super("Catégorie non trouvée avec " + field + ": " + value);
    }
}
