package org.example.springcrypto.controller;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.service.VaultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador de Caja Fuerte (Vault)
 *
 * IMPORTANTE: Este servidor NO descifra nada. Solo almacena datos ya cifrados
 * que llegan del cliente. El cifrado/descifrado se hace en el cliente con
 * la password del usuario.
 *
 * Arquitectura Zero-Knowledge: El servidor nunca tiene acceso a los datos en claro.
 */
@RestController
@RequestMapping("/api/vault")
public class VaultController {

    private final VaultService vaultService;

    public VaultController(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    /**
     * Guarda un secreto cifrado
     *
     * El cliente debe:
     * 1. Derivar clave AES con PBKDF2(password, salt, 100000 iteraciones)
     * 2. Cifrar datos con AES-256-GCM
     * 3. Enviar: encryptedData (base64), iv (base64), salt (base64)
     *
     * @param userId Mock de autenticación (en producción: obtener de JWT)
     * @param request Datos cifrados desde el cliente
     * @return ID del secreto guardado
     */
    @PostMapping("/secrets")
    public ResponseEntity<SaveSecretResponse> saveSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @RequestBody SaveSecretRequest request
    ) {
        SaveSecretResponse response = vaultService.saveSecret(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene un secreto cifrado
     *
     * El servidor devuelve los datos TAL CUAL están en la base de datos (cifrados).
     * El cliente debe descifrarlos con la password del usuario.
     *
     * @param userId Mock de autenticación
     * @param secretId ID del secreto
     * @return Datos cifrados, IV y salt
     */
    @GetMapping("/secrets/{secretId}")
    public ResponseEntity<SecretDetailResponse> getSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long secretId
    ) {
        SecretDetailResponse response = vaultService.getSecret(userId, secretId);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista todos los secretos del usuario
     *
     * Devuelve solo metadatos (que también están cifrados).
     * El cliente puede descifrar los títulos para mostrar una lista.
     *
     * @param userId Mock de autenticación
     * @return Lista de secretos (solo IDs y metadatos cifrados)
     */
    @GetMapping("/secrets")
    public ResponseEntity<List<SecretListItem>> listSecrets(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        List<SecretListItem> secrets = vaultService.listSecrets(userId);
        return ResponseEntity.ok(secrets);
    }

    /**
     * Elimina un secreto
     *
     * @param userId Mock de autenticación
     * @param secretId ID del secreto
     */
    @DeleteMapping("/secrets/{secretId}")
    public ResponseEntity<Void> deleteSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long secretId
    ) {
        vaultService.deleteSecret(userId, secretId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Actualiza un secreto (re-cifrado)
     *
     * Útil si el usuario quiere cambiar la password:
     * 1. Cliente descifra con password antigua
     * 2. Cliente re-cifra con password nueva
     * 3. Cliente envía datos re-cifrados al servidor
     *
     * @param userId Mock de autenticación
     * @param secretId ID del secreto
     * @param request Nuevos datos cifrados
     */
    @PutMapping("/secrets/{secretId}")
    public ResponseEntity<Void> updateSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long secretId,
        @RequestBody SaveSecretRequest request
    ) {
        vaultService.updateSecret(userId, secretId, request);
        return ResponseEntity.ok().build();
    }
}

