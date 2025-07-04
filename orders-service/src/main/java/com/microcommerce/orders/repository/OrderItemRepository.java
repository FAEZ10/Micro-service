package com.microcommerce.orders.repository;

import com.microcommerce.orders.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Recherche par commande
    List<OrderItem> findByOrderIdOrderByCreatedAt(Long orderId);

    // Recherche par produit
    List<OrderItem> findByProductId(Long productId);

    // Statistiques
    @Query("SELECT COUNT(oi) FROM OrderItem oi WHERE oi.productId = :productId")
    long countByProductId(@Param("productId") Long productId);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.productId = :productId")
    Long sumQuantityByProductId(@Param("productId") Long productId);
}
