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

    @Lob
    @Column(name = "server_signature")
    private byte[] serverSignature; // Firma del servidor sobre la clave pública

    @Column(name = "signature_algorithm")
    private String signatureAlgorithm; // "SHA256withRSA" o "SHA256withECDSA"

    @Column(name = "signed_at")
    private LocalDateTime signedAt; // Cuándo se firmó (para verificación)

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

    public byte[] getServerSignature() {
        return serverSignature;
    }

    public void setServerSignature(byte[] serverSignature) {
        this.serverSignature = serverSignature;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public void setSignedAt(LocalDateTime signedAt) {
        this.signedAt = signedAt;
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

