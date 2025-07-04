package com.microcommerce.orders.kafka.producer;

import com.microcommerce.orders.entity.Order;
import com.microcommerce.orders.entity.OrderItem;
import com.microcommerce.orders.kafka.event.OrderEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String ORDER_EVENTS_TOPIC = "order-events";

    public void publishOrderCreated(Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CREATED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClientId())
                .clientEmail(order.getClientEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItems(order))
                .timestamp(LocalDateTime.now())
                .source("orders-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishOrderUpdated(Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_UPDATED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClientId())
                .clientEmail(order.getClientEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItems(order))
                .timestamp(LocalDateTime.now())
                .source("orders-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishOrderConfirmed(Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CONFIRMED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClientId())
                .clientEmail(order.getClientEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItems(order))
                .timestamp(LocalDateTime.now())
                .source("orders-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishOrderCancelled(Order order) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ORDER_CANCELLED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClientId())
                .clientEmail(order.getClientEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItems(order))
                .timestamp(LocalDateTime.now())
                .source("orders-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishItemAdded(Order order, OrderItem item) {
        OrderEvent event = OrderEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("ITEM_ADDED")
                .orderId(order.getId())
                .orderNumber(order.getOrderNumber())
                .clientId(order.getClientId())
                .clientEmail(order.getClientEmail())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .items(mapOrderItems(order))
                .timestamp(LocalDateTime.now())
                .source("orders-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    private java.util.List<OrderEvent.OrderItemEvent> mapOrderItems(Order order) {
        if (order.getItems() == null) {
            return java.util.Collections.emptyList();
        }
        
        return order.getItems().stream()
                .map(item -> OrderEvent.OrderItemEvent.builder()
                        .productId(item.getProductId())
                        .productSku(item.getProductSku())
                        .productName(item.getProductName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

    private void publishEvent(OrderEvent event) {
        try {
            log.info("Publishing order event: {} for order ID: {}", event.getEventType(), event.getOrderId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                ORDER_EVENTS_TOPIC, 
                event.getOrderId().toString(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published order event: {} for order ID: {} to partition: {} with offset: {}",
                            event.getEventType(), 
                            event.getOrderId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish order event: {} for order ID: {}", 
                            event.getEventType(), 
                            event.getOrderId(), 
                            ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing order event: {} for order ID: {}", 
                    event.getEventType(), 
                    event.getOrderId(), 
                    e);
        }
    }
}
