package com.microcommerce.clients.repository;

import com.microcommerce.clients.entity.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {

    /**
     * Trouve un client par son email
     */
    Optional<Client> findByEmail(String email);

    /**
     * Vérifie si un email existe déjà
     */
    boolean existsByEmail(String email);

    /**
     * Trouve tous les clients actifs
     */
    Page<Client> findByActiveTrue(Pageable pageable);

    /**
     * Trouve tous les clients inactifs
     */
    Page<Client> findByActiveFalse(Pageable pageable);

    /**
     * Trouve les clients par rôle
     */
    Page<Client> findByRole(Client.Role role, Pageable pageable);

    /**
     * Recherche de clients par nom, prénom ou email (insensible à la casse)
     */
    @Query("SELECT c FROM Client c WHERE " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Client> searchClients(@Param("search") String search, Pageable pageable);

    /**
     * Recherche de clients actifs par nom, prénom ou email
     */
    @Query("SELECT c FROM Client c WHERE c.active = true AND (" +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Client> searchActiveClients(@Param("search") String search, Pageable pageable);

    /**
     * Compte le nombre de clients actifs
     */
    long countByActiveTrue();

    /**
     * Compte le nombre de clients par rôle
     */
    long countByRole(Client.Role role);

    /**
     * Trouve un client actif par email
     */
    Optional<Client> findByEmailAndActiveTrue(String email);
}
