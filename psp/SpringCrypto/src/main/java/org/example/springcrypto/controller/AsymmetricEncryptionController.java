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
     * Encriptación con ECIES (Elliptic Curve Integrated Encryption Scheme)
     * POST /api/asymmetric/encrypt-ecies
     * Body: { "plainText": "texto", "publicKey": "clave_publica_EC_base64" }
     */
    @PostMapping("/encrypt-ecies")
    public ResponseEntity<?> encryptECIES(@RequestBody Map<String, String> request) {
        try {
            String plainText = request.get("plainText");
            String publicKeyBase64 = request.get("publicKey");

            if (plainText == null || publicKeyBase64 == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Se requieren los campos 'plainText' y 'publicKey'"));
            }

            PublicKey publicKey = encryptionService.base64ToPublicKey(publicKeyBase64);
            AsymmetricEncryptionService.ECIESResult result =
                    encryptionService.encryptECIES(plainText, publicKey);

            return ResponseEntity.ok(Map.of(
                    "ephemeralPublicKey", result.ephemeralPublicKey(),
                    "encryptedData", result.encryptedData(),
                    "iv", result.iv(),
                    "algorithm", "ECIES (ECDH + AES-GCM)",
                    "description", "Cifrado híbrido con curvas elípticas"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Desencriptación con ECIES
     * POST /api/asymmetric/decrypt-ecies
     * Body: {
     *   "privateKey": "clave_privada_EC_base64",
     *   "ephemeralPublicKey": "clave_publica_efimera_base64",
     *   "encryptedData": "datos_cifrados_base64",
     *   "iv": "iv_base64"
     * }
     */
    @PostMapping("/decrypt-ecies")
    public ResponseEntity<?> decryptECIES(@RequestBody Map<String, String> request) {
        try {
            String privateKeyBase64 = request.get("privateKey");
            String ephemeralPublicKey = request.get("ephemeralPublicKey");
            String encryptedData = request.get("encryptedData");
            String iv = request.get("iv");

            if (privateKeyBase64 == null || ephemeralPublicKey == null ||
                encryptedData == null || iv == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Se requieren todos los campos: privateKey, ephemeralPublicKey, encryptedData, iv"));
            }

            PrivateKey privateKey = encryptionService.base64ToPrivateKey(privateKeyBase64);
            AsymmetricEncryptionService.ECIESResult eciesResult =
                    new AsymmetricEncryptionService.ECIESResult(ephemeralPublicKey, encryptedData, iv);

            String plainText = encryptionService.decryptECIES(eciesResult, privateKey);

            return ResponseEntity.ok(Map.of(
                    "plainText", plainText,
                    "algorithm", "ECIES"
            ));
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

            // Generar par de claves RSA
            KeyPair keyPair = encryptionService.generateKeyPair("RSA");
            String publicKeyBase64 = encryptionService.publicKeyToBase64(keyPair.getPublic());
            String privateKeyBase64 = encryptionService.privateKeyToBase64(keyPair.getPrivate());

            // Generar par de claves EC
            KeyPair ecKeyPair = encryptionService.generateKeyPair("EC");
            String ecPublicKeyBase64 = encryptionService.publicKeyToBase64(ecKeyPair.getPublic());
            String ecPrivateKeyBase64 = encryptionService.privateKeyToBase64(ecKeyPair.getPrivate());

            // Encriptación normal RSA
            String encryptedOAEP = encryptionService.encryptOAEP(originalText, keyPair.getPublic());
            String decryptedOAEP = encryptionService.decryptOAEP(encryptedOAEP, keyPair.getPrivate());

            // Firma digital RSA
            String signature = encryptionService.sign(originalText, keyPair.getPrivate());
            boolean signatureValid = encryptionService.verify(originalText, signature, keyPair.getPublic());

            // Firma digital ECDSA
            String ecSignature = encryptionService.sign(originalText, ecKeyPair.getPrivate());
            boolean ecSignatureValid = encryptionService.verify(originalText, ecSignature, ecKeyPair.getPublic());

            // Encriptación híbrida RSA + AES
            AsymmetricEncryptionService.HybridEncryptionResult hybridResult =
                    encryptionService.encryptHybrid(longMessage, keyPair.getPublic());
            String decryptedHybrid = encryptionService.decryptHybrid(hybridResult, keyPair.getPrivate());

            // Encriptación ECIES (EC + AES)
            AsymmetricEncryptionService.ECIESResult eciesResult =
                    encryptionService.encryptECIES(longMessage, ecKeyPair.getPublic());
            String decryptedECIES = encryptionService.decryptECIES(eciesResult, ecKeyPair.getPrivate());

            return ResponseEntity.ok(Map.of(
                    "rsaKeys", Map.of(
                            "publicKey", publicKeyBase64,
                            "privateKey", privateKeyBase64,
                            "algorithm", "RSA-2048"
                    ),
                    "ecKeys", Map.of(
                            "publicKey", ecPublicKeyBase64,
                            "privateKey", ecPrivateKeyBase64,
                            "algorithm", "EC (P-256/secp256r1)"
                    ),
                    "standardEncryption", Map.of(
                            "original", originalText,
                            "encrypted", encryptedOAEP,
                            "decrypted", decryptedOAEP,
                            "match", decryptedOAEP.equals(originalText)
                    ),
                    "rsaSignature", Map.of(
                            "message", originalText,
                            "signature", signature,
                            "valid", signatureValid
                    ),
                    "ecdsaSignature", Map.of(
                            "message", originalText,
                            "signature", ecSignature,
                            "valid", ecSignatureValid,
                            "signatureLength", ecSignature.length() + " chars (más pequeña que RSA)"
                    ),
                    "hybridEncryption", Map.of(
                            "originalLength", longMessage.length(),
                            "encryptedData", hybridResult.encryptedData().substring(0, 50) + "...",
                            "decryptedLength", decryptedHybrid.length(),
                            "match", decryptedHybrid.equals(longMessage),
                            "description", "RSA + AES-GCM"
                    ),
                    "eciesEncryption", Map.of(
                            "originalLength", longMessage.length(),
                            "encryptedData", eciesResult.encryptedData().substring(0, 50) + "...",
                            "decryptedLength", decryptedECIES.length(),
                            "match", decryptedECIES.equals(longMessage),
                            "description", "ECIES (ECDH + AES-GCM)",
                            "ephemeralPublicKey", eciesResult.ephemeralPublicKey().substring(0, 50) + "..."
                    )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}

