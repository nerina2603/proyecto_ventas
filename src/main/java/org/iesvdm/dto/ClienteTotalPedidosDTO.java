package org.iesvdm.dto;

import java.math.BigDecimal;

public record ClienteTotalPedidosDTO(int clienteId, String nombre, BigDecimal importeTotal) {
}