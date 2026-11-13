package org.iesvdm.servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.iesvdm.dao.ComercialDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.dto.ClienteTotalPedidosDTO;
import org.iesvdm.dto.PedidoEstadisticasDTO;
import org.iesvdm.modelo.PedidoDetalle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComercialServiceTests {

    private PedidoDAO pedidoDAO;
    private ComercialService comercialService;

    @BeforeEach
    void setUp() {
        pedidoDAO = mock(PedidoDAO.class);
        comercialService = new ComercialService(mock(ComercialDAO.class), pedidoDAO);
    }

    @Test
    void calculatesTotalAverageAndExtremes() {
        List<PedidoDetalle> pedidos = List.of(
                pedido(1, "10.00", 1, 2, "Ana"),
                pedido(2, "20.00", 1, 2, "Ana"));

        PedidoEstadisticasDTO estadisticas = comercialService.calcularEstadisticasPedidos(pedidos);

        assertEquals(2, estadisticas.cantidadPedidos());
        assertEquals(new BigDecimal("30.00"), estadisticas.importeTotal());
        assertEquals(new BigDecimal("15.00"), estadisticas.importeMedio());
        assertEquals(new BigDecimal("20.00"), estadisticas.importeMaximo());
        assertEquals(new BigDecimal("10.00"), estadisticas.importeMinimo());
    }

    @Test
    void emptyOrderStatisticsHaveZeroTotalsAndNoExtremes() {
        PedidoEstadisticasDTO estadisticas = comercialService.calcularEstadisticasPedidos(List.of());

        assertEquals(BigDecimal.ZERO, estadisticas.importeTotal());
        assertEquals(BigDecimal.ZERO, estadisticas.importeMedio());
        assertNull(estadisticas.importeMaximo());
        assertNull(estadisticas.importeMinimo());
    }

    @Test
    void ranksCommercialsClientsByTheirGlobalOrderAmounts() {
        PedidoDetalle pedidoComercialAna = pedido(1, "100.00", 1, 2, "Ana Ruiz");
        PedidoDetalle pedidoOtroComercialAna = pedido(2, "500.00", 1, 4, "Ana Ruiz");
        PedidoDetalle pedidoComercialBea = pedido(3, "200.00", 3, 2, "Bea Solís");
        PedidoDetalle pedidoOtroComercialCliente = pedido(4, "900.00", 5, 4, "Carla Díaz");
        when(pedidoDAO.getAllWithDetails()).thenReturn(List.of(
                pedidoComercialAna, pedidoOtroComercialAna, pedidoComercialBea, pedidoOtroComercialCliente));

        List<ClienteTotalPedidosDTO> ranking = comercialService.listarClientesPorTotalPedidos(
                List.of(pedidoComercialAna, pedidoComercialBea));

        assertEquals(List.of(
                new ClienteTotalPedidosDTO(1, "Ana Ruiz", new BigDecimal("600.00")),
                new ClienteTotalPedidosDTO(3, "Bea Solís", new BigDecimal("200.00"))), ranking);
    }

    private static PedidoDetalle pedido(int id, String total, int clienteId, int comercialId, String clienteNombre) {
        return new PedidoDetalle(id, new BigDecimal(total), LocalDate.of(2026, 11, 1), clienteId, comercialId,
                clienteNombre, "Comercial");
    }
}