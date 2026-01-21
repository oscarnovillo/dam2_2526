package org.example.springcrypto.repository;

import org.example.springcrypto.entity.SharedSecret;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SharedSecretRepository extends JpaRepository<SharedSecret, Long> {

    /**
     * Encuentra todos los secretos compartidos CON un usuario
     */
    List<SharedSecret> findBySharedWithId(Long userId);

    /**
     * Encuentra todos los secretos que un usuario ha compartido
     */
    List<SharedSecret> findByOwnerId(Long ownerId);

    /**
     * Verifica si un secreto está compartido con un usuario específico
     */
    Optional<SharedSecret> findBySecretIdAndSharedWithId(Long secretId, Long userId);

    /**
     * Encuentra todas las personas con quien se ha compartido un secreto
     */
    List<SharedSecret> findBySecretId(Long secretId);

    /**
     * Elimina un compartido específico (revocar acceso)
     */
    void deleteBySecretIdAndSharedWithId(Long secretId, Long userId);
}

