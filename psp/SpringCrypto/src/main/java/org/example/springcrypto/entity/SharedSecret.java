package org.example.springcrypto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Representa un secreto compartido entre usuarios
 *
 * Flujo:
 * 1. Usuario A (owner) cifra secreto con su password (AES)
 * 2. Usuario A comparte con Usuario B:
 *    - A descifra con su password
 *    - A obtiene publicKey de B
 *    - A cifra con publicKey de B (RSA/EC)
 *    - Se guarda en esta tabla
 * 3. Usuario B descifra con su privateKey
 */
@Entity
@Table(name = "shared_secrets")
public class SharedSecret {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "secret_id", nullable = false)
    private Long secretId; // Referencia al secreto original

    @Column(name = "owner_id", nullable = false)
    private Long ownerId; // Usuario que comparte

    @Column(name = "shared_with_id", nullable = false)
    private Long sharedWithId; // Usuario con quien se comparte

    @Lob
    @Column(name = "encrypted_secret_key", nullable = false)
    private byte[] encryptedSecretKey; // Secreto cifrado con publicKey del receptor

    @Column(name = "algorithm", nullable = false)
    private String algorithm; // "RSA" o "EC"

    @Column(name = "permission", nullable = false)
    private String permission; // "READ" o "READ_WRITE"

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt; // Opcional: compartir por tiempo limitado

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSecretId() {
        return secretId;
    }

    public void setSecretId(Long secretId) {
        this.secretId = secretId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public Long getSharedWithId() {
        return sharedWithId;
    }

    public void setSharedWithId(Long sharedWithId) {
        this.sharedWithId = sharedWithId;
    }

    public byte[] getEncryptedSecretKey() {
        return encryptedSecretKey;
    }

    public void setEncryptedSecretKey(byte[] encryptedSecretKey) {
        this.encryptedSecretKey = encryptedSecretKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
}
