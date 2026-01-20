package org.example.springcrypto.controller;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.service.SymmetricEncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.util.Map;

/**
 * REST Controller para encriptación simétrica
 */
@RestController
@RequestMapping("/api/symmetric")
public class SymmetricEncryptionController {

    private final SymmetricEncryptionService encryptionService;

    public SymmetricEncryptionController(SymmetricEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Genera una nueva clave AES
     * GET /api/symmetric/generate-key
     */
    @GetMapping("/generate-key")
    public ResponseEntity<Map<String, String>> generateKey() {
        try {
            SecretKey key = encryptionService.generateKey();
            String keyBase64 = encryptionService.keyToBase64(key);
            return ResponseEntity.ok(Map.of(
                    "key", keyBase64,
                    "algorithm", "AES",
                    "keySize", "256",
                    "type", "random"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Genera una clave AES desde un password/string usando PBKDF2
     * POST /api/symmetric/generate-key-from-password
     */
    @PostMapping("/generate-key-from-password")
    public ResponseEntity<Map<String, String>> generateKeyFromPassword(@RequestBody Map<String, String> request) {
        try {
            String password = request.get("password");
            if (password == null || password.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "El campo 'password' es requerido"));
            }

            SecretKey key = encryptionService.generateKeyFromPassword(password);
            String keyBase64 = encryptionService.keyToBase64(key);

            return ResponseEntity.ok(Map.of(
                    "key", keyBase64,
                    "algorithm", "AES",
                    "keySize", "256",
                    "type", "derived",
                    "derivationMethod", "PBKDF2WithHmacSHA256",
                    "iterations", "65536",
                    "info", "La misma contraseña siempre genera la misma clave"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Obtiene la clave configurada en application.properties
     * GET /api/symmetric/configured-key
     */
    @GetMapping("/configured-key")
    public ResponseEntity<Map<String, String>> getConfiguredKey() {
        try {
            SecretKey key = encryptionService.getConfiguredKey();
            String keyBase64 = encryptionService.keyToBase64(key);

            return ResponseEntity.ok(Map.of(
                    "key", keyBase64,
                    "algorithm", "AES",
                    "keySize", "256",
                    "type", "configured",
                    "info", "Clave derivada desde application.properties (crypto.aes.secret-key)"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Encripta texto usando AES
     * POST /api/symmetric/encrypt
     */
    @PostMapping("/encrypt")
    public ResponseEntity<?> encrypt(@RequestBody SymmetricEncryptRequest request) {
        try {
            // Obtener o generar clave
            SecretKey key;
            String keyBase64;
            if (request.key() != null && !request.key().isEmpty()) {
                key = encryptionService.base64ToKey(request.key());
                keyBase64 = request.key();
            } else {
                key = encryptionService.generateKey();
                keyBase64 = encryptionService.keyToBase64(key);
            }

            String encryptedText;
            String ivBase64 = null;
            String mode = request.mode() != null ? request.mode().toUpperCase() : "GCM";

            switch (mode) {
                case "ECB" -> encryptedText = encryptionService.encryptECB(request.plainText(), key);
                case "CBC" -> {
                    byte[] iv = encryptionService.generateIV();
                    encryptedText = encryptionService.encryptCBC(request.plainText(), key, iv);
                    ivBase64 = encryptionService.ivToBase64(iv);
                }
                case "CTR" -> {
                    byte[] iv = encryptionService.generateIV();
                    encryptedText = encryptionService.encryptCTR(request.plainText(), key, iv);
                    ivBase64 = encryptionService.ivToBase64(iv);
                }
                case "GCM" -> {
                    byte[] iv = encryptionService.generateIV();
                    encryptedText = encryptionService.encryptGCM(request.plainText(), key, iv);
                    ivBase64 = encryptionService.ivToBase64(iv);
                }
                default -> {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Modo no soportado: " + mode + ". Use: ECB, CBC, CTR, GCM"));
                }
            }

            return ResponseEntity.ok(new SymmetricEncryptResponse(
                    encryptedText,
                    keyBase64,
                    mode,
                    ivBase64
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Desencripta texto usando AES
     * POST /api/symmetric/decrypt
     */
    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@RequestBody SymmetricDecryptRequest request) {
        try {
            SecretKey key = encryptionService.base64ToKey(request.key());
            String plainText;
            String mode = request.mode() != null ? request.mode().toUpperCase() : "GCM";

            switch (mode) {
                case "ECB" -> plainText = encryptionService.decryptECB(request.encryptedText(), key);
                case "CBC" -> plainText = encryptionService.decryptCBC(request.encryptedText(), key);
                case "CTR" -> plainText = encryptionService.decryptCTR(request.encryptedText(), key);
                case "GCM" -> plainText = encryptionService.decryptGCM(request.encryptedText(), key);
                default -> {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Modo no soportado: " + mode));
                }
            }

            return ResponseEntity.ok(new SymmetricDecryptResponse(plainText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint de ejemplo rápido para demostrar el flujo completo
     * GET /api/symmetric/demo
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demo() {
        try {
            String originalText = "¡Hola! Este es un mensaje secreto 🔐";

            // Generar clave
            SecretKey key = encryptionService.generateKey();
            String keyBase64 = encryptionService.keyToBase64(key);

            // Encriptar con diferentes modos
            byte[] iv = encryptionService.generateIV();
            String encryptedECB = encryptionService.encryptECB(originalText, key);
            String encryptedCBC = encryptionService.encryptCBC(originalText, key, iv);
            String encryptedGCM = encryptionService.encryptGCM(originalText, key, iv);

            // Desencriptar
            String decryptedECB = encryptionService.decryptECB(encryptedECB, key);
            String decryptedCBC = encryptionService.decryptCBC(encryptedCBC, key);
            String decryptedGCM = encryptionService.decryptGCM(encryptedGCM, key);

            return ResponseEntity.ok(Map.of(
                    "originalText", originalText,
                    "key", keyBase64,
                    "results", Map.of(
                            "ECB", Map.of(
                                    "encrypted", encryptedECB,
                                    "decrypted", decryptedECB,
                                    "match", decryptedECB.equals(originalText)
                            ),
                            "CBC", Map.of(
                                    "encrypted", encryptedCBC,
                                    "decrypted", decryptedCBC,
                                    "match", decryptedCBC.equals(originalText)
                            ),
                            "GCM", Map.of(
                                    "encrypted", encryptedGCM,
                                    "decrypted", decryptedGCM,
                                    "match", decryptedGCM.equals(originalText)
                            )
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

