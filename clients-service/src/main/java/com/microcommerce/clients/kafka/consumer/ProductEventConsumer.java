package com.microcommerce.clients.kafka.consumer;

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
        groupId = "clients-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProductEvent(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received product event from topic: {}, partition: {}, key: {}", topic, partition, key);
            log.debug("Product event payload: {}", event);

            // Ici on pourrait implémenter la logique pour :
            // - Notifier les clients des nouveaux produits
            // - Envoyer des alertes de retour en stock pour les produits en wishlist
            // - Recommander des produits similaires aux clients
            // - Mettre à jour les préférences produit des clients
            // - Envoyer des promotions ciblées basées sur les intérêts

            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.debug("Successfully processed product event from topic: {}", topic);

        } catch (Exception e) {
            log.error("Error processing product event from topic: {}", topic, e);
            // Don't acknowledge - message will be retried
        }
    }
}
