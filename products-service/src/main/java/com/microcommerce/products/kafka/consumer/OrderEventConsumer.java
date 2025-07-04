package com.microcommerce.products.kafka.consumer;

import com.microcommerce.products.kafka.event.OrderEvent;
import com.microcommerce.products.service.ProductService;
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
public class OrderEventConsumer {

    private final ProductService productService;

    @KafkaListener(
        topics = "order-events",
        groupId = "products-service-group",
        containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
            @Payload OrderEvent orderEvent,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received order event from topic: {}, partition: {}, key: {}", topic, partition, key);
            log.info("Order event details - Type: {}, OrderId: {}, Items count: {}", 
                    orderEvent.getEventType(), orderEvent.getOrderId(), 
                    orderEvent.getItems() != null ? orderEvent.getItems().size() : 0);
            log.debug("Order event payload: {}", orderEvent);

            processOrderEvent(orderEvent);

            acknowledgment.acknowledge();
            log.info("Successfully processed order event: {} for order ID: {}", 
                    orderEvent.getEventType(), orderEvent.getOrderId());

        } catch (Exception e) {
            log.error("Error processing order event from topic: {}, event: {}", topic, orderEvent, e);
        }
    }

    private void processOrderEvent(OrderEvent orderEvent) {
        if (orderEvent.getItems() == null || orderEvent.getItems().isEmpty()) {
            log.warn("Événement de commande sans articles: {}", orderEvent.getEventType());
            return;
        }

        switch (orderEvent.getEventType()) {
            case "ORDER_CONFIRMED":
                log.info("Traitement de la confirmation de commande ID: {}", orderEvent.getOrderId());
                productService.processOrderStockReduction(orderEvent.getItems(), orderEvent.getOrderId());
                break;
                
            case "ORDER_CANCELLED":
                log.info("Traitement de l'annulation de commande ID: {}", orderEvent.getOrderId());
                productService.restoreOrderStock(orderEvent.getItems(), orderEvent.getOrderId());
                break;
                
            case "ORDER_CREATED":
                log.debug("Commande créée ID: {} - Aucune action sur le stock nécessaire", orderEvent.getOrderId());
                // Pas d'action sur le stock lors de la création
                break;
                
            case "ORDER_UPDATED":
                log.debug("Commande mise à jour ID: {} - Traitement spécifique requis", orderEvent.getOrderId());
                // TODO: Implémenter la logique pour les mises à jour de commande
                // Cela pourrait nécessiter de comparer l'ancien et le nouveau contenu
                break;
                
            case "ITEM_ADDED":
                log.debug("Article ajouté à la commande ID: {}", orderEvent.getOrderId());
                // Pas d'action sur le stock tant que la commande n'est pas confirmée
                break;
                
            case "ITEM_REMOVED":
                log.debug("Article retiré de la commande ID: {}", orderEvent.getOrderId());
                // Pas d'action sur le stock tant que la commande n'est pas confirmée
                break;
                
            default:
                log.warn("Type d'événement de commande non géré: {} pour commande ID: {}", 
                        orderEvent.getEventType(), orderEvent.getOrderId());
        }
    }
}
