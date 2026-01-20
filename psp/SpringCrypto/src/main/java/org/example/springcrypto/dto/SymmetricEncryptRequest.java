package org.example.springcrypto.dto;

/**
 * Request para operaciones de encriptación simétrica
 */
public record SymmetricEncryptRequest(
        String plainText,
        String mode,  // ECB, CBC, CTR, GCM
        String key    // Base64 encoded key (opcional, se genera si no se proporciona)
) {}

