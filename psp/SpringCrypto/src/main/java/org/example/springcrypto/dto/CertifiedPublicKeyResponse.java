package org.example.springcrypto.dto;

import java.time.LocalDateTime;

/**
 * Respuesta con clave pública CERTIFICADA (firmada por el servidor)
 *
 * La firma permite a los clientes verificar que la clave pública
 * realmente fue registrada en el servidor legítimo, previniendo
 * ataques Man-in-the-Middle.
 */
public record CertifiedPublicKeyResponse(
    Long userId,
    String publicKey,           // Base64 encoded public key
    String algorithm,           // "RSA" o "EC"
    Integer keySize,            // 2048, 4096, 256, 384
    String serverSignature,     // Base64 - Firma del servidor
    String signatureAlgorithm,  // "SHA256withRSA" o "SHA256withECDSA"
    LocalDateTime signedAt      // Timestamp de la firma
) {
}

