package com.microcommerce.products.kafka.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientEvent {
    
    private String eventId;
    private String eventType; // CLIENT_CREATED, CLIENT_UPDATED, CLIENT_DELETED
    private Long clientId;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private Boolean active;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
    
    // Métadonnées de l'événement
    private String source;
    private String version;
}
