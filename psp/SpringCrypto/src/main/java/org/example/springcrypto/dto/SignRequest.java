package org.example.springcrypto.dto;

/**
 * Request para firma digital
 */
public record SignRequest(
        String message,
        String privateKey     // Base64 encoded
) {}

