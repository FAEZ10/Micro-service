package com.microcommerce.orders.kafka.consumer;

import com.microcommerce.orders.kafka.event.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductEventConsumer {

    @KafkaListener(
        topics = "product-events",
        groupId = "orders-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProductEvent(
            @Payload ProductEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received product event: {} for product ID: {} from topic: {}, partition: {}, key: {}",
                    event.getEventType(), event.getProductId(), topic, partition, key);

            switch (event.getEventType()) {
                case "STOCK_UPDATED":
                    handleStockUpdated(event);
                    break;
                case "PRODUCT_UPDATED":
                    handleProductUpdated(event);
                    break;
                case "PRODUCT_DELETED":
                    handleProductDeleted(event);
                    break;
                default:
                    log.debug("Ignoring product event type: {}", event.getEventType());
            }

            acknowledgment.acknowledge();
            log.debug("Successfully processed product event: {} for product ID: {}", 
                    event.getEventType(), event.getProductId());

        } catch (Exception e) {
            log.error("Error processing product event: {} for product ID: {}", 
                    event.getEventType(), event.getProductId(), e);
            // Don't acknowledge - message will be retried
        }
    }

    private void handleStockUpdated(ProductEvent event) {
        log.info("Product stock updated - Product ID: {}, SKU: {}, Available: {}",
                event.getProductId(), event.getSku(), event.getStockAvailable());
        
        // Ici on pourrait implémenter la logique pour :
        // - Vérifier si des commandes en attente peuvent être traitées
        // - Notifier les clients de la disponibilité des produits
        // - Mettre à jour un cache local des stocks si nécessaire
    }

    private void handleProductUpdated(ProductEvent event) {
        log.info("Product updated - Product ID: {}, SKU: {}, Name: {}, Active: {}",
                event.getProductId(), event.getSku(), event.getName(), event.getActive());
        
        // Ici on pourrait implémenter la logique pour :
        // - Mettre à jour les informations produit dans les commandes en cours
        // - Gérer les produits désactivés dans les paniers
    }

    private void handleProductDeleted(ProductEvent event) {
        log.info("Product deleted - Product ID: {}, SKU: {}",
                event.getProductId(), event.getSku());
        
        // Ici on pourrait implémenter la logique pour :
        // - Supprimer le produit des paniers en cours
        // - Annuler les commandes non confirmées contenant ce produit
    }
}
