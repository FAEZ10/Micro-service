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
public class OrderEventConsumer {

    @KafkaListener(
        topics = "order-events",
        groupId = "clients-service-group",
        containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleOrderEvent(
            @Payload Object event,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.RECEIVED_KEY) Object key,
            Acknowledgment acknowledgment) {
        
        try {
            log.info("Received order event from topic: {}, partition: {}, key: {}", topic, partition, key);
            log.debug("Order event payload: {}", event);

            // Ici on pourrait implémenter la logique pour :
            // - Envoyer des notifications de confirmation de commande
            // - Mettre à jour l'historique d'achat du client
            // - Calculer des points de fidélité
            // - Envoyer des emails de suivi de commande
            // - Mettre à jour les préférences client basées sur les achats

            // Acknowledge successful processing
            acknowledgment.acknowledge();
            log.debug("Successfully processed order event from topic: {}", topic);

        } catch (Exception e) {
            log.error("Error processing order event from topic: {}", topic, e);
            // Don't acknowledge - message will be retried
        }
    }
}
