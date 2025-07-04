package com.microcommerce.products.service;

import com.microcommerce.products.dto.request.CreateProductRequest;
import com.microcommerce.products.dto.request.StockUpdateRequest;
import com.microcommerce.products.dto.request.UpdateProductRequest;
import com.microcommerce.products.dto.response.ProductResponse;
import com.microcommerce.products.dto.response.StockHistoryResponse;
import com.microcommerce.products.entity.Category;
import com.microcommerce.products.entity.Product;
import com.microcommerce.products.exception.CategoryNotFoundException;
import com.microcommerce.products.exception.InsufficientStockException;
import com.microcommerce.products.exception.ProductNotFoundException;
import com.microcommerce.products.exception.SkuAlreadyExistsException;
import com.microcommerce.products.repository.CategoryRepository;
import com.microcommerce.products.repository.ProductRepository;
import com.microcommerce.products.repository.StockHistoryRepository;
import com.microcommerce.products.entity.StockHistory;
import com.microcommerce.products.kafka.event.OrderEvent;
import com.microcommerce.products.kafka.producer.ProductEventProducer;
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
    private final StockHistoryRepository stockHistoryRepository;
    private final ProductEventProducer productEventProducer;

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

    // ===== Stock History =====

    /**
     * Récupère l'historique des mouvements de stock pour un produit donné
     */
    @Transactional(readOnly = true)
    public Page<StockHistoryResponse> getProductStockHistory(Long productId, int page, int size, String sort) {
        log.info("Récupération de l'historique de stock pour le produit ID: {}, page: {}, size: {}, sort: {}", 
                productId, page, size, sort);

        // Vérifier que le produit existe
        if (!productRepository.existsById(productId)) {
            throw new ProductNotFoundException(productId);
        }

        // Parser le paramètre de tri pour l'historique
        Sort sortObj = parseStockHistorySort(sort);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        // Récupérer l'historique paginé
        Page<StockHistory> stockHistoryPage = stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId, pageable);

        log.info("Historique de stock récupéré avec succès pour le produit ID: {}, {} entrées trouvées", 
                productId, stockHistoryPage.getTotalElements());

        // Convertir en DTO de réponse
        return stockHistoryPage.map(StockHistoryResponse::fromEntity);
    }

    /**
     * Parse le paramètre de tri spécifique à l'historique de stock
     */
    private Sort parseStockHistorySort(String sort) {
        if (sort == null || sort.trim().isEmpty()) {
            return Sort.by("createdAt").descending();
        }
        
        try {
            String[] parts = sort.split(",");
            String property = parts[0].trim();
            String direction = parts.length > 1 ? parts[1].trim() : "desc";
            
            if (!isValidStockHistorySortProperty(property)) {
                property = "createdAt";
            }
            
            return "asc".equalsIgnoreCase(direction) 
                ? Sort.by(property).ascending() 
                : Sort.by(property).descending();
        } catch (Exception e) {
            log.warn("Erreur lors du parsing du tri d'historique '{}', utilisation du tri par défaut", sort);
            return Sort.by("createdAt").descending();
        }
    }
    
    /**
     * Vérifie si la propriété de tri est valide pour l'historique de stock
     */
    private boolean isValidStockHistorySortProperty(String property) {
        return List.of("id", "createdAt", "movementType", "quantity", "previousStock", "newStock", "orderId").contains(property);
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

    // ===== Order Event Processing =====

    /**
     * Traite la réduction de stock suite à la validation d'une commande
     */
    public void processOrderStockReduction(List<OrderEvent.OrderItemEvent> items, Long orderId) {
        log.info("Traitement de la réduction de stock pour la commande ID: {}", orderId);
        
        for (OrderEvent.OrderItemEvent item : items) {
            try {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                
                int previousStock = product.getStockAvailable();
                
                if (!product.canReserve(item.getQuantity())) {
                    log.error("Stock insuffisant pour le produit ID: {}, stock disponible: {}, quantité demandée: {}", 
                             item.getProductId(), previousStock, item.getQuantity());
                    
                    // Publier un événement de stock insuffisant
                    publishStockInsufficientEvent(orderId, item, previousStock);
                    continue;
                }
                
                // Réduire le stock
                product.reserveStock(item.getQuantity());
                productRepository.save(product);
                
                // Créer l'historique
                createStockHistory(
                    product.getId(), 
                    StockHistory.MovementType.ORDER_REDUCTION, 
                    item.getQuantity(), 
                    previousStock, 
                    product.getStockAvailable(), 
                    orderId, 
                    "Réduction automatique suite à validation commande #" + orderId
                );
                
                log.info("Stock réduit avec succès pour le produit ID: {}, ancien stock: {}, nouveau stock: {}", 
                        product.getId(), previousStock, product.getStockAvailable());
                        
            } catch (Exception e) {
                log.error("Erreur lors de la réduction de stock pour le produit ID: {}", item.getProductId(), e);
                // Publier un événement d'erreur
                publishStockErrorEvent(orderId, item, e.getMessage());
            }
        }
    }

    /**
     * Restaure le stock suite à l'annulation d'une commande
     */
    public void restoreOrderStock(List<OrderEvent.OrderItemEvent> items, Long orderId) {
        log.info("Restauration du stock pour la commande annulée ID: {}", orderId);
        
        for (OrderEvent.OrderItemEvent item : items) {
            try {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ProductNotFoundException(item.getProductId()));
                
                int previousStock = product.getStockAvailable();
                
                // Restaurer le stock
                product.addStock(item.getQuantity());
                productRepository.save(product);
                
                // Créer l'historique
                createStockHistory(
                    product.getId(), 
                    StockHistory.MovementType.ORDER_CANCELLATION, 
                    item.getQuantity(), 
                    previousStock, 
                    product.getStockAvailable(), 
                    orderId, 
                    "Restauration suite à annulation commande #" + orderId
                );
                
                log.info("Stock restauré avec succès pour le produit ID: {}, ancien stock: {}, nouveau stock: {}", 
                        product.getId(), previousStock, product.getStockAvailable());
                        
            } catch (Exception e) {
                log.error("Erreur lors de la restauration de stock pour le produit ID: {}", item.getProductId(), e);
            }
        }
    }

    /**
     * Crée une entrée dans l'historique des mouvements de stock
     */
    private void createStockHistory(Long productId, StockHistory.MovementType movementType, 
                                  Integer quantity, Integer previousStock, Integer newStock, 
                                  Long orderId, String reason) {
        StockHistory stockHistory = StockHistory.builder()
                .productId(productId)
                .movementType(movementType)
                .quantity(quantity)
                .previousStock(previousStock)
                .newStock(newStock)
                .orderId(orderId)
                .reason(reason)
                .build();
        
        stockHistoryRepository.save(stockHistory);
        log.debug("Historique de stock créé: {}", stockHistory);
    }

    /**
     * Publie un événement de stock insuffisant
     */
    private void publishStockInsufficientEvent(Long orderId, OrderEvent.OrderItemEvent item, Integer availableStock) {
        try {
            // Créer l'événement de stock insuffisant
            // TODO: Implémenter la publication d'événement vers le service orders
            log.warn("Publication d'événement STOCK_INSUFFICIENT pour commande: {}, produit: {}, stock disponible: {}", 
                    orderId, item.getProductId(), availableStock);
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement STOCK_INSUFFICIENT", e);
        }
    }

    /**
     * Publie un événement d'erreur de stock
     */
    private void publishStockErrorEvent(Long orderId, OrderEvent.OrderItemEvent item, String errorMessage) {
        try {
            // Créer l'événement d'erreur
            // TODO: Implémenter la publication d'événement vers le service orders
            log.error("Publication d'événement STOCK_ERROR pour commande: {}, produit: {}, erreur: {}", 
                     orderId, item.getProductId(), errorMessage);
        } catch (Exception e) {
            log.error("Erreur lors de la publication de l'événement STOCK_ERROR", e);
        }
    }
}
