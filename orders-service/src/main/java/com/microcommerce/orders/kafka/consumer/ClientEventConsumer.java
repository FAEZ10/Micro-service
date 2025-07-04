package com.microcommerce.orders.kafka.consumer;

import com.microcommerce.orders.kafka.event.ClientEvent;
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
        groupId = "orders-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleClientEvent(
            @Payload ClientEvent event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received client event from topic: {}, partition: {}, key: {}", topic, partition, key);
            log.info("Client event details - Type: {}, ClientId: {}, Email: {}", 
                    event.getEventType(), event.getClientId(), event.getEmail());
            log.debug("Client event payload: {}", event);

            // Ici on pourrait implémenter la logique pour :
            // - Valider les informations client lors de la création de commande
            // - Appliquer des remises basées sur le statut client (VIP, nouveau client, etc.)
            // - Gérer les commandes en cas de suppression de compte client
            // - Mettre à jour les adresses de livraison disponibles
            // - Appliquer des règles de crédit client

            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.debug("Successfully processed client event from topic: {}", topic);

        } catch (Exception e) {
            log.error("Error processing client event from topic: {}", topic, e);
            // Don't acknowledge - message will be retried
        }
    }
}
