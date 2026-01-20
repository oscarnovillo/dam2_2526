package org.example.springcrypto.dto;

/**
 * Response con par de claves RSA
 */
public record KeyPairResponse(
        String publicKey,     // Base64 encoded
        String privateKey     // Base64 encoded
) {}

