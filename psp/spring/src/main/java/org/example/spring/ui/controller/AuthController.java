package org.example.spring.ui.controller;

import jakarta.servlet.http.HttpSession;

import org.example.spring.data.UsuarioRepository;
import org.example.spring.domain.model.Usuario;
import org.example.spring.ui.dto.LoginRequest;
import org.example.spring.ui.dto.RegistroRequest;
import org.example.spring.ui.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.authService = authService;
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
}
