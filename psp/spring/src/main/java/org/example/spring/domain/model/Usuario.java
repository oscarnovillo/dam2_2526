package org.example.spring.domain.model;

public record Usuario(
    Long id,
    String username,
    String password,
    String email,
    String nombre,
    String rol
) {}
