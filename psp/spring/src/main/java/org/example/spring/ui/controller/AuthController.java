package org.example.spring.ui.controller;

import dev.samstevens.totp.exceptions.QrGenerationException;
import jakarta.servlet.http.HttpSession;

import org.example.spring.data.UsuarioRepository;
import org.example.spring.domain.model.Usuario;
import org.example.spring.ui.dto.*;
import org.example.spring.ui.service.AuthService;
import org.example.spring.ui.service.TotpService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final TotpService totpService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, TotpService totpService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
        this.totpService = totpService;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        Optional<Usuario> usuario = authService.login(request.username(), request.password(), session);


        if (usuario.isPresent()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Login exitoso");
            response.put("usuario", Map.of(
                "id", usuario.get().id(),
                "username", usuario.get().username(),
                "email", usuario.get().email(),
                "nombre", usuario.get().nombre(),
                "rol", usuario.get().rol()
            ));
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(Map.of("success", false, "message", "Credenciales inválidas"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegistroRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            return ResponseEntity.badRequest()
                .body(Map.of("success", false, "message", "El username ya existe"));
        }

        // Hashear la contraseña antes de guardar
        String hashedPassword = passwordEncoder.encode(request.password());

        Usuario nuevoUsuario = new Usuario(
            null,
            request.username(),
            hashedPassword,
            request.email(),
            request.nombre(),
            "USER"
        );

        // Generar código de activación aleatorio
        String codigoActivacion = UUID.randomUUID().toString();

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(Map.of(
                "success", true,
                "message", "Usuario registrado exitosamente",
                "usuario", Map.of(
                    "id", usuarioGuardado.id(),
                    "username", usuarioGuardado.username(),
                    "email", usuarioGuardado.email(),
                    "nombre", usuarioGuardado.nombre()
                )
            ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(Map.of("success", true, "message", "Logout exitoso"));
    }

    @GetMapping("/session")
    public ResponseEntity<?> checkSession(HttpSession session) {
        if (authService.isAuthenticated(session)) {
            Long usuarioId = authService.getUsuarioIdFromSession(session);
            Optional<Usuario> usuario = usuarioRepository.findById(usuarioId);

            if (usuario.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "authenticated", true,
                    "usuario", Map.of(
                        "id", usuario.get().id(),
                        "username", usuario.get().username(),
                        "email", usuario.get().email(),
                        "nombre", usuario.get().nombre(),
                        "rol", usuario.get().rol()
                    )
                ));
            }
        }

        return ResponseEntity.ok(Map.of("authenticated", false));
    }



    // ============== ENDPOINTS 2FA (TOTP) ==============

    @PostMapping("/2fa/enable")
    public ResponseEntity<?> enable2FA(HttpSession session) {
        // Verificar que el usuario esté autenticado
        if (!authService.isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }

        Long usuarioId = authService.getUsuarioIdFromSession(session);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        try {
            // Generar secreto TOTP
            String secret = totpService.generateSecret();

            // Generar QR code
            String qrCodeUri = totpService.generateQrCodeImageUri(
                    secret,
                    usuario.username(),
                    "MiAplicacion" // Nombre de tu app que aparecerá en Google Authenticator
            );

            // Guardar el secreto temporalmente (aún no activado)
            usuario = usuario.set2FA(false,secret); // Aún no activado hasta confirmar
            usuarioRepository.save(usuario);

            Enable2FAResponse response = new Enable2FAResponse(
                    secret,
                    qrCodeUri,
                    "Escanea el código QR con tu aplicación autenticadora (Google Authenticator, Authy, etc.) y confirma con un código"
            );

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", response
            ));
        } catch (QrGenerationException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error generando código QR: " + e.getMessage()));
        }
    }

    @PostMapping("/2fa/confirm")
    public ResponseEntity<?> confirm2FA(@RequestBody Confirm2FARequest request, HttpSession session) {
        // Verificar que el usuario esté autenticado
        if (!authService.isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }

        Long usuarioId = authService.getUsuarioIdFromSession(session);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que tiene un secreto pendiente
        if (usuario.twoFactorSecret() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "No hay un proceso de habilitación 2FA pendiente"));
        }

        // Verificar el código TOTP
        boolean isValid = totpService.verifyCode(usuario.twoFactorSecret(), request.code());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Código inválido. Verifica que tu app esté sincronizada correctamente."));
        }

        // Activar 2FA
        usuario = usuario.set2FA(true,usuario.twoFactorSecret());
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Autenticación de dos factores activada correctamente"
        ));
    }

    @PostMapping("/2fa/disable")
    public ResponseEntity<?> disable2FA(HttpSession session) {
        // Verificar que el usuario esté autenticado
        if (!authService.isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }

        Long usuarioId = authService.getUsuarioIdFromSession(session);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get().set2FA(false,null);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Autenticación de dos factores desactivada"
        ));
    }

    @PostMapping("/2fa/verify")
    public ResponseEntity<?> verify2FA(@RequestBody Verify2FARequest request, HttpSession session) {
        // Verificar que hay un login pendiente de 2FA
        String pendingUsername = (String) session.getAttribute("pendingTwoFactorUsername");

        if (pendingUsername == null || !pendingUsername.equals(request.username())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No hay un login pendiente de verificación 2FA"));
        }

        Optional<Usuario> usuarioOpt = usuarioRepository.findByUsername(request.username());

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        // Verificar que tiene 2FA habilitado
        if (!Boolean.TRUE.equals(usuario.twoFactorEnabled()) || usuario.twoFactorSecret() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("success", false, "message", "El usuario no tiene 2FA habilitado"));
        }

        // Verificar el código TOTP
        boolean isValid = totpService.verifyCode(usuario.twoFactorSecret(), request.code());

        if (!isValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Código de verificación inválido"));
        }

        // Código válido - completar el login
        session.removeAttribute("pendingTwoFactorUsername");
        session.setAttribute("usuarioId", usuario.id());
        session.setAttribute("username", usuario.username());
        session.setAttribute("rol", usuario.rol());

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Login completado exitosamente",
                "usuario", usuario
        ));
    }


    @GetMapping("/2fa/status")
    public ResponseEntity<?> get2FAStatus(HttpSession session) {
        if (!authService.isAuthenticated(session)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "No autenticado"));
        }

        Long usuarioId = authService.getUsuarioIdFromSession(session);
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);

        if (usuarioOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", "Usuario no encontrado"));
        }

        Usuario usuario = usuarioOpt.get();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "twoFactorEnabled", usuario.twoFactorEnabled()
        ));
    }

}
