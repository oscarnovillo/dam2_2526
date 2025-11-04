package org.example.spring.ui.dto;

import java.util.List;

public record CrearPedidoRequest(
    List<ItemPedidoRequest> items
) {}
