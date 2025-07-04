package com.microcommerce.products.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductEvent {
    
    private String eventId;
    private String eventType; // PRODUCT_CREATED, PRODUCT_UPDATED, PRODUCT_DELETED, STOCK_UPDATED, STOCK_RESERVED, STOCK_RELEASED
    private Long productId;
    private String name;
    private String sku;
    private BigDecimal price;
    private Integer stockAvailable;
    private Integer previousStock;
    private Integer newStock;
    private String reason;
    private Boolean active;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Métadonnées de l'événement
    private String source;
    private String version;
    private String externalReference; // Order ID, etc.
}
