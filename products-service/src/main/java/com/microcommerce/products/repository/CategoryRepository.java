package com.microcommerce.products.repository;

import com.microcommerce.products.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Recherche par nom
    Optional<Category> findByNameIgnoreCase(String name);

    // Catégories actives seulement
    List<Category> findByActiveTrue();
    Page<Category> findByActiveTrue(Pageable pageable);

    // Catégories racines (sans parent)
    List<Category> findByParentIsNullAndActiveTrue();

    // Sous-catégories d'une catégorie parent
    List<Category> findByParentIdAndActiveTrue(Long parentId);

    // Recherche avec nombre de produits
    @Query("""
        SELECT c, COUNT(p) as productCount 
        FROM Category c 
        LEFT JOIN c.products p 
        WHERE c.active = true 
        GROUP BY c 
        ORDER BY c.name
        """)
    List<Object[]> findActiveCategoriesWithProductCount();

    // Vérifier si une catégorie a des sous-catégories
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.parent.id = :categoryId AND c.active = true")
    boolean hasActiveChildren(@Param("categoryId") Long categoryId);

    // Vérifier si une catégorie a des produits
    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    boolean hasActiveProducts(@Param("categoryId") Long categoryId);

    // Recherche par nom (pour l'autocomplete)
    @Query("SELECT c FROM Category c WHERE c.active = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Category> findByNameContainingIgnoreCase(@Param("name") String name);
    
    // Recherche par nom avec pagination
    @Query("SELECT c FROM Category c WHERE c.active = true AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findByNameContainingIgnoreCaseAndActiveTrue(@Param("name") String name, Pageable pageable);
    
    // Compter les produits dans une catégorie
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    Long countProductsInCategory(@Param("categoryId") Long categoryId);
    
    // Compter les sous-catégories
    Long countByParentIdAndActiveTrue(Long parentId);

    // Hiérarchie complète d'une catégorie (requête SQL native)
    @Query(value = """
        WITH RECURSIVE category_hierarchy AS (
            SELECT id, name, description, parent_id, active, created_at, updated_at, 0 as level
            FROM categories 
            WHERE id = :categoryId
            UNION ALL
            SELECT c.id, c.name, c.description, c.parent_id, c.active, c.created_at, c.updated_at, ch.level + 1
            FROM categories c
            INNER JOIN category_hierarchy ch ON c.parent_id = ch.id
        )
        SELECT * FROM category_hierarchy ORDER BY level, name
        """, nativeQuery = true)
    List<Category> findCategoryHierarchy(@Param("categoryId") Long categoryId);
}
