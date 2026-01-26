package org.example.springcrypto.service;

import org.springframework.stereotype.Service;

import java.security.*;
import javax.crypto.KeyGenerator;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;

/**
 * Servicio para encriptación asimétrica con RSA y EC
 * Usa par de claves: pública (cifrar/verificar) y privada (descifrar/firmar)
 */
@Service
public class AsymmetricEncryptionService {

    private static final String RSA_ALGORITHM = "RSA";
    private static final String EC_ALGORITHM = "EC";
    private static final int RSA_KEY_SIZE = 2048;
    private static final String EC_CURVE = "secp256r1"; // También conocida como P-256 o prime256v1

    /**
     * Genera un par de claves RSA (pública y privada)
     */
    public KeyPair generateKeyPair() throws Exception {
        return generateKeyPair("RSA");
    }

    /**
     * Genera un par de claves con el algoritmo especificado
     * @param algorithm "RSA" o "EC" (Elliptic Curve)
     */
    public KeyPair generateKeyPair(String algorithm) throws Exception {
        if (algorithm == null || algorithm.isEmpty()) {
            algorithm = "RSA";
        }

        algorithm = algorithm.toUpperCase();

        return switch (algorithm) {
            case "RSA" -> {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
                keyPairGenerator.initialize(RSA_KEY_SIZE);
                yield keyPairGenerator.generateKeyPair();
            }
            case "EC", "ECDSA", "ECC" -> {
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(EC_ALGORITHM);
                // Usar curva secp256r1 (P-256) - equivalente a RSA-3072 en seguridad
                java.security.spec.ECGenParameterSpec ecSpec =
                    new java.security.spec.ECGenParameterSpec(EC_CURVE);
                keyPairGenerator.initialize(ecSpec);
                yield keyPairGenerator.generateKeyPair();
            }
            default -> throw new IllegalArgumentException(
                "Algoritmo no soportado: " + algorithm + ". Use 'RSA' o 'EC'");
        };
    }

    /**
     * Convierte clave pública a Base64
     */
    public String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Convierte clave privada a Base64
     */
    public String privateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }

    /**
     * Convierte Base64 a clave pública
     * Detecta automáticamente si es RSA o EC
     */
    public PublicKey base64ToPublicKey(String base64PublicKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PublicKey);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);

        // Intentar primero con RSA, luego con EC
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            // Si falla con RSA, intentar con EC
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            return keyFactory.generatePublic(keySpec);
        }
    }

    /**
     * Convierte Base64 a clave privada
     * Detecta automáticamente si es RSA o EC
     */
    public PrivateKey base64ToPrivateKey(String base64PrivateKey) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);

        // Intentar primero con RSA, luego con EC
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            // Si falla con RSA, intentar con EC
            KeyFactory keyFactory = KeyFactory.getInstance(EC_ALGORITHM);
            return keyFactory.generatePrivate(keySpec);
        }
    }

    /**
     * RSA con PKCS1 Padding (clásico)
     * Tamaño máximo de datos: (keySize / 8) - 11 bytes
     * Para RSA-2048: 256 - 11 = 245 bytes
     */
    public String encryptPKCS1(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptPKCS1(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * RSA con OAEP Padding (más seguro)
     * OAEP = Optimal Asymmetric Encryption Padding
     * Tamaño máximo de datos: (keySize / 8) - 42 bytes
     * Para RSA-2048: 256 - 42 = 214 bytes
     */
    public String encryptOAEP(String plainText, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptOAEP(String encryptedText, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Firma digital con RSA
     * Se firma con la clave privada y se verifica con la pública
     */
    public String sign(String message, PrivateKey privateKey) throws Exception {
        // Detectar el algoritmo de la clave
        String algorithm = privateKey.getAlgorithm();
        String signatureAlgorithm = algorithm.equals("EC") ? "SHA256withECDSA" : "SHA256withRSA";

        Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Verifica una firma digital (RSA o ECDSA)
     */
    public boolean verify(String message, String signatureBase64, PublicKey publicKey) throws Exception {
        // Detectar el algoritmo de la clave
        String algorithm = publicKey.getAlgorithm();
        String signatureAlgorithm = algorithm.equals("EC") ? "SHA256withECDSA" : "SHA256withRSA";

        Signature signature = Signature.getInstance(signatureAlgorithm);
        signature.initVerify(publicKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }

    /**
     * Firma digital específicamente con ECDSA
     * Curvas elípticas - firmas más pequeñas que RSA
     */
    public String signECDSA(String message, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initSign(privateKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = signature.sign();
        return Base64.getEncoder().encodeToString(signatureBytes);
    }

    /**
     * Verifica una firma ECDSA
     */
    public boolean verifyECDSA(String message, String signatureBase64, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withECDSA");
        signature.initVerify(publicKey);
        signature.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }

    /**
     * Encriptación híbrida: Combina RSA + AES
     * 1. Genera clave AES aleatoria
     * 2. Encripta datos con AES (rápido)
     * 3. Encripta clave AES con RSA (seguro)
     *
     * Ventaja: Permite encriptar mensajes grandes sin límite de tamaño RSA
     */
    public HybridEncryptionResult encryptHybrid(String plainText, PublicKey publicKey) throws Exception {
        // Generar clave AES aleatoria
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        Key aesKey = keyGenerator.generateKey();

        // Encriptar datos con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // GCM usa IV de 12 bytes
        new SecureRandom().nextBytes(iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new javax.crypto.spec.GCMParameterSpec(128, iv));
        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Encriptar clave AES con RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedKey = rsaCipher.doFinal(aesKey.getEncoded());

        return new HybridEncryptionResult(
                Base64.getEncoder().encodeToString(encryptedData),
                Base64.getEncoder().encodeToString(encryptedKey),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    public String decryptHybrid(HybridEncryptionResult hybridResult, PrivateKey privateKey) throws Exception {
        // Descifrar clave AES con RSA
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] aesKeyBytes = rsaCipher.doFinal(Base64.getDecoder().decode(hybridResult.encryptedKey()));
        Key aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

        // Descifrar datos con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(hybridResult.iv());
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new javax.crypto.spec.GCMParameterSpec(128, iv));
        byte[] decryptedData = aesCipher.doFinal(Base64.getDecoder().decode(hybridResult.encryptedData()));

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * ECIES - Elliptic Curve Integrated Encryption Scheme
     * Cifrado con curvas elípticas usando ECDH + AES-GCM
     *
     * Proceso:
     * 1. Genera un par de claves efímero EC
     * 2. Usa ECDH para derivar clave compartida
     * 3. Deriva clave AES de la clave compartida (KDF)
     * 4. Cifra datos con AES-GCM
     * 5. Devuelve: clave pública efímera + datos cifrados + IV
     */
    public ECIESResult encryptECIES(String plainText, PublicKey recipientPublicKey) throws Exception {
        // Verificar que sea una clave EC
        if (!recipientPublicKey.getAlgorithm().equals("EC")) {
            throw new IllegalArgumentException("ECIES requiere una clave pública EC");
        }

        // 1. Generar par de claves efímero
        KeyPair ephemeralKeyPair = generateKeyPair("EC");

        // 2. Realizar ECDH para obtener secreto compartido
        javax.crypto.KeyAgreement keyAgreement = javax.crypto.KeyAgreement.getInstance("ECDH");
        keyAgreement.init(ephemeralKeyPair.getPrivate());
        keyAgreement.doPhase(recipientPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();

        // 3. Derivar clave AES del secreto compartido usando KDF (HKDF simplificado con SHA-256)
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        Key aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

        // 4. Cifrar con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // GCM usa IV de 12 bytes
        new SecureRandom().nextBytes(iv);
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, new javax.crypto.spec.GCMParameterSpec(128, iv));
        byte[] encryptedData = aesCipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // 5. Devolver clave pública efímera + datos cifrados + IV
        return new ECIESResult(
                publicKeyToBase64(ephemeralKeyPair.getPublic()),
                Base64.getEncoder().encodeToString(encryptedData),
                Base64.getEncoder().encodeToString(iv)
        );
    }

    /**
     * Descifra datos cifrados con ECIES
     */
    public String decryptECIES(ECIESResult eciesResult, PrivateKey recipientPrivateKey) throws Exception {
        // Verificar que sea una clave EC
        if (!recipientPrivateKey.getAlgorithm().equals("EC")) {
            throw new IllegalArgumentException("ECIES requiere una clave privada EC");
        }

        // 1. Recuperar clave pública efímera
        PublicKey ephemeralPublicKey = base64ToPublicKey(eciesResult.ephemeralPublicKey());

        // 2. Realizar ECDH con nuestra clave privada y la clave pública efímera
        javax.crypto.KeyAgreement keyAgreement = javax.crypto.KeyAgreement.getInstance("ECDH");
        keyAgreement.init(recipientPrivateKey);
        keyAgreement.doPhase(ephemeralPublicKey, true);
        byte[] sharedSecret = keyAgreement.generateSecret();

        // 3. Derivar la misma clave AES
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        Key aesKey = new javax.crypto.spec.SecretKeySpec(aesKeyBytes, "AES");

        // 4. Descifrar con AES-GCM
        Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = Base64.getDecoder().decode(eciesResult.iv());
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new javax.crypto.spec.GCMParameterSpec(128, iv));
        byte[] decryptedData = aesCipher.doFinal(Base64.getDecoder().decode(eciesResult.encryptedData()));

        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    /**
     * Record para almacenar resultado de encriptación híbrida
     */
    public record HybridEncryptionResult(String encryptedData, String encryptedKey, String iv) {}

    /**
     * Record para almacenar resultado de ECIES
     * ephemeralPublicKey: Clave pública efímera generada para este cifrado
     * encryptedData: Datos cifrados con AES-GCM
     * iv: Vector de inicialización para AES-GCM
     */
    public record ECIESResult(String ephemeralPublicKey, String encryptedData, String iv) {}
}

