package org.example.springcrypto.dto;

/**
 * Response para firma digital
 */
public record SignResponse(
        String signature      // Base64 encoded
) {}

