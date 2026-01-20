package org.example.springcrypto.controller;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.service.AsymmetricEncryptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Map;

/**
 * REST Controller para encriptación asimétrica
 */
@RestController
@RequestMapping("/api/asymmetric")
public class AsymmetricEncryptionController {

    private final AsymmetricEncryptionService encryptionService;

    public AsymmetricEncryptionController(AsymmetricEncryptionService encryptionService) {
        this.encryptionService = encryptionService;
    }

    /**
     * Genera un par de claves (RSA o EC)
     * GET /api/asymmetric/generate-keypair?algorithm=RSA
     * GET /api/asymmetric/generate-keypair?algorithm=EC
     */
    @GetMapping("/generate-keypair")
    public ResponseEntity<?> generateKeyPair(@RequestParam(required = false, defaultValue = "RSA") String algorithm) {
        try {
            KeyPair keyPair = encryptionService.generateKeyPair(algorithm);
            String publicKeyBase64 = encryptionService.publicKeyToBase64(keyPair.getPublic());
            String privateKeyBase64 = encryptionService.privateKeyToBase64(keyPair.getPrivate());

            return ResponseEntity.ok(Map.of(
                    "publicKey", publicKeyBase64,
                    "privateKey", privateKeyBase64,
                    "algorithm", keyPair.getPublic().getAlgorithm(),
                    "keySize", algorithm.equalsIgnoreCase("EC") ? "256 (P-256/secp256r1)" : "2048"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Encripta texto usando RSA
     * POST /api/asymmetric/encrypt
     */
    @PostMapping("/encrypt")
    public ResponseEntity<?> encrypt(@RequestBody AsymmetricEncryptRequest request) {
        try {
            PublicKey publicKey = encryptionService.base64ToPublicKey(request.publicKey());
            String padding = request.padding() != null ? request.padding().toUpperCase() : "OAEP";

            String encryptedText = switch (padding) {
                case "PKCS1" -> encryptionService.encryptPKCS1(request.plainText(), publicKey);
                case "OAEP" -> encryptionService.encryptOAEP(request.plainText(), publicKey);
                default -> throw new IllegalArgumentException("Padding no soportado: " + padding);
            };

            return ResponseEntity.ok(new AsymmetricEncryptResponse(encryptedText, padding));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Desencripta texto usando RSA
     * POST /api/asymmetric/decrypt
     */
    @PostMapping("/decrypt")
    public ResponseEntity<?> decrypt(@RequestBody AsymmetricDecryptRequest request) {
        try {
            PrivateKey privateKey = encryptionService.base64ToPrivateKey(request.privateKey());
            String padding = request.padding() != null ? request.padding().toUpperCase() : "OAEP";

            String plainText = switch (padding) {
                case "PKCS1" -> encryptionService.decryptPKCS1(request.encryptedText(), privateKey);
                case "OAEP" -> encryptionService.decryptOAEP(request.encryptedText(), privateKey);
                default -> throw new IllegalArgumentException("Padding no soportado: " + padding);
            };

            return ResponseEntity.ok(new AsymmetricDecryptResponse(plainText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Firma un mensaje con la clave privada
     * POST /api/asymmetric/sign
     */
    @PostMapping("/sign")
    public ResponseEntity<?> sign(@RequestBody SignRequest request) {
        try {
            PrivateKey privateKey = encryptionService.base64ToPrivateKey(request.privateKey());
            String signature = encryptionService.sign(request.message(), privateKey);

            return ResponseEntity.ok(new SignResponse(signature));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Verifica una firma digital
     * POST /api/asymmetric/verify
     */
    @PostMapping("/verify")
    public ResponseEntity<?> verify(@RequestBody VerifyRequest request) {
        try {
            PublicKey publicKey = encryptionService.base64ToPublicKey(request.publicKey());
            boolean valid = encryptionService.verify(
                    request.message(),
                    request.signature(),
                    publicKey
            );

            return ResponseEntity.ok(new VerifyResponse(valid));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Encriptación híbrida (RSA + AES)
     * POST /api/asymmetric/encrypt-hybrid
     */
    @PostMapping("/encrypt-hybrid")
    public ResponseEntity<?> encryptHybrid(@RequestBody HybridEncryptRequest request) {
        try {
            PublicKey publicKey = encryptionService.base64ToPublicKey(request.publicKey());
            AsymmetricEncryptionService.HybridEncryptionResult result =
                    encryptionService.encryptHybrid(request.plainText(), publicKey);

            return ResponseEntity.ok(new HybridEncryptResponse(
                    result.encryptedData(),
                    result.encryptedKey(),
                    result.iv()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Desencriptación híbrida
     * POST /api/asymmetric/decrypt-hybrid
     */
    @PostMapping("/decrypt-hybrid")
    public ResponseEntity<?> decryptHybrid(@RequestBody HybridDecryptRequest request) {
        try {
            PrivateKey privateKey = encryptionService.base64ToPrivateKey(request.privateKey());
            AsymmetricEncryptionService.HybridEncryptionResult result =
                    new AsymmetricEncryptionService.HybridEncryptionResult(
                            request.encryptedData(),
                            request.encryptedKey(),
                            request.iv()
                    );

            String plainText = encryptionService.decryptHybrid(result, privateKey);

            return ResponseEntity.ok(new HybridDecryptResponse(plainText));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint de ejemplo rápido para demostrar el flujo completo
     * GET /api/asymmetric/demo
     */
    @GetMapping("/demo")
    public ResponseEntity<Map<String, Object>> demo() {
        try {
            String originalText = "¡Mensaje cifrado con RSA! 🔒";
            String longMessage = "Este es un mensaje muy largo que excede el límite de RSA normal. ".repeat(10);

            // Generar par de claves
            KeyPair keyPair = encryptionService.generateKeyPair();
            String publicKeyBase64 = encryptionService.publicKeyToBase64(keyPair.getPublic());
            String privateKeyBase64 = encryptionService.privateKeyToBase64(keyPair.getPrivate());

            // Encriptación normal
            String encryptedOAEP = encryptionService.encryptOAEP(originalText, keyPair.getPublic());
            String decryptedOAEP = encryptionService.decryptOAEP(encryptedOAEP, keyPair.getPrivate());

            // Firma digital
            String signature = encryptionService.sign(originalText, keyPair.getPrivate());
            boolean signatureValid = encryptionService.verify(originalText, signature, keyPair.getPublic());

            // Encriptación híbrida
            AsymmetricEncryptionService.HybridEncryptionResult hybridResult =
                    encryptionService.encryptHybrid(longMessage, keyPair.getPublic());
            String decryptedHybrid = encryptionService.decryptHybrid(hybridResult, keyPair.getPrivate());

            return ResponseEntity.ok(Map.of(
                    "keys", Map.of(
                            "publicKey", publicKeyBase64,
                            "privateKey", privateKeyBase64
                    ),
                    "standardEncryption", Map.of(
                            "original", originalText,
                            "encrypted", encryptedOAEP,
                            "decrypted", decryptedOAEP,
                            "match", decryptedOAEP.equals(originalText)
                    ),
                    "digitalSignature", Map.of(
                            "message", originalText,
                            "signature", signature,
                            "valid", signatureValid
                    ),
                    "hybridEncryption", Map.of(
                            "originalLength", longMessage.length(),
                            "encryptedData", hybridResult.encryptedData().substring(0, 50) + "...",
                            "decryptedLength", decryptedHybrid.length(),
                            "match", decryptedHybrid.equals(longMessage)
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

