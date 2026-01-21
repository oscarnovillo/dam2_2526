package org.example.springcrypto.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Almacena las claves públicas de los usuarios para compartir secretos
 *
 * Cada usuario tiene un par de claves RSA/EC:
 * - Clave pública: Guardada aquí (para que otros cifren)
 * - Clave privada: Solo el usuario la tiene (en su dispositivo/keystore)
 */
@Entity
@Table(name = "user_public_keys")
public class UserPublicKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Lob
    @Column(name = "public_key", nullable = false)
    private byte[] publicKey; // Clave pública codificada (X.509 format)

    @Column(name = "algorithm", nullable = false)
    private String algorithm; // "RSA" o "EC"

    @Column(name = "key_size")
    private Integer keySize; // 2048, 4096 para RSA; 256, 384 para EC

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters y Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Integer getKeySize() {
        return keySize;
    }

    public void setKeySize(Integer keySize) {
        this.keySize = keySize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}

