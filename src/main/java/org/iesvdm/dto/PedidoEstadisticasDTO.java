package org.iesvdm.dto;

import java.math.BigDecimal;

public record PedidoEstadisticasDTO(
        int cantidadPedidos,
        BigDecimal importeTotal,
        BigDecimal importeMedio,
        BigDecimal importeMaximo,
        BigDecimal importeMinimo) {
}