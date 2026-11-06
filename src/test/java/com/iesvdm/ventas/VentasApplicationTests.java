package com.iesvdm.ventas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.iesvdm.controlador.ClienteController;
import org.iesvdm.controlador.ComercialController;
import org.iesvdm.controlador.PedidoController;
import org.iesvdm.modelo.Cliente;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;
import org.iesvdm.servicios.ClienteService;
import org.iesvdm.servicios.ComercialService;
import org.iesvdm.servicios.PedidoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

class VentasApplicationTests {

	private PedidoService pedidoService;
	private ClienteService clienteService;
	private ComercialService comercialService;
	private PedidoController pedidoController;

	@BeforeEach
	void setUp() {
		pedidoService = mock(PedidoService.class);
		clienteService = mock(ClienteService.class);
		comercialService = mock(ComercialService.class);
		pedidoController = new PedidoController(pedidoService, clienteService, comercialService);
		when(clienteService.listAll()).thenReturn(List.of(new Cliente(1, "Ana", "Ruiz", null, "Jaén", 100)));
		when(comercialService.listAll()).thenReturn(List.of(new Comercial(2, "Luis", "Vega", null, 0.1f)));
	}

	@Test
	void createAndUpdateKeepBothSelectedIds() {
		Pedido created = new Pedido(0, new BigDecimal("20.10"), LocalDate.of(2026, 11, 5), 1, 2);
		ExtendedModelMap model = new ExtendedModelMap();

		assertEquals("redirect:/pedidos", pedidoController.submitCrear(created, model));
		verify(pedidoService).create(created);

		Pedido edited = new Pedido(7, new BigDecimal("35.25"), LocalDate.of(2026, 11, 6), 3, 4);
		assertEquals("redirect:/pedidos", pedidoController.submitEditar(7, edited, model));
		assertEquals(7, edited.getId());
		assertEquals(3, edited.getClienteId());
		assertEquals(4, edited.getComercialId());
		verify(pedidoService).update(edited);
	}

	@Test
	void listAndDetailExposeJoinedNames() {
		PedidoDetalle pedido = new PedidoDetalle(1, new BigDecimal("20.10"), LocalDate.of(2026, 11, 5),
				1, 2, "Ana Ruiz", "Luis Vega");
		when(pedidoService.listAllWithDetails()).thenReturn(List.of(pedido));
		when(pedidoService.findDetails(1)).thenReturn(Optional.of(pedido));
		ExtendedModelMap model = new ExtendedModelMap();

		assertEquals("pedidos", pedidoController.listar(model));
		assertEquals("Ana Ruiz", ((PedidoDetalle) ((List<?>) model.get("pedidos")).get(0)).getClienteNombre());

		assertEquals("detalle-pedido", pedidoController.detalle(model, 1));
		assertEquals("Luis Vega", ((PedidoDetalle) model.get("pedido")).getComercialNombre());
	}

	@Test
	void invalidForeignKeysReturnFormWithSelectionsAndError() {
		doThrow(new DataIntegrityViolationException("foreign key"))
				.when(pedidoService).create(any(Pedido.class));
		Pedido pedido = new Pedido(0, new BigDecimal("10.00"), null, 999, 2);
		ExtendedModelMap model = new ExtendedModelMap();

		assertEquals("crear-pedido", pedidoController.submitCrear(pedido, model));
		assertTrue(((String) model.get("error"))
				.contains("Comprueba que el cliente y el comercial seleccionados existen"));
		assertSame(pedido, model.get("pedido"));
		assertEquals(1, ((List<?>) model.get("clientes")).size());
		assertEquals(1, ((List<?>) model.get("comerciales")).size());

		doThrow(new DataIntegrityViolationException("foreign key"))
				.when(pedidoService).update(any(Pedido.class));
		assertEquals("editar-pedido", pedidoController.submitEditar(8, pedido, model));
		assertEquals(8, pedido.getId());
		assertTrue(((String) model.get("error"))
				.contains("Comprueba que el cliente y el comercial seleccionados existen"));
	}

	@Test
	void restrictedPersonDeletesShowClearErrors() {
		ClienteService clientes = mock(ClienteService.class);
		ComercialService comerciales = mock(ComercialService.class);
		doThrow(new DataIntegrityViolationException("foreign key")).when(clientes).delete(1);
		doThrow(new DataIntegrityViolationException("foreign key")).when(comerciales).delete(2);
		ClienteController clienteController = new ClienteController(clientes);
		ComercialController comercialController = new ComercialController(comerciales);
		RedirectAttributesModelMap clienteFlash = new RedirectAttributesModelMap();
		RedirectAttributesModelMap comercialFlash = new RedirectAttributesModelMap();

		assertEquals("redirect:/clientes", clienteController.submitBorrar(1, clienteFlash));
		assertTrue(((String) clienteFlash.getFlashAttributes().get("error")).contains("tiene pedidos asociados"));
		assertEquals("redirect:/comerciales", comercialController.submitBorrar(2, comercialFlash));
		assertTrue(((String) comercialFlash.getFlashAttributes().get("error")).contains("tiene pedidos asociados"));
	}
}
