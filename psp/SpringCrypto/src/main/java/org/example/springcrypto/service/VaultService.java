package org.example.springcrypto.service;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.entity.VaultSecret;
import org.example.springcrypto.repository.VaultSecretRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VaultService {

    private final VaultSecretRepository repository;

    public VaultService(VaultSecretRepository repository) {
        this.repository = repository;
    }

    /**
     * Guarda un secreto CIFRADO (el servidor NO descifra)
     */
    @Transactional
    public SaveSecretResponse saveSecret(Long userId, SaveSecretRequest request) {
        VaultSecret secret = new VaultSecret();
        secret.setUserId(userId);

        // Los datos YA vienen cifrados desde el cliente
        secret.setEncryptedData(Base64.getDecoder().decode(request.encryptedData()));
        secret.setIv(Base64.getDecoder().decode(request.iv()));
        secret.setSalt(Base64.getDecoder().decode(request.salt()));
        secret.setMetadata(request.metadata());

        secret = repository.save(secret);

        return new SaveSecretResponse(secret.getId(), secret.getCreatedAt());
    }

    /**
     * Obtiene un secreto cifrado (el servidor NO descifra)
     */
    public SecretDetailResponse getSecret(Long userId, Long secretId) {
        VaultSecret secret = repository.findByIdAndUserId(secretId, userId)
            .orElseThrow(() -> new RuntimeException("Secret not found or access denied"));

        return new SecretDetailResponse(
            secret.getId(),
            Base64.getEncoder().encodeToString(secret.getEncryptedData()),
            Base64.getEncoder().encodeToString(secret.getIv()),
            Base64.getEncoder().encodeToString(secret.getSalt()),
            secret.getMetadata()
        );
    }

    /**
     * Lista todos los secretos del usuario (solo metadatos cifrados)
     */
    public List<SecretListItem> listSecrets(Long userId) {
        return repository.findByUserId(userId)
            .stream()
            .map(s -> new SecretListItem(
                s.getId(),
                s.getMetadata(),
                s.getCreatedAt(),
                s.getUpdatedAt()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Elimina un secreto
     */
    @Transactional
    public void deleteSecret(Long userId, Long secretId) {
        VaultSecret secret = repository.findByIdAndUserId(secretId, userId)
            .orElseThrow(() -> new RuntimeException("Secret not found or access denied"));

        repository.delete(secret);
    }

    /**
     * Actualiza un secreto (re-cifrado con nueva password, por ejemplo)
     */
    @Transactional
    public void updateSecret(Long userId, Long secretId, SaveSecretRequest request) {
        VaultSecret secret = repository.findByIdAndUserId(secretId, userId)
            .orElseThrow(() -> new RuntimeException("Secret not found or access denied"));

        secret.setEncryptedData(Base64.getDecoder().decode(request.encryptedData()));
        secret.setIv(Base64.getDecoder().decode(request.iv()));
        secret.setSalt(Base64.getDecoder().decode(request.salt()));
        secret.setMetadata(request.metadata());

        repository.save(secret);
    }
}

