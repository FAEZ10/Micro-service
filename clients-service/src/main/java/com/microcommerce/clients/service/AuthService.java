package com.microcommerce.clients.service;

import com.microcommerce.clients.dto.request.LoginRequest;
import com.microcommerce.clients.dto.request.RegisterRequest;
import com.microcommerce.clients.dto.response.AuthResponse;
import com.microcommerce.clients.dto.response.ClientResponse;
import com.microcommerce.clients.entity.Client;
import com.microcommerce.clients.exception.InvalidCredentialsException;
import com.microcommerce.clients.kafka.producer.ClientEventProducer;
import com.microcommerce.clients.security.JwtTokenProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private ClientService clientService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private ClientEventProducer clientEventProducer;

 
    public AuthResponse register(RegisterRequest request) {
        logger.info("Registering new client with email: {}", request.getEmail());

        ClientResponse clientResponse = clientService.createClient(request);

        try {
            clientEventProducer.publishClientCreated(
                clientResponse.getId(),
                clientResponse.getEmail(),
                clientResponse.getFirstName(),
                clientResponse.getLastName(),
                clientResponse.getRole()
            );
        } catch (Exception e) {
            logger.warn("Failed to publish client created event for client: {}", clientResponse.getEmail(), e);
        }

        String accessToken = jwtTokenProvider.generateAccessToken(clientResponse.getEmail(), clientResponse.getRole(), clientResponse.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(clientResponse.getEmail());
        Long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        logger.info("Client registered and authenticated successfully: {}", clientResponse.getEmail());

        return new AuthResponse(accessToken, refreshToken, expiresIn, clientResponse);
    }

    public AuthResponse login(LoginRequest request) {
        logger.info("Login attempt for email: {}", request.getEmail());

        Client client = clientService.findActiveByEmail(request.getEmail())
                .orElseThrow(() -> {
                    logger.warn("Login failed - client not found or inactive: {}", request.getEmail());
                    return new InvalidCredentialsException();
                });

        if (!passwordEncoder.matches(request.getPassword(), client.getPassword())) {
            logger.warn("Login failed - invalid password for email: {}", request.getEmail());
            throw new InvalidCredentialsException();
        }

        String accessToken = jwtTokenProvider.generateAccessToken(client.getEmail(), client.getRole().name(), client.getId());
        String refreshToken = jwtTokenProvider.generateRefreshToken(client.getEmail());
        Long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        ClientResponse clientResponse = mapToClientResponse(client);

        logger.info("Client authenticated successfully: {}", client.getEmail());

        return new AuthResponse(accessToken, refreshToken, expiresIn, clientResponse);
    }

    /**
     * Rafraîchir le token d'accès
     */
    public AuthResponse refreshToken(String refreshToken) {
        logger.info("Refreshing access token");

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            logger.warn("Invalid refresh token provided");
            throw new InvalidCredentialsException("Token de rafraîchissement invalide");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);

        Client client = clientService.findActiveByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("Refresh token failed - client not found or inactive: {}", email);
                    return new InvalidCredentialsException("Client introuvable ou inactif");
                });

        String newAccessToken = jwtTokenProvider.generateAccessToken(client.getEmail(), client.getRole().name(), client.getId());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(client.getEmail());
        Long expiresIn = jwtTokenProvider.getAccessTokenExpiration();

        ClientResponse clientResponse = mapToClientResponse(client);

        logger.info("Token refreshed successfully for client: {}", client.getEmail());

        return new AuthResponse(newAccessToken, newRefreshToken, expiresIn, clientResponse);
    }

    @Transactional(readOnly = true)
    public boolean validateAccessToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Transactional(readOnly = true)
    public String getEmailFromToken(String token) {
        return jwtTokenProvider.getEmailFromToken(token);
    }

    @Transactional(readOnly = true)
    public String getRoleFromToken(String token) {
        return jwtTokenProvider.getRoleFromToken(token);
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
}
