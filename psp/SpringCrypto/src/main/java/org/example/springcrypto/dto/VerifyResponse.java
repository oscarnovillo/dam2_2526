package org.example.springcrypto.dto;

/**
 * Response para verificación de firma
 */
public record VerifyResponse(
        boolean valid
) {}

