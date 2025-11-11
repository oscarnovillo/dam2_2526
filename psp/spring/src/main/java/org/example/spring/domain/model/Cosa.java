package org.example.spring.domain.model;

public record Cosa(
        int id,
        int userId,
        String nombre,
        String descripcion
) {
}
