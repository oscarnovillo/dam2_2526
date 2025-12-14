package org.example.spring.domain.model;

public record Usuario(
    Long id,
    String username,
    String password,
    String email,
    String nombre,
    String rol,
    Boolean twoFactorEnabled,
    String twoFactorSecret
) {

    public Usuario(Long id, String username, String password, String email, String nombre, String rol) {
        this(id, username, password, email, nombre, rol, false, "");
    }

    public Usuario set2FA(Boolean enabled, String secret) {
        return new Usuario(this.id, this.username, this.password, this.email, this.nombre, this.rol, enabled, secret);
    }
}
