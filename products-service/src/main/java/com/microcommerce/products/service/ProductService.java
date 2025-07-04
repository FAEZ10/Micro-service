package com.microcommerce.products.service;

import com.microcommerce.products.dto.request.CreateProductRequest;
import com.microcommerce.products.dto.request.StockUpdateRequest;
import com.microcommerce.products.dto.request.UpdateProductRequest;
import com.microcommerce.products.dto.response.ProductResponse;
import com.microcommerce.products.entity.Category;
import com.microcommerce.products.entity.Product;
import com.microcommerce.products.exception.CategoryNotFoundException;
import com.microcommerce.products.exception.InsufficientStockException;
import com.microcommerce.products.exception.ProductNotFoundException;
import com.microcommerce.products.exception.SkuAlreadyExistsException;
import com.microcommerce.products.repository.CategoryRepository;
import com.microcommerce.products.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // ===== CRUD Operations =====

    public ProductResponse createProduct(CreateProductRequest request) {
        log.info("Création d'un nouveau produit avec SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new SkuAlreadyExistsException(request.getSku());
        }

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockAvailable(request.getStockAvailable() != null ? request.getStockAvailable() : 0)
                .category(category)
                .sku(request.getSku())
                .imageUrl(request.getImageUrl())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Produit créé avec succès: ID={}, SKU={}", savedProduct.getId(), savedProduct.getSku());
        return convertToResponse(savedProduct);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
        return convertToResponse(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ProductNotFoundException("SKU", sku));
        return convertToResponse(product);
    }

    public ProductResponse updateProduct(Long id, UpdateProductRequest request) {
        log.info("Mise à jour du produit ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(request.getCategoryId()));
            product.setCategory(category);
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Produit mis à jour avec succès: ID={}", updatedProduct.getId());
        return convertToResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        log.info("Suppression du produit ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));

        productRepository.delete(product);
        log.info("Produit supprimé avec succès: ID={}", id);
    }

    // ===== Stock Management =====

    public ProductResponse updateStock(Long productId, StockUpdateRequest request) {
        log.info("Mise à jour du stock pour le produit ID: {}, type: {}, quantité: {}",
                productId, request.getMovementType(), request.getQuantity());

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        int oldStock = product.getStockAvailable();
        int newStock;

        switch (request.getMovementType()) {
            case "INBOUND":
                newStock = oldStock + Math.abs(request.getQuantity());
                break;
            case "OUTBOUND":
                newStock = oldStock - Math.abs(request.getQuantity());
                if (newStock < 0) {
                    throw new InsufficientStockException(productId, Math.abs(request.getQuantity()), oldStock);
                }
                break;
            case "ADJUSTMENT":
                newStock = oldStock + request.getQuantity(); // Peut être positif ou négatif
                if (newStock < 0) {
                    throw new InsufficientStockException(productId, Math.abs(request.getQuantity()), oldStock);
                }
                break;
            default:
                throw new IllegalArgumentException("Type de mouvement invalide: " + request.getMovementType());
        }

        product.setStockAvailable(newStock);
        Product updatedProduct = productRepository.save(product);

        log.info("Stock mis à jour avec succès: ID={}, ancien stock: {}, nouveau stock: {}",
                productId, oldStock, newStock);

        return convertToResponse(updatedProduct);
    }

    public boolean reserveStock(Long productId, Integer quantity) {
        log.info("Réservation de stock: produit ID={}, quantité={}", productId, quantity);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        if (product.getStockAvailable() < quantity) {
            log.warn("Stock insuffisant pour la réservation: produit ID={}, quantité demandée={}", productId, quantity);
            return false;
        }

        product.reserveStock(quantity);
        productRepository.save(product);

        log.info("Stock réservé avec succès: produit ID={}, quantité={}", productId, quantity);
        return true;
    }

    // ===== Search and Filtering =====

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryIdAndActiveTrue(categoryId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(String search, Pageable pageable) {
        return productRepository.searchProducts(search, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findWithFilters(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, 
                                               Boolean inStock, String search, Pageable pageable) {
        return productRepository.findWithFilters(categoryId, minPrice, maxPrice, inStock, search, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findWithFilters(Long categoryId, BigDecimal minPrice, BigDecimal maxPrice, 
                                               Boolean inStock, String search, int page, int size, String sort) {
        // Parser le paramètre de tri
        Sort sortObj = parseSort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        
        return productRepository.findWithFilters(categoryId, minPrice, maxPrice, inStock, search, pageable)
                .map(this::convertToResponse);
    }

    // ===== Utility Methods =====

    private Sort parseSort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by("id").ascending();
        }
        
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "asc";
            
            if (!isValidSortProperty(property)) {
                property = "id";
            }
            
            return "desc".equalsIgnoreCase(direction) 
                ? Sort.by(property).descending() 
                : Sort.by(property).ascending();
        } catch (Exception e) {
            log.warn("Erreur lors du parsing du tri '{}', utilisation du tri par défaut", sort);
            return Sort.by("id").ascending();
        }
    }
    
    private boolean isValidSortProperty(String property) {
        return List.of("id", "name", "price", "stockAvailable", "createdAt", "updatedAt", "sku").contains(property);
    }

    private ProductResponse convertToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockAvailable(product.getStockAvailable())
                .category(product.getCategory() != null ? 
                    com.microcommerce.products.dto.response.CategoryResponse.builder()
                        .id(product.getCategory().getId())
                        .name(product.getCategory().getName())
                        .description(product.getCategory().getDescription())
                        .parentId(product.getCategory().getParent() != null ? 
                            product.getCategory().getParent().getId() : null)
                        .active(product.getCategory().getActive())
                        .createdAt(product.getCategory().getCreatedAt())
                        .updatedAt(product.getCategory().getUpdatedAt())
                        .build() : null)
                .sku(product.getSku())
                .imageUrl(product.getImageUrl())
                .active(product.getActive())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
