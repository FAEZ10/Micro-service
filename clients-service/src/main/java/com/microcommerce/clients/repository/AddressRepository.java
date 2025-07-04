package com.microcommerce.clients.repository;

import com.microcommerce.clients.entity.Address;
import com.microcommerce.clients.entity.Address.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    /**
     * Trouve toutes les adresses d'un client
     */
    List<Address> findByClientId(Long clientId);

    /**
     * Trouve les adresses d'un client par type
     */
    List<Address> findByClientIdAndType(Long clientId, AddressType type);

    /**
     * Trouve l'adresse principale d'un client par type
     */
    Optional<Address> findByClientIdAndTypeAndIsPrimaryTrue(Long clientId, AddressType type);

    /**
     * Trouve toutes les adresses principales d'un client
     */
    List<Address> findByClientIdAndIsPrimaryTrue(Long clientId);

    /**
     * Vérifie si un client a une adresse principale pour un type donné
     */
    boolean existsByClientIdAndTypeAndIsPrimaryTrue(Long clientId, AddressType type);

    /**
     * Compte le nombre d'adresses d'un client
     */
    long countByClientId(Long clientId);

    /**
     * Compte le nombre d'adresses d'un client par type
     */
    long countByClientIdAndType(Long clientId, AddressType type);

    /**
     * Supprime toutes les adresses d'un client
     */
    void deleteByClientId(Long clientId);

    /**
     * Met à jour toutes les adresses d'un type donné pour un client pour qu'elles ne soient plus principales
     */
    @Modifying
    @Query("UPDATE Address a SET a.isPrimary = false WHERE a.client.id = :clientId AND a.type = :type")
    void unsetPrimaryAddresses(@Param("clientId") Long clientId, @Param("type") AddressType type);

    /**
     * Trouve une adresse spécifique d'un client
     */
    Optional<Address> findByIdAndClientId(Long addressId, Long clientId);

    /**
     * Vérifie si une adresse appartient à un client
     */
    boolean existsByIdAndClientId(Long addressId, Long clientId);
}
