package org.example.spring.ui.dto;

public record ItemPedidoRequest(
    Long productoId,
    Integer cantidad
) {}
