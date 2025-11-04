package org.example.spring.ui.dto;

public record RegistroRequest(
    String username,
    String password,
    String email,
    String nombre
) {}
