package com.microcommerce.products.dto.response;

import com.microcommerce.products.entity.StockHistory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockHistoryResponse {

    private Long id;
    private Long productId;
    private String movementType;
    private String movementTypeDescription;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private Long orderId;
    private String reason;
    private LocalDateTime createdAt;

    /**
     * Convertit une entité StockHistory en DTO de réponse
     */
    public static StockHistoryResponse fromEntity(StockHistory stockHistory) {
        return StockHistoryResponse.builder()
                .id(stockHistory.getId())
                .productId(stockHistory.getProductId())
                .movementType(stockHistory.getMovementType().name())
                .movementTypeDescription(stockHistory.getMovementType().getDescription())
                .quantity(stockHistory.getQuantity())
                .previousStock(stockHistory.getPreviousStock())
                .newStock(stockHistory.getNewStock())
                .orderId(stockHistory.getOrderId())
                .reason(stockHistory.getReason())
                .createdAt(stockHistory.getCreatedAt())
                .build();
    }
}
