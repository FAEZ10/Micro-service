package com.microcommerce.products.repository;

import com.microcommerce.products.entity.StockHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {
    
    /**
     * Trouve l'historique des mouvements pour un produit donné
     */
    Page<StockHistory> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
    
    /**
     * Trouve l'historique des mouvements pour une commande donnée
     */
    List<StockHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);
    
    /**
     * Trouve l'historique par type de mouvement
     */
    Page<StockHistory> findByMovementTypeOrderByCreatedAtDesc(StockHistory.MovementType movementType, Pageable pageable);
    
    /**
     * Trouve l'historique pour un produit et un type de mouvement
     */
    List<StockHistory> findByProductIdAndMovementTypeOrderByCreatedAtDesc(Long productId, StockHistory.MovementType movementType);
    
    /**
     * Trouve l'historique dans une période donnée
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.createdAt BETWEEN :startDate AND :endDate ORDER BY sh.createdAt DESC")
    Page<StockHistory> findByDateRangeOrderByCreatedAtDesc(
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate, 
            Pageable pageable);
    
    /**
     * Trouve l'historique pour un produit dans une période donnée
     */
    @Query("SELECT sh FROM StockHistory sh WHERE sh.productId = :productId AND sh.createdAt BETWEEN :startDate AND :endDate ORDER BY sh.createdAt DESC")
    List<StockHistory> findByProductIdAndDateRange(
            @Param("productId") Long productId,
            @Param("startDate") LocalDateTime startDate, 
            @Param("endDate") LocalDateTime endDate);
}
