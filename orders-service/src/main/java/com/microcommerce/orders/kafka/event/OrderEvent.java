package com.microcommerce.orders.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    
    private String eventId;
    private String eventType; // ORDER_CREATED, ORDER_UPDATED, ORDER_CONFIRMED, ORDER_CANCELLED, ITEM_ADDED, ITEM_REMOVED
    private Long orderId;
    private String orderNumber;
    private Long clientId;
    private String clientEmail;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemEvent> items;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Métadonnées de l'événement
    private String source;
    private String version;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemEvent {
        private Long productId;
        private String productSku;
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subtotal;
    }
}
