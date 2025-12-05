package org.example.spring.ui.dto;

public record LoginRequest(
    String username,
    String password
) {}
