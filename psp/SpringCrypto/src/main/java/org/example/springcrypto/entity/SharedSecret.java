package org.example.springcrypto.entity;
}
    }
        this.expiresAt = expiresAt;
    public void setExpiresAt(LocalDateTime expiresAt) {

    }
        return expiresAt;
    public LocalDateTime getExpiresAt() {

    }
        this.createdAt = createdAt;
    public void setCreatedAt(LocalDateTime createdAt) {

    }
        return createdAt;
    public LocalDateTime getCreatedAt() {

    }
        this.permission = permission;
    public void setPermission(String permission) {

    }
        return permission;
    public String getPermission() {

    }
        this.algorithm = algorithm;
    public void setAlgorithm(String algorithm) {

    }
        return algorithm;
    public String getAlgorithm() {

    }
        this.encryptedSecretKey = encryptedSecretKey;
    public void setEncryptedSecretKey(byte[] encryptedSecretKey) {

    }
        return encryptedSecretKey;
    public byte[] getEncryptedSecretKey() {

    }
        this.sharedWithId = sharedWithId;
    public void setSharedWithId(Long sharedWithId) {

    }
        return sharedWithId;
    public Long getSharedWithId() {

    }
        this.ownerId = ownerId;
    public void setOwnerId(Long ownerId) {

    }
        return ownerId;
    public Long getOwnerId() {

    }
        this.secretId = secretId;
    public void setSecretId(Long secretId) {

    }
        return secretId;
    public Long getSecretId() {

    }
        this.id = id;
    public void setId(Long id) {

    }
        return id;
    public Long getId() {

    // Getters y Setters

    }
        createdAt = LocalDateTime.now();
    protected void onCreate() {
    @PrePersist

    private LocalDateTime expiresAt; // Opcional: compartir por tiempo limitado
    @Column(name = "expires_at")

    private LocalDateTime createdAt;
    @Column(name = "created_at", nullable = false, updatable = false)

    private String permission; // "READ" o "READ_WRITE"
    @Column(name = "permission", nullable = false)

    private String algorithm; // "RSA" o "EC"
    @Column(name = "algorithm", nullable = false)

    private byte[] encryptedSecretKey; // Secreto cifrado con publicKey del receptor
    @Column(name = "encrypted_secret_key", nullable = false)
    @Lob

    private Long sharedWithId; // Usuario con quien se comparte
    @Column(name = "shared_with_id", nullable = false)

    private Long ownerId; // Usuario que comparte
    @Column(name = "owner_id", nullable = false)

    private Long secretId; // Referencia al secreto original
    @Column(name = "secret_id", nullable = false)

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class SharedSecret {
@Table(name = "shared_secrets")
@Entity
 */
 * 3. Usuario B descifra con su privateKey
 *    - Se guarda en esta tabla
 *    - A cifra con publicKey de B (RSA/EC)
 *    - A obtiene publicKey de B
 *    - A descifra con su password
 * 2. Usuario A comparte con Usuario B:
 * 1. Usuario A (owner) cifra secreto con su password (AES)
 * Flujo:
 *
 * Representa un secreto compartido entre usuarios
/**

import java.time.LocalDateTime;
import jakarta.persistence.*;


