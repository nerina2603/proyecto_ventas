package org.iesvdm.servicios;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.iesvdm.dao.ClienteDAO;
import org.iesvdm.dao.ClienteComercialDAO;
import org.iesvdm.dao.ComercialDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.modelo.Cliente;
import org.iesvdm.modelo.ClienteComercial;
import org.iesvdm.modelo.Comercial;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final PedidoDAO pedidoDAO;
    private final ComercialDAO comercialDAO;
    private final ClienteComercialDAO clienteComercialDAO;

    public ClienteService(ClienteDAO clienteDAO, PedidoDAO pedidoDAO, ComercialDAO comercialDAO,
            ClienteComercialDAO clienteComercialDAO) {
        this.clienteDAO = clienteDAO;
        this.pedidoDAO = pedidoDAO;
        this.comercialDAO = comercialDAO;
        this.clienteComercialDAO = clienteComercialDAO;
    }

    public List<Cliente> listAll() {
        return clienteDAO.getAll();
    }

    public List<Cliente> listByCity(String ciudad) {
        if (ciudad == null || ciudad.isBlank()) {
            return listAll();
        }
        return clienteDAO.findByCity(ciudad.trim());
    }

    public Optional<Cliente> find(int id) {
        return clienteDAO.find(id);
    }

    public List<PedidoDetalle> listPedidosByCliente(int clienteId) {
        return pedidoDAO.findByClienteId(clienteId);
    }

    public List<Comercial> listComercialesDisponibles(int clienteId) {
        Set<Integer> vinculados = clienteComercialDAO.findByClienteId(clienteId).stream()
                .map(vinculo -> vinculo.getComercialId())
                .collect(Collectors.toSet());
        return comercialDAO.getAll().stream()
                .filter(comercial -> !vinculados.contains(comercial.getId()))
                .toList();
    }

    public List<ClienteComercial> listComercialesByCliente(int clienteId) {
        return clienteComercialDAO.findByClienteId(clienteId);
    }

    public void addComercialToCliente(int clienteId, int comercialId, java.time.LocalDate fecha, int prioridad) {
        clienteComercialDAO.add(clienteId, comercialId, fecha, prioridad);
    }

    public void removeComercialFromCliente(int clienteId, int comercialId) {
        clienteComercialDAO.delete(clienteId, comercialId);
    }

    public void create(Cliente cliente) {
        clienteDAO.create(cliente);
    }

    public void update(Cliente cliente) {
        clienteDAO.update(cliente);
    }

    public void delete(int id) {
        clienteDAO.delete(id);
    }
}
