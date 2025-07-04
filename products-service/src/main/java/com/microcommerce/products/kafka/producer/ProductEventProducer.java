package com.microcommerce.products.kafka.producer;

import com.microcommerce.products.entity.Product;
import com.microcommerce.products.kafka.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String PRODUCT_EVENTS_TOPIC = "product-events";

    public void publishProductCreated(Product product) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PRODUCT_CREATED")
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockAvailable(product.getStockAvailable())
                .active(product.getActive())
                .timestamp(LocalDateTime.now())
                .source("products-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishProductUpdated(Product product) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("PRODUCT_UPDATED")
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .price(product.getPrice())
                .stockAvailable(product.getStockAvailable())
                .active(product.getActive())
                .timestamp(LocalDateTime.now())
                .source("products-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishStockUpdated(Product product, Integer previousStock, String reason) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("STOCK_UPDATED")
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .stockAvailable(product.getStockAvailable())
                .previousStock(previousStock)
                .newStock(product.getStockAvailable())
                .reason(reason)
                .timestamp(LocalDateTime.now())
                .source("products-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishStockReserved(Product product, Integer quantity, String externalReference) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("STOCK_RESERVED")
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .stockAvailable(product.getStockAvailable())
                .newStock(quantity)
                .reason("Stock reserved for order")
                .externalReference(externalReference)
                .timestamp(LocalDateTime.now())
                .source("products-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishStockReleased(Product product, Integer quantity, String externalReference) {
        ProductEvent event = ProductEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("STOCK_RELEASED")
                .productId(product.getId())
                .name(product.getName())
                .sku(product.getSku())
                .stockAvailable(product.getStockAvailable())
                .newStock(quantity)
                .reason("Stock released from cancelled order")
                .externalReference(externalReference)
                .timestamp(LocalDateTime.now())
                .source("products-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    private void publishEvent(ProductEvent event) {
        try {
            log.info("Publishing product event: {} for product ID: {}", event.getEventType(), event.getProductId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                PRODUCT_EVENTS_TOPIC, 
                event.getProductId().toString(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published product event: {} for product ID: {} to partition: {} with offset: {}",
                            event.getEventType(), 
                            event.getProductId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish product event: {} for product ID: {}", 
                            event.getEventType(), 
                            event.getProductId(), 
                            ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing product event: {} for product ID: {}", 
                    event.getEventType(), 
                    event.getProductId(), 
                    e);
        }
    }
}
