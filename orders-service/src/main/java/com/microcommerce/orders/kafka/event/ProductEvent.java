package com.microcommerce.orders.kafka.event;

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
    private String eventType;
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
    
    private String source;
    private String version;
    private String externalReference;
}
