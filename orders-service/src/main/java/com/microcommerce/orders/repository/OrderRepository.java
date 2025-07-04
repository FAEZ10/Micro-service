package com.microcommerce.orders.repository;

import com.microcommerce.orders.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Recherche par numéro de commande
    Optional<Order> findByOrderNumber(String orderNumber);

    // Recherche par client
    Page<Order> findByClientIdOrderByCreatedAtDesc(Long clientId, Pageable pageable);

    // Recherche par statut
    Page<Order> findByStatusOrderByCreatedAtDesc(String status, Pageable pageable);

    // Recherche par client et statut
    Page<Order> findByClientIdAndStatusOrderByCreatedAtDesc(Long clientId, String status, Pageable pageable);

    // Panier actuel d'un client
    Optional<Order> findByClientIdAndStatus(Long clientId, String status);

    // Commandes par période
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    Page<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, 
                               @Param("endDate") LocalDateTime endDate, 
                               Pageable pageable);

    // Recherche par email client
    Page<Order> findByClientEmailContainingIgnoreCaseOrderByCreatedAtDesc(String email, Pageable pageable);

    // Statistiques
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") String status);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.clientId = :clientId")
    long countByClientId(@Param("clientId") Long clientId);

    // Commandes récentes
    List<Order> findTop10ByOrderByCreatedAtDesc();
}
