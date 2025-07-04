package com.microcommerce.orders.service;

import com.microcommerce.orders.dto.request.AddToCartRequest;
import com.microcommerce.orders.dto.response.OrderItemResponse;
import com.microcommerce.orders.dto.response.OrderResponse;
import com.microcommerce.orders.entity.Order;
import com.microcommerce.orders.entity.OrderItem;
import com.microcommerce.orders.exception.OrderNotFoundException;
import com.microcommerce.orders.kafka.producer.OrderEventProducer;
import com.microcommerce.orders.repository.OrderRepository;
import com.microcommerce.orders.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderEventProducer orderEventProducer;
    private final WebClient.Builder webClientBuilder;

    // ===== Gestion du panier =====

    public OrderResponse getOrCreateCart(Long clientId) {
        log.info("Récupération ou création du panier pour le client: {}", clientId);
        
        Optional<Order> existingCart = orderRepository.findByClientIdAndStatus(clientId, "CART");
        
        if (existingCart.isPresent()) {
            return convertToResponse(existingCart.get());
        }
        
        // Créer un nouveau panier
        Order newCart = Order.builder()
                .clientId(clientId)
                .orderNumber(generateOrderNumber())
                .status("CART")
                .paymentStatus("PENDING")
                .subtotal(BigDecimal.ZERO)
                .shippingCost(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .clientEmail("temp@temp.com") // Sera mis à jour lors de la validation
                .clientFirstName("Temp")
                .clientLastName("Temp")
                .shippingAddress("{}")
                .billingAddress("{}")
                .build();
        
        Order savedCart = orderRepository.save(newCart);
        log.info("Nouveau panier créé avec l'ID: {}", savedCart.getId());
        
        return convertToResponse(savedCart);
    }

    public OrderResponse addToCart(Long clientId, AddToCartRequest request) {
        log.info("Ajout au panier - Client: {}, Produit: {}, Quantité: {}", 
                clientId, request.getProductId(), request.getQuantity());
        
        Order cart = orderRepository.findByClientIdAndStatus(clientId, "CART")
                .orElseGet(() -> {
                    Order newCart = Order.builder()
                            .clientId(clientId)
                            .orderNumber(generateOrderNumber())
                            .status("CART")
                            .paymentStatus("PENDING")
                            .subtotal(BigDecimal.ZERO)
                            .shippingCost(BigDecimal.ZERO)
                            .taxAmount(BigDecimal.ZERO)
                            .discountAmount(BigDecimal.ZERO)
                            .totalAmount(BigDecimal.ZERO)
                            .clientEmail("temp@temp.com")
                            .clientFirstName("Temp")
                            .clientLastName("Temp")
                            .shippingAddress("{}")
                            .billingAddress("{}")
                            .build();
                    return orderRepository.save(newCart);
                });

        Optional<OrderItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst();

        if (existingItem.isPresent()) {
            OrderItem item = existingItem.get();
            item.updateQuantity(item.getQuantity() + request.getQuantity());
            orderItemRepository.save(item);
        } else {
            OrderItem newItem = OrderItem.builder()
                    .order(cart)
                    .productId(request.getProductId())
                    .productName("Produit " + request.getProductId())
                    .productSku("SKU-" + request.getProductId())
                    .unitPrice(BigDecimal.valueOf(99.99))
                    .quantity(request.getQuantity())
                    .subtotal(BigDecimal.valueOf(99.99).multiply(BigDecimal.valueOf(request.getQuantity())))
                    .imageUrl("/images/product-" + request.getProductId() + ".jpg")
                    .build();
            
            cart.addItem(newItem);
            orderItemRepository.save(newItem);
        }

        cart.recalculateTotal();
        Order updatedCart = orderRepository.save(cart);
        
        try {
            orderEventProducer.publishItemAdded(updatedCart, 
                existingItem.orElse(cart.getItems().get(cart.getItems().size() - 1)));
        } catch (Exception e) {
            log.warn("Erreur lors de la publication de l'événement Kafka pour l'ajout au panier: {}", e.getMessage());
        }
        
        log.info("Produit ajouté au panier avec succès");
        return convertToResponse(updatedCart);
    }

    public OrderResponse removeFromCart(Long clientId, Long productId) {
        log.info("Suppression du panier - Client: {}, Produit: {}", clientId, productId);
        
        Order cart = orderRepository.findByClientIdAndStatus(clientId, "CART")
                .orElseThrow(() -> new OrderNotFoundException("Aucun panier trouvé pour le client: " + clientId));

        OrderItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé dans le panier"));

        cart.removeItem(itemToRemove);
        orderItemRepository.delete(itemToRemove);
        
        cart.recalculateTotal();
        Order updatedCart = orderRepository.save(cart);
        
        log.info("Produit supprimé du panier avec succès");
        return convertToResponse(updatedCart);
    }

    // ===== Gestion des commandes =====

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));
        return convertToResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByClient(Long clientId, Pageable pageable) {
        return orderRepository.findByClientIdOrderByCreatedAtDesc(clientId, pageable)
                .map(this::convertToResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::convertToResponse);
    }

    public OrderResponse validateOrder(Long orderId) {
        log.info("Validation de la commande: {}", orderId);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(orderId));

        if (!"CART".equals(order.getStatus())) {
            throw new IllegalArgumentException("Seules les commandes en statut CART peuvent être validées");
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("Impossible de valider une commande vide");
        }

        order.setStatus("PENDING");
        order.setValidatedAt(LocalDateTime.now());
        
        Order validatedOrder = orderRepository.save(order);
        
        try {
            orderEventProducer.publishOrderConfirmed(validatedOrder);
        } catch (Exception e) {
            log.warn("Erreur lors de la publication de l'événement Kafka pour la validation de commande: {}", e.getMessage());
        }
        
        log.info("Commande validée avec succès: {}", orderId);
        
        return convertToResponse(validatedOrder);
    }


    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return "ORD-" + timestamp + "-" + uuid;
    }

    private OrderResponse convertToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(this::convertItemToResponse)
                .toList();

        return OrderResponse.builder()
                .id(order.getId())
                .clientId(order.getClientId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal())
                .shippingCost(order.getShippingCost())
                .taxAmount(order.getTaxAmount())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
                .clientEmail(order.getClientEmail())
                .clientFirstName(order.getClientFirstName())
                .clientLastName(order.getClientLastName())
                .clientPhone(order.getClientPhone())
                .shippingAddress(order.getShippingAddress())
                .billingAddress(order.getBillingAddress())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .validatedAt(order.getValidatedAt())
                .paidAt(order.getPaidAt())
                .shippedAt(order.getShippedAt())
                .deliveredAt(order.getDeliveredAt())
                .carrier(order.getCarrier())
                .trackingNumber(order.getTrackingNumber())
                .clientComment(order.getClientComment())
                .items(itemResponses)
                .totalItems(itemResponses.size())
                .build();
    }

    private OrderItemResponse convertItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .productName(item.getProductName())
                .productDescription(item.getProductDescription())
                .productSku(item.getProductSku())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .subtotal(item.getSubtotal())
                .imageUrl(item.getImageUrl())
                .unitWeight(item.getUnitWeight())
                .createdAt(item.getCreatedAt())
                .build();
    }
}
