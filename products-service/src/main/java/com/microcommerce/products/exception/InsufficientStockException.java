package com.microcommerce.products.exception;

public class InsufficientStockException extends RuntimeException {
    
    public InsufficientStockException(String message) {
        super(message);
    }
    
    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super("Stock insuffisant pour le produit " + productId + 
              ". Demandé: " + requested + ", Disponible: " + available);
    }
    
    public InsufficientStockException(String productName, Integer requested, Integer available) {
        super("Stock insuffisant pour le produit '" + productName + 
              "'. Demandé: " + requested + ", Disponible: " + available);
    }
}
