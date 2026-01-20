package org.example.springcrypto.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Servicio para encriptación simétrica con AES
 * Implementa diferentes modos de operación: ECB, CBC, CTR, GCM
 */
@Service
public class SymmetricEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_SIZE = 16;

    // Salt fijo para PBKDF2 - En producción esto debería ser único por aplicación
    private static final byte[] PBKDF2_SALT = "SpringCrypto2026".getBytes(StandardCharsets.UTF_8);
    private static final int PBKDF2_ITERATIONS = 65536; // Iteraciones para PBKDF2

    @Value("${crypto.aes.secret-key:#{null}}")
    private String configuredSecretKey;

    /**
     * Genera una clave AES de 256 bits
     */
    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
        keyGenerator.init(KEY_SIZE);
        return keyGenerator.generateKey();
    }

    /**
     * Obtiene la clave configurada en application.properties
     * Si no está configurada, genera una nueva
     */
    public SecretKey getConfiguredKey() throws Exception {
        if (configuredSecretKey != null && !configuredSecretKey.isEmpty()) {
            return generateKeyFromPassword(configuredSecretKey);
        }
        return generateKey();
    }

    /**
     * Genera una clave AES desde un password/string usando PBKDF2
     * PBKDF2 = Password-Based Key Derivation Function 2
     *
     * @param password String base para generar la clave (puede ser cualquier texto)
     * @return SecretKey derivada del password
     */
    public SecretKey generateKeyFromPassword(String password) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            PBKDF2_SALT,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    /**
     * Genera una clave AES desde un password con salt personalizado
     *
     * @param password String base para generar la clave
     * @param salt Salt único (mínimo 8 bytes recomendado)
     * @return SecretKey derivada del password y salt
     */
    public SecretKey generateKeyFromPassword(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        );
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), ALGORITHM);
    }

    /**
     * Convierte una clave a Base64 para almacenamiento/transmisión
     */
    public String keyToBase64(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    /**
     * Convierte una clave desde Base64
     */
    public SecretKey base64ToKey(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, ALGORITHM);
    }

    /**
     * AES-ECB: Electronic Codebook Mode
     * NO RECOMENDADO para producción - Mismo texto produce mismo cifrado
     * No usa IV (Vector de Inicialización)
     */
    public String encryptECB(String plainText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptECB(String encryptedText, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(encryptedText);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES-CBC: Cipher Block Chaining
     * Cada bloque se XOR con el anterior antes de encriptar
     * Requiere IV (Vector de Inicialización)
     */
    public String encryptCBC(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + texto cifrado para facilitar descifrado
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptCBC(String encryptedText, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        // Extraer IV y texto cifrado
        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES-CTR: Counter Mode
     * Convierte el cifrado de bloque en cifrado de flujo
     * Paralelizable y no requiere padding
     */
    public String encryptCTR(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + texto cifrado
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptCTR(String encryptedText, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, key, ivSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * AES-GCM: Galois/Counter Mode
     * RECOMENDADO - Proporciona encriptación y autenticación (AEAD)
     * Detecta modificaciones en el texto cifrado
     */
    public String encryptGCM(String plainText, SecretKey key, byte[] iv) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);
        byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + texto cifrado (que incluye el tag de autenticación)
        byte[] combined = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, combined, 0, iv.length);
        System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(combined);
    }

    public String decryptGCM(String encryptedText, SecretKey key) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedText);

        byte[] iv = new byte[IV_SIZE];
        byte[] encrypted = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encrypted, 0, encrypted.length);

        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    /**
     * Genera un IV (Vector de Inicialización) aleatorio
     */
    public byte[] generateIV() {
        byte[] iv = new byte[IV_SIZE];
        new SecureRandom().nextBytes(iv);
        return iv;
    }

    /**
     * Convierte IV a Base64
     */
    public String ivToBase64(byte[] iv) {
        return Base64.getEncoder().encodeToString(iv);
    }

    /**
     * Convierte IV desde Base64
     */
    public byte[] base64ToIv(String base64Iv) {
        return Base64.getDecoder().decode(base64Iv);
    }
}

