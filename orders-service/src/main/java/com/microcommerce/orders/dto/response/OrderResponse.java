package com.microcommerce.orders.dto.response;

import com.microcommerce.orders.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long clientId;
    private String orderNumber;
    private String status;
    private String paymentStatus;
    
    // Montants
    private BigDecimal subtotal;
    private BigDecimal shippingCost;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    
    // Informations client
    private String clientEmail;
    private String clientFirstName;
    private String clientLastName;
    private String clientPhone;
    
    // Adresses
    private String shippingAddress;
    private String billingAddress;
    
    // Dates
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime validatedAt;
    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    
    // Livraison
    private String carrier;
    private String trackingNumber;
    private String clientComment;
    
    // Articles
    private List<OrderItemResponse> items;
    
    // Statistiques
    private Integer totalItems;
}
