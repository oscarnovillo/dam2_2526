package org.example.springcrypto.controller;

import org.example.springcrypto.dto.*;
import org.example.springcrypto.service.SharingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador para Compartir Secretos entre Usuarios
 *
 * Flujo completo:
 *
 * 1. SETUP: Cada usuario registra su clave pública
 *    POST /api/sharing/public-key
 *
 * 2. COMPARTIR: Usuario A comparte con Usuario B
 *    a) A descifra su secreto con su password (AES)
 *    b) A obtiene la clave pública de B
 *    c) A cifra el secreto con la clave pública de B (RSA/EC)
 *    d) A envía al servidor
 *    POST /api/sharing/share
 *
 * 3. ACCEDER: Usuario B accede al secreto compartido
 *    a) B obtiene el secreto cifrado
 *    b) B descifra con su clave privada (RSA/EC)
 *    GET /api/sharing/shared-with-me
 *    GET /api/sharing/shares/{shareId}
 *
 * 4. REVOCAR: Usuario A revoca acceso
 *    DELETE /api/sharing/revoke/{secretId}/{userId}
 */
@RestController
@RequestMapping("/api/sharing")
public class SharingController {

    private final SharingService sharingService;

    public SharingController(SharingService sharingService) {
        this.sharingService = sharingService;
    }

    /**
     * Registra la clave pública de un usuario
     *
     * El cliente genera un par de claves RSA/EC y registra la pública aquí.
     * La clave privada NUNCA se envía al servidor.
     *
     * @param userId ID del usuario
     * @param request Clave pública en formato X.509 (Base64)
     */
    @PostMapping("/public-key")
    public ResponseEntity<Void> registerPublicKey(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @RequestBody RegisterPublicKeyRequest request
    ) {
        sharingService.registerPublicKey(userId, request);
        return ResponseEntity.ok().build();
    }

    /**
     * Obtiene la clave pública de un usuario
     *
     * Necesario para cifrar un secreto antes de compartirlo.
     *
     * @param targetUserId ID del usuario cuya clave pública se quiere obtener
     */
    @GetMapping("/public-key/{targetUserId}")
    public ResponseEntity<UserPublicKeyResponse> getUserPublicKey(
        @PathVariable Long targetUserId
    ) {
        UserPublicKeyResponse response = sharingService.getUserPublicKey(targetUserId);
        return ResponseEntity.ok(response);
    }

    /**
     * Comparte un secreto con otro usuario
     *
     * Flujo del cliente:
     * 1. Descifrar secreto con password (AES)
     * 2. GET /api/sharing/public-key/{targetUserId}
     * 3. Cifrar secreto con clave pública del receptor (RSA/EC)
     * 4. POST este endpoint con datos cifrados
     *
     * @param userId ID del usuario que comparte (owner)
     * @param request Datos del secreto compartido
     */
    @PostMapping("/share")
    public ResponseEntity<ShareSecretResponse> shareSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @RequestBody ShareSecretRequest request
    ) {
        ShareSecretResponse response = sharingService.shareSecret(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Lista los secretos que han sido compartidos CONMIGO
     *
     * Devuelve secretos cifrados con mi clave pública.
     * El cliente debe descifrarlos con su clave privada.
     */
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<SharedSecretItem>> getSecretsSharedWithMe(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        List<SharedSecretItem> secrets = sharingService.getSecretsSharedWithMe(userId);
        return ResponseEntity.ok(secrets);
    }

    /**
     * Lista los secretos que YO he compartido con otros
     */
    @GetMapping("/shared-by-me")
    public ResponseEntity<List<SharedSecretItem>> getSecretsSharedByMe(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId
    ) {
        List<SharedSecretItem> secrets = sharingService.getSecretsSharedByMe(userId);
        return ResponseEntity.ok(secrets);
    }

    /**
     * Obtiene un secreto compartido específico
     *
     * El cliente descifra con su clave privada.
     *
     * @param userId Usuario que accede
     * @param shareId ID del compartido
     */
    @GetMapping("/shares/{shareId}")
    public ResponseEntity<SharedSecretItem> getSharedSecret(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long shareId
    ) {
        SharedSecretItem item = sharingService.getSharedSecret(userId, shareId);
        return ResponseEntity.ok(item);
    }

    /**
     * Revoca el acceso de un usuario a un secreto
     *
     * Solo el propietario (owner) puede revocar.
     *
     * @param userId ID del propietario
     * @param secretId ID del secreto
     * @param revokeUserId ID del usuario al que se revoca acceso
     */
    @DeleteMapping("/revoke/{secretId}/{revokeUserId}")
    public ResponseEntity<Void> revokeAccess(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long secretId,
        @PathVariable Long revokeUserId
    ) {
        sharingService.revokeAccess(userId, secretId, revokeUserId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lista con quién he compartido un secreto específico
     *
     * Útil para gestionar permisos.
     *
     * @param userId ID del propietario
     * @param secretId ID del secreto
     */
    @GetMapping("/secret/{secretId}/shares")
    public ResponseEntity<List<SharedSecretItem>> getSecretShares(
        @RequestHeader(value = "X-User-Id", defaultValue = "1") Long userId,
        @PathVariable Long secretId
    ) {
        List<SharedSecretItem> shares = sharingService.getSecretShares(userId, secretId);
        return ResponseEntity.ok(shares);
    }
}

