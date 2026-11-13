package org.iesvdm.servicios;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;

import org.iesvdm.dao.ComercialDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.dto.ClienteTotalPedidosDTO;
import org.iesvdm.dto.PedidoEstadisticasDTO;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.stereotype.Service;

@Service
public class ComercialService {

	private final ComercialDAO comercialDAO;
	private final PedidoDAO pedidoDAO;

	public ComercialService(ComercialDAO comercialDAO, PedidoDAO pedidoDAO) {
		this.comercialDAO = comercialDAO;
		this.pedidoDAO = pedidoDAO;
	}

	public void create(Comercial comercial) {
		comercialDAO.create(comercial);
	}

	public List<Comercial> listAll() {
		return comercialDAO.getAll();
	}

	public Optional<Comercial> find(int id) {
		return comercialDAO.find(id);
	}

	public List<PedidoDetalle> listPedidosByComercial(int comercialId) {
		return pedidoDAO.findByComercialId(comercialId);
	}

	public PedidoEstadisticasDTO calcularEstadisticasPedidos(List<PedidoDetalle> pedidos) {
		BigDecimal importeTotal = BigDecimal.ZERO;
		BigDecimal importeMaximo = null;
		BigDecimal importeMinimo = null;
		for (PedidoDetalle pedido : pedidos) {
			BigDecimal importe = pedido.getTotal();
			importeTotal = importeTotal.add(importe);
			importeMaximo = importeMaximo == null ? importe : importeMaximo.max(importe);
			importeMinimo = importeMinimo == null ? importe : importeMinimo.min(importe);
		}
		int cantidadPedidos = pedidos.size();
		BigDecimal importeMedio = cantidadPedidos == 0
				? BigDecimal.ZERO
				: importeTotal.divide(BigDecimal.valueOf(cantidadPedidos), 2, RoundingMode.HALF_UP);

		return new PedidoEstadisticasDTO(cantidadPedidos, importeTotal, importeMedio, importeMaximo, importeMinimo);
	}

	public List<ClienteTotalPedidosDTO> listarClientesPorTotalPedidos(List<PedidoDetalle> pedidosComercial) {
		Set<Integer> clientesDelComercial = new HashSet<>();
		for (PedidoDetalle pedido : pedidosComercial) {
			clientesDelComercial.add(pedido.getClienteId());
		}
		List<PedidoDetalle> todosLosPedidos = pedidoDAO.getAllWithDetails();
		Map<Integer, BigDecimal> totalesPorCliente = new HashMap<>();
		Map<Integer, String> nombresPorCliente = new HashMap<>();
		for (PedidoDetalle pedido : todosLosPedidos) {
			Integer clienteId = pedido.getClienteId();
			BigDecimal totalActual = totalesPorCliente.getOrDefault(clienteId, BigDecimal.ZERO);
			totalesPorCliente.put(clienteId, totalActual.add(pedido.getTotal()));
			nombresPorCliente.putIfAbsent(clienteId, pedido.getClienteNombre());
		}

		List<ClienteTotalPedidosDTO> clientesOrdenados = new ArrayList<>();
		for (Integer clienteId : clientesDelComercial) {
			clientesOrdenados.add(new ClienteTotalPedidosDTO(clienteId, nombresPorCliente.get(clienteId),
					totalesPorCliente.getOrDefault(clienteId, BigDecimal.ZERO)));
		}
		clientesOrdenados.sort((cliente1, cliente2) -> {
			int comparacionImportes = cliente2.importeTotal().compareTo(cliente1.importeTotal());
			return comparacionImportes != 0 ? comparacionImportes
					: cliente1.nombre().compareToIgnoreCase(cliente2.nombre());
		});
		return clientesOrdenados;
	}

	public void update(Comercial comercial) {
		comercialDAO.update(comercial);
	}

	public void delete(int id) {
		comercialDAO.delete(id);
	}
}