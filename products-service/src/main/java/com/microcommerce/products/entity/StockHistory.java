package com.microcommerce.products.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "stock_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class StockHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Column(name = "order_id")
    private Long orderId;

    @Column(length = 255)
    private String reason;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MovementType {
        ORDER_REDUCTION("Réduction suite à validation de commande"),
        ORDER_CANCELLATION("Restauration suite à annulation de commande"),
        MANUAL_ADJUSTMENT("Ajustement manuel du stock"),
        INBOUND("Entrée de stock"),
        OUTBOUND("Sortie de stock"),
        ADJUSTMENT("Ajustement de stock");

        private final String description;

        MovementType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    @Override
    public String toString() {
        return "StockHistory{" +
                "id=" + id +
                ", productId=" + productId +
                ", movementType=" + movementType +
                ", quantity=" + quantity +
                ", previousStock=" + previousStock +
                ", newStock=" + newStock +
                ", orderId=" + orderId +
                ", reason='" + reason + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
