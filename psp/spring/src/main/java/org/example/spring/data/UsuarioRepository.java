package org.example.spring.data;


import org.example.spring.domain.model.Usuario;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class UsuarioRepository {
    private final Map<Long, Usuario> usuarios = new ConcurrentHashMap<>();
    private final AtomicLong idCounter = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    public UsuarioRepository(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        // Datos iniciales con contraseñas hasheadas
        saveWithPlainPassword("admin", "admin123", "admin@example.com", "Administrador", "ADMIN");
        saveWithPlainPassword("user", "user123", "user@example.com", "Usuario Test", "USER");
    }

    // Método privado para inicializar usuarios con contraseñas en texto plano
    private void saveWithPlainPassword(String username, String plainPassword, String email, String nombre, String rol) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        Usuario usuario = new Usuario(null, username, hashedPassword, email, nombre, rol);
        save(usuario);
    }

    public Usuario save(Usuario usuario) {
        Long id = usuario.id() != null ? usuario.id() : idCounter.getAndIncrement();
        Usuario usuarioConId = new Usuario(id, usuario.username(), usuario.password(),
                usuario.email(), usuario.nombre(), usuario.rol());
        usuarios.put(id, usuarioConId);
        return usuarioConId;
    }

    public Optional<Usuario> findById(Long id) {
        return Optional.ofNullable(usuarios.get(id));
    }

    public List<Usuario> findAll() {
        return new ArrayList<>(usuarios.values());
    }

    public Optional<Usuario> findByUsername(String username) {

        return usuarios.values().stream()
                .filter(u -> u.username().equals(username))
                .findFirst();
    }

    public void deleteById(Long id) {
        usuarios.remove(id);
    }

    public boolean existsByUsername(String username) {
        return usuarios.values().stream()
                .anyMatch(u -> u.username().equals(username));
    }
}
