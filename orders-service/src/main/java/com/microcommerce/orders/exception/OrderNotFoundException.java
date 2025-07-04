package com.microcommerce.orders.exception;

public class OrderNotFoundException extends RuntimeException {
    public OrderNotFoundException(Long id) {
        super("Commande non trouvée avec l'ID: " + id);
    }
    
    public OrderNotFoundException(String orderNumber) {
        super("Commande non trouvée avec le numéro: " + orderNumber);
    }
}
