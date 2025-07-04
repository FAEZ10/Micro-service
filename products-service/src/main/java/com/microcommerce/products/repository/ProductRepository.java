package com.microcommerce.products.repository;

import com.microcommerce.products.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Recherche par SKU
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);

    // Produits actifs seulement
    Page<Product> findByActiveTrue(Pageable pageable);
    List<Product> findByActiveTrue();

    // Recherche par catégorie
    Page<Product> findByCategoryIdAndActiveTrue(Long categoryId, Pageable pageable);
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    // Recherche par nom
    @Query("SELECT p FROM Product p WHERE p.active = true AND LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Product> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);

    // Recherche par tags - fonctionnalité désactivée temporairement (table simplifiée)
    // @Query(value = "SELECT * FROM products p WHERE p.active = true AND :tag = ANY(p.tags)", nativeQuery = true)
    // Page<Product> findByTag(@Param("tag") String tag, Pageable pageable);

    // Recherche par plage de prix
    Page<Product> findByActiveTrueAndPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Produits en stock
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockAvailable > 0")
    Page<Product> findInStock(Pageable pageable);

    // Produits en rupture de stock
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockAvailable = 0")
    Page<Product> findOutOfStock(Pageable pageable);

    // Produits avec stock faible (moins de 10 unités)
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.stockAvailable <= 10 AND p.stockAvailable > 0")
    Page<Product> findLowStock(Pageable pageable);

    // Recherche full-text
    @Query("""
        SELECT p FROM Product p 
        WHERE p.active = true 
        AND (
            LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) 
            OR LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%'))
            OR LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%'))
        )
        """)
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);

    // Recherche avancée avec filtres
    @Query("""
        SELECT p FROM Product p 
        WHERE p.active = true
        AND (:categoryId IS NULL OR p.category.id = :categoryId)
        AND (:minPrice IS NULL OR p.price >= :minPrice)
        AND (:maxPrice IS NULL OR p.price <= :maxPrice)
        AND (:inStock IS NULL OR (:inStock = true AND p.stockAvailable > 0) OR (:inStock = false))
        AND (:search IS NULL OR 
             LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR 
             LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))
        """)
    Page<Product> findWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("inStock") Boolean inStock,
            @Param("search") String search,
            Pageable pageable
    );

    // Produits similaires (même catégorie, prix similaire)
    @Query("""
        SELECT p FROM Product p 
        WHERE p.active = true 
        AND p.id != :productId 
        AND p.category.id = :categoryId 
        AND p.price BETWEEN :minPrice AND :maxPrice
        ORDER BY ABS(p.price - :targetPrice)
        """)
    List<Product> findSimilarProducts(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("targetPrice") BigDecimal targetPrice,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );

    // Statistiques de stock
    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockAvailable > 0")
    long countInStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockAvailable = 0")
    long countOutOfStock();

    @Query("SELECT COUNT(p) FROM Product p WHERE p.active = true AND p.stockAvailable <= 10")
    long countLowStock();

    // Produits les plus récents
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);

    // Vérification de stock pour réservation
    @Query("SELECT p FROM Product p WHERE p.id = :productId AND p.stockAvailable >= :quantity")
    Optional<Product> findByIdWithSufficientStock(@Param("productId") Long productId, @Param("quantity") Integer quantity);

    // Produits par liste d'IDs
    @Query("SELECT p FROM Product p WHERE p.id IN :ids AND p.active = true")
    List<Product> findByIdInAndActiveTrue(@Param("ids") List<Long> ids);
}
