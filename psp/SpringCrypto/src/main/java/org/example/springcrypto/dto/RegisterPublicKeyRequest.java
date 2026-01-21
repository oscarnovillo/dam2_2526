package org.example.springcrypto.dto;

public record RegisterPublicKeyRequest(
    String publicKey,  // Base64 encoded public key (X.509 format)
    String algorithm,  // "RSA" o "EC"
    Integer keySize    // 2048, 4096 para RSA; 256, 384 para EC
) {
}

