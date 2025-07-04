package com.microcommerce.clients.service;

import com.microcommerce.clients.dto.request.RegisterRequest;
import com.microcommerce.clients.dto.response.ClientResponse;
import com.microcommerce.clients.entity.Client;
import com.microcommerce.clients.exception.ClientNotFoundException;
import com.microcommerce.clients.exception.EmailAlreadyExistsException;
import com.microcommerce.clients.repository.ClientRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class ClientService {

    private static final Logger logger = LoggerFactory.getLogger(ClientService.class);

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Créer un nouveau client
     */
    public ClientResponse createClient(RegisterRequest request) {
        logger.info("Creating new client with email: {}", request.getEmail());

        // Vérifier si l'email existe déjà
        if (clientRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }

        // Vérifier que les mots de passe correspondent
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        Client client = new Client();
        client.setEmail(request.getEmail());
        client.setPassword(passwordEncoder.encode(request.getPassword()));
        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhone(request.getPhone());
        client.setRole(Client.Role.CLIENT);
        client.setActive(true);

        Client savedClient = clientRepository.save(client);
        logger.info("Client created successfully with ID: {}", savedClient.getId());

        return mapToClientResponse(savedClient);
    }

    @Transactional(readOnly = true)
    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<Client> findActiveByEmail(String email) {
        return clientRepository.findByEmailAndActiveTrue(email);
    }

    @Transactional(readOnly = true)
    public ClientResponse findById(Long id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException(id));
        
        return mapToClientResponse(client);
    }

    public ClientResponse updateProfile(Long clientId, RegisterRequest request) {
        logger.info("Updating profile for client ID: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (!client.getEmail().equals(request.getEmail())) {
            if (clientRepository.existsByEmail(request.getEmail())) {
                throw new EmailAlreadyExistsException(request.getEmail());
            }
            client.setEmail(request.getEmail());
        }

        client.setFirstName(request.getFirstName());
        client.setLastName(request.getLastName());
        client.setPhone(request.getPhone());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            if (!request.isPasswordMatching()) {
                throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
            }
            client.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        Client updatedClient = clientRepository.save(client);
        logger.info("Profile updated successfully for client ID: {}", clientId);

        return mapToClientResponse(updatedClient);
    }

    public ClientResponse toggleClientStatus(Long clientId) {
        logger.info("Toggling status for client ID: {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        client.setActive(!client.getActive());
        Client updatedClient = clientRepository.save(client);

        logger.info("Client ID: {} status changed to: {}", clientId, updatedClient.getActive());
        return mapToClientResponse(updatedClient);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> searchClients(String search, Pageable pageable) {
        Page<Client> clients;
        
        if (search != null && !search.trim().isEmpty()) {
            clients = clientRepository.searchClients(search.trim(), pageable);
        } else {
            clients = clientRepository.findAll(pageable);
        }

        return clients.map(this::mapToClientResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> findActiveClients(Pageable pageable) {
        Page<Client> clients = clientRepository.findByActiveTrue(pageable);
        return clients.map(this::mapToClientResponse);
    }

    @Transactional(readOnly = true)
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        logger.info("Retrieving all clients - Page: {}, Size: {}", 
                pageable.getPageNumber(), pageable.getPageSize());
        
        Page<Client> clients = clientRepository.findAll(pageable);
        return clients.map(this::mapToClientResponse);
    }

    public ClientResponse promoteToAdmin(Long clientId) {
        logger.info("Promoting client {} to admin", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (client.getRole() == Client.Role.ADMIN) {
            logger.warn("Client {} is already an admin", clientId);
            throw new IllegalStateException("Le client est déjà administrateur");
        }

        client.setRole(Client.Role.ADMIN);
        Client updatedClient = clientRepository.save(client);

        logger.info("Client {} promoted to admin successfully", clientId);
        return mapToClientResponse(updatedClient);
    }

    public ClientResponse demoteFromAdmin(Long clientId) {
        logger.info("Demoting admin {} to client", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (client.getRole() == Client.Role.CLIENT) {
            logger.warn("Client {} is already a regular client", clientId);
            throw new IllegalStateException("Le client n'est pas administrateur");
        }

        client.setRole(Client.Role.CLIENT);
        Client updatedClient = clientRepository.save(client);

        logger.info("Admin {} demoted to client successfully", clientId);
        return mapToClientResponse(updatedClient);
    }

    public ClientResponse deactivateClient(Long clientId) {
        logger.info("Deactivating client {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (!client.getActive()) {
            logger.warn("Client {} is already inactive", clientId);
            throw new IllegalStateException("Le client est déjà désactivé");
        }

        client.setActive(false);
        Client updatedClient = clientRepository.save(client);

        logger.info("Client {} deactivated successfully", clientId);
        return mapToClientResponse(updatedClient);
    }

    public ClientResponse activateClient(Long clientId) {
        logger.info("Activating client {}", clientId);

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException(clientId));

        if (client.getActive()) {
            logger.warn("Client {} is already active", clientId);
            throw new IllegalStateException("Le client est déjà actif");
        }

        client.setActive(true);
        Client updatedClient = clientRepository.save(client);

        logger.info("Client {} activated successfully", clientId);
        return mapToClientResponse(updatedClient);
    }

    @Transactional(readOnly = true)
    public ClientStats getClientStats() {
        long totalClients = clientRepository.count();
        long activeClients = clientRepository.countByActiveTrue();
        long adminClients = clientRepository.countByRole(Client.Role.ADMIN);
        long regularClients = clientRepository.countByRole(Client.Role.CLIENT);

        return new ClientStats(totalClients, activeClients, adminClients, regularClients);
    }

    private ClientResponse mapToClientResponse(Client client) {
        ClientResponse response = new ClientResponse();
        response.setId(client.getId());
        response.setEmail(client.getEmail());
        response.setFirstName(client.getFirstName());
        response.setLastName(client.getLastName());
        response.setPhone(client.getPhone());
        response.setRole(client.getRole().name());
        response.setActive(client.getActive());
        response.setCreatedAt(client.getCreatedAt());
        response.setUpdatedAt(client.getUpdatedAt());
        return response;
    }

    public static class ClientStats {
        private final long totalClients;
        private final long activeClients;
        private final long adminClients;
        private final long regularClients;

        public ClientStats(long totalClients, long activeClients, long adminClients, long regularClients) {
            this.totalClients = totalClients;
            this.activeClients = activeClients;
            this.adminClients = adminClients;
            this.regularClients = regularClients;
        }

        public long getTotalClients() { return totalClients; }
        public long getActiveClients() { return activeClients; }
        public long getAdminClients() { return adminClients; }
        public long getRegularClients() { return regularClients; }
    }
}
