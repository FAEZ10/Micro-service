package com.microcommerce.clients.kafka.producer;

import com.microcommerce.clients.kafka.event.ClientEvent;
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
public class ClientEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String CLIENT_EVENTS_TOPIC = "client-events";

    public void publishClientCreated(Long clientId, String email, String firstName, String lastName, String role) {
        ClientEvent event = ClientEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLIENT_CREATED")
                .clientId(clientId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .active(true)
                .timestamp(LocalDateTime.now())
                .source("clients-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishClientUpdated(Long clientId, String email, String firstName, String lastName, String role, Boolean active) {
        ClientEvent event = ClientEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLIENT_UPDATED")
                .clientId(clientId)
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .role(role)
                .active(active)
                .timestamp(LocalDateTime.now())
                .source("clients-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    public void publishClientDeleted(Long clientId, String email) {
        ClientEvent event = ClientEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType("CLIENT_DELETED")
                .clientId(clientId)
                .email(email)
                .active(false)
                .timestamp(LocalDateTime.now())
                .source("clients-service")
                .version("1.0")
                .build();

        publishEvent(event);
    }

    private void publishEvent(ClientEvent event) {
        try {
            log.info("Publishing client event: {} for client ID: {}", event.getEventType(), event.getClientId());
            
            CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(
                CLIENT_EVENTS_TOPIC, 
                event.getClientId().toString(), 
                event
            );
            
            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    log.info("Successfully published client event: {} for client ID: {} to partition: {} with offset: {}",
                            event.getEventType(), 
                            event.getClientId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                } else {
                    log.error("Failed to publish client event: {} for client ID: {}", 
                            event.getEventType(), 
                            event.getClientId(), 
                            ex);
                }
            });
            
        } catch (Exception e) {
            log.error("Error publishing client event: {} for client ID: {}", 
                    event.getEventType(), 
                    event.getClientId(), 
                    e);
        }
    }
}
