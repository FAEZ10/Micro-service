package com.microcommerce.products.kafka.consumer;

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
public class ClientEventConsumer {

    @KafkaListener(
        topics = "client-events",
        groupId = "products-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClientEvent(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received client event from topic: {}, partition: {}, key: {}", topic, partition, key);
            log.debug("Client event payload: {}", event);

            // Ici on pourrait implémenter la logique pour :
            // - Analyser les préférences client pour recommander des produits
            // - Créer des segments de clients pour des promotions ciblées
            // - Adapter l'affichage des produits selon le profil client
            // - Gérer les listes de souhaits et favoris
            // - Personnaliser les prix selon le statut client (VIP, professionnel, etc.)

            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.debug("Successfully processed client event from topic: {}", topic);

        } catch (Exception e) {
            log.error("Error processing client event from topic: {}", topic, e);
            // Don't acknowledge - message will be retried
        }
    }
}
