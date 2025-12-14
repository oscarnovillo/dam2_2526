package org.example.spring.ui.service;

import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.qr.QrGenerator;
import dev.samstevens.totp.qr.ZxingPngQrGenerator;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.springframework.stereotype.Service;

import static dev.samstevens.totp.util.Utils.getDataUriForImage;

/**
 * Servicio para gestionar TOTP (Time-based One-Time Password) para 2FA.
 *
 * TOTP genera códigos de 6 dígitos que cambian cada 30 segundos.
 * Se basa en un secreto compartido entre servidor y cliente (app autenticadora).
 */
@Service
public class TotpService {

    private final DefaultSecretGenerator secretGenerator = new DefaultSecretGenerator();
    private final QrGenerator qrGenerator = new ZxingPngQrGenerator();
    private final TimeProvider timeProvider = new SystemTimeProvider();
    private final CodeGenerator codeGenerator = new DefaultCodeGenerator();
    private final CodeVerifier verifier;

    public TotpService() {
        // Configurar el verificador con HashingAlgorithm SHA1 y 30 segundos de ventana
        DefaultCodeVerifier defaultVerifier = new DefaultCodeVerifier(codeGenerator, timeProvider);
        // Permitir 1 periodo antes y después (tolerancia de 30 segundos en cada dirección)
        defaultVerifier.setTimePeriod(30);
        defaultVerifier.setAllowedTimePeriodDiscrepancy(1);
        this.verifier = defaultVerifier;
    }

    /**
     * Genera un secreto aleatorio para TOTP.
     * Este secreto debe guardarse en la base de datos del usuario.
     *
     * @return El secreto en formato Base32
     */
    public String generateSecret() {
        return secretGenerator.generate();
    }

    /**
     * Genera un código QR en formato Data URI que el usuario puede escanear
     * con una app autenticadora (Google Authenticator, Authy, etc.).
     *
     * @param secret El secreto TOTP del usuario
     * @param username El nombre de usuario (se mostrará en la app)
     * @param issuer El nombre de la aplicación (ej: "MiTienda")
     * @return URI de imagen PNG con el código QR en formato base64
     * @throws QrGenerationException Si hay un error generando el QR
     */
    public String generateQrCodeImageUri(String secret, String username, String issuer) throws QrGenerationException {
        QrData data = new QrData.Builder()
                .label(username)
                .secret(secret)
                .issuer(issuer)
                .algorithm(HashingAlgorithm.SHA1)
                .digits(6)
                .period(30)
                .build();

        byte[] imageData = qrGenerator.generate(data);
        return getDataUriForImage(imageData, qrGenerator.getImageMimeType());
    }

    /**
     * Verifica si un código TOTP proporcionado por el usuario es válido.
     *
     * @param secret El secreto TOTP almacenado del usuario
     * @param code El código de 6 dígitos proporcionado por el usuario
     * @return true si el código es válido, false si no lo es
     */
    public boolean verifyCode(String secret, String code) {
        return verifier.isValidCode(secret, code);
    }

    /**
     * Genera el código TOTP actual para un secreto dado.
     * Útil para testing o debugging.
     *
     * @param secret El secreto TOTP
     * @return El código TOTP actual de 6 dígitos
     * @throws CodeGenerationException Si hay un error generando el código
     */
    public String getCurrentCode(String secret) throws CodeGenerationException {
        long currentBucket = Math.floorDiv(timeProvider.getTime(), 30);
        return codeGenerator.generate(secret, currentBucket);
    }
}

