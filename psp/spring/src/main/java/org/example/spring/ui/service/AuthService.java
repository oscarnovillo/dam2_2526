package org.example.spring.ui.service;

import jakarta.servlet.http.HttpSession;
import org.example.spring.data.UsuarioRepository;
import org.example.spring.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<Usuario> login(String username, String password, HttpSession session) {
        Optional<Usuario> usuario = usuarioRepository.findByUsername(username);

        // Verificar la contraseña usando BCrypt
        if (usuario.isPresent() && passwordEncoder.matches(password, usuario.get().password())) {
            // Guardamos el usuario en la sesión
            session.setAttribute("usuario", usuario);
            return usuario;
        }

        return Optional.empty();
    }

    public void logout(HttpSession session) {
        session.invalidate();
    }

    public boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("usuarioId") != null;
    }

    public Long getUsuarioIdFromSession(HttpSession session) {
        return (Long) session.getAttribute("usuarioId");
    }

    public String getRolFromSession(HttpSession session) {
        return (String) session.getAttribute("rol");
    }

    public boolean isAdmin(HttpSession session) {
        String rol = getRolFromSession(session);
        return "ADMIN".equals(rol);
    }
}
