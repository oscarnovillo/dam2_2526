package org.example.springcrypto.entity;
}
    }
        this.updatedAt = updatedAt;
    public void setUpdatedAt(LocalDateTime updatedAt) {

    }
        return updatedAt;
    public LocalDateTime getUpdatedAt() {

    }
        this.createdAt = createdAt;
    public void setCreatedAt(LocalDateTime createdAt) {

    }
        return createdAt;
    public LocalDateTime getCreatedAt() {

    }
        this.metadata = metadata;
    public void setMetadata(String metadata) {

    }
        return metadata;
    public String getMetadata() {

    }
        this.salt = salt;
    public void setSalt(byte[] salt) {

    }
        return salt;
    public byte[] getSalt() {

    }
        this.iv = iv;
    public void setIv(byte[] iv) {

    }
        return iv;
    public byte[] getIv() {

    }
        this.encryptedData = encryptedData;
    public void setEncryptedData(byte[] encryptedData) {

    }
        return encryptedData;
    public byte[] getEncryptedData() {

    }
        this.userId = userId;
    public void setUserId(Long userId) {

    }
        return userId;
    public Long getUserId() {

    }
        this.id = id;
    public void setId(Long id) {

    }
        return id;
    public Long getId() {

    // Getters y Setters

    }
        updatedAt = LocalDateTime.now();
    protected void onUpdate() {
    @PreUpdate

    }
        updatedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    protected void onCreate() {
    @PrePersist

    private LocalDateTime updatedAt;
    @Column(name = "updated_at")

    private LocalDateTime createdAt;
    @Column(name = "created_at", nullable = false, updatable = false)

    private String metadata; // JSON con título, tags, etc. (también cifrado)
    @Column(name = "metadata")

    private byte[] salt;
    @Column(name = "salt", nullable = false)

    private byte[] iv;
    @Column(name = "iv", nullable = false)

    private byte[] encryptedData;
    @Column(name = "encrypted_data", nullable = false)
    @Lob

    private Long userId;
    @Column(name = "user_id", nullable = false)

    private Long id;
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id

public class VaultSecret {
@Table(name = "vault_secrets")
@Entity

import java.time.LocalDateTime;
import jakarta.persistence.*;


