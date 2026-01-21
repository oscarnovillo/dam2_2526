package org.example.springcrypto.service;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.entity.SharedSecret;
import org.example.springcrypto.entity.UserPublicKey;
import org.example.springcrypto.entity.VaultSecret;
import org.example.springcrypto.repository.SharedSecretRepository;
import org.example.springcrypto.repository.UserPublicKeyRepository;
import org.example.springcrypto.repository.VaultSecretRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SharingService {

    private final SharedSecretRepository sharedSecretRepository;
    private final UserPublicKeyRepository userPublicKeyRepository;
    private final VaultSecretRepository vaultSecretRepository;

    public SharingService(
        SharedSecretRepository sharedSecretRepository,
        UserPublicKeyRepository userPublicKeyRepository,
        VaultSecretRepository vaultSecretRepository
    ) {
        this.sharedSecretRepository = sharedSecretRepository;
        this.userPublicKeyRepository = userPublicKeyRepository;
        this.vaultSecretRepository = vaultSecretRepository;
    }

    /**
     * Registra la clave pública de un usuario
     */
    @Transactional
    public void registerPublicKey(Long userId, RegisterPublicKeyRequest request) {
        UserPublicKey userPublicKey = userPublicKeyRepository.findByUserId(userId)
            .orElse(new UserPublicKey());

        userPublicKey.setUserId(userId);
        userPublicKey.setPublicKey(Base64.getDecoder().decode(request.publicKey()));
        userPublicKey.setAlgorithm(request.algorithm());
        userPublicKey.setKeySize(request.keySize());

        userPublicKeyRepository.save(userPublicKey);
    }

    /**
     * Obtiene la clave pública de un usuario
     */
    public UserPublicKeyResponse getUserPublicKey(Long userId) {
        UserPublicKey key = userPublicKeyRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no tiene clave pública registrada"));

        return new UserPublicKeyResponse(
            key.getUserId(),
            Base64.getEncoder().encodeToString(key.getPublicKey()),
            key.getAlgorithm(),
            key.getKeySize()
        );
    }

    /**
     * Comparte un secreto con otro usuario
     *
     * El cliente debe:
     * 1. Descifrar el secreto con su password (AES)
     * 2. Obtener la clave pública del receptor
     * 3. Cifrar el secreto con la clave pública del receptor (RSA/EC)
     * 4. Enviar los datos cifrados al servidor
     */
    @Transactional
    public ShareSecretResponse shareSecret(Long ownerId, ShareSecretRequest request) {
        // Verificar que el secreto existe y pertenece al owner
        VaultSecret secret = vaultSecretRepository.findByIdAndUserId(request.secretId(), ownerId)
            .orElseThrow(() -> new RuntimeException("Secreto no encontrado o sin permisos"));

        // Verificar que el receptor tiene clave pública registrada
        if (!userPublicKeyRepository.existsByUserId(request.sharedWithUserId())) {
            throw new RuntimeException("El usuario receptor no tiene clave pública registrada");
        }

        // Verificar que no esté ya compartido
        if (sharedSecretRepository.findBySecretIdAndSharedWithId(
                request.secretId(), request.sharedWithUserId()).isPresent()) {
            throw new RuntimeException("El secreto ya está compartido con este usuario");
        }

        // Crear compartido
        SharedSecret shared = new SharedSecret();
        shared.setSecretId(request.secretId());
        shared.setOwnerId(ownerId);
        shared.setSharedWithId(request.sharedWithUserId());
        shared.setEncryptedSecretKey(Base64.getDecoder().decode(request.encryptedData()));
        shared.setAlgorithm(request.algorithm());
        shared.setPermission(request.permission());

        if (request.expiresInDays() != null) {
            shared.setExpiresAt(LocalDateTime.now().plusDays(request.expiresInDays()));
        }

        shared = sharedSecretRepository.save(shared);

        return new ShareSecretResponse(
            shared.getId(),
            shared.getSecretId(),
            shared.getSharedWithId(),
            shared.getPermission(),
            shared.getCreatedAt(),
            shared.getExpiresAt()
        );
    }

    /**
     * Lista los secretos compartidos CON el usuario actual
     */
    public List<SharedSecretItem> getSecretsSharedWithMe(Long userId) {
        return sharedSecretRepository.findBySharedWithId(userId)
            .stream()
            .map(this::toSharedSecretItem)
            .collect(Collectors.toList());
    }

    /**
     * Lista los secretos que el usuario ha compartido con otros
     */
    public List<SharedSecretItem> getSecretsSharedByMe(Long userId) {
        return sharedSecretRepository.findByOwnerId(userId)
            .stream()
            .map(this::toSharedSecretItem)
            .collect(Collectors.toList());
    }

    /**
     * Obtiene un secreto compartido (para descifrarlo)
     */
    public SharedSecretItem getSharedSecret(Long userId, Long shareId) {
        SharedSecret shared = sharedSecretRepository.findById(shareId)
            .orElseThrow(() -> new RuntimeException("Secreto compartido no encontrado"));

        // Verificar que el usuario tiene acceso
        if (!shared.getSharedWithId().equals(userId) && !shared.getOwnerId().equals(userId)) {
            throw new RuntimeException("Sin permisos para acceder a este secreto");
        }

        // Verificar expiración
        if (shared.getExpiresAt() != null && LocalDateTime.now().isAfter(shared.getExpiresAt())) {
            throw new RuntimeException("El acceso a este secreto ha expirado");
        }

        return toSharedSecretItem(shared);
    }

    /**
     * Revoca el acceso a un secreto compartido
     */
    @Transactional
    public void revokeAccess(Long ownerId, Long secretId, Long sharedWithUserId) {
        SharedSecret shared = sharedSecretRepository
            .findBySecretIdAndSharedWithId(secretId, sharedWithUserId)
            .orElseThrow(() -> new RuntimeException("Secreto compartido no encontrado"));

        // Verificar que es el owner
        if (!shared.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Solo el propietario puede revocar acceso");
        }

        sharedSecretRepository.delete(shared);
    }

    /**
     * Lista con quién se ha compartido un secreto específico
     */
    public List<SharedSecretItem> getSecretShares(Long ownerId, Long secretId) {
        // Verificar que el secreto pertenece al owner
        vaultSecretRepository.findByIdAndUserId(secretId, ownerId)
            .orElseThrow(() -> new RuntimeException("Secreto no encontrado"));

        return sharedSecretRepository.findBySecretId(secretId)
            .stream()
            .map(this::toSharedSecretItem)
            .collect(Collectors.toList());
    }

    private SharedSecretItem toSharedSecretItem(SharedSecret shared) {
        boolean isExpired = shared.getExpiresAt() != null &&
                           LocalDateTime.now().isAfter(shared.getExpiresAt());

        return new SharedSecretItem(
            shared.getId(),
            shared.getSecretId(),
            shared.getOwnerId(),
            shared.getSharedWithId(),
            Base64.getEncoder().encodeToString(shared.getEncryptedSecretKey()),
            shared.getPermission(),
            shared.getAlgorithm(),
            shared.getCreatedAt(),
            shared.getExpiresAt(),
            isExpired
        );
    }
}

