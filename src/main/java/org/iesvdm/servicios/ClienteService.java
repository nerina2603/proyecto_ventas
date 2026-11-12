package org.iesvdm.servicios;

import java.util.List;
import java.util.Optional;

import org.iesvdm.dao.ClienteDAO;
import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.modelo.Cliente;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.stereotype.Service;

@Service
public class ClienteService {

    private final ClienteDAO clienteDAO;
    private final PedidoDAO pedidoDAO;

    public ClienteService(ClienteDAO clienteDAO, PedidoDAO pedidoDAO) {
        this.clienteDAO = clienteDAO;
        this.pedidoDAO = pedidoDAO;
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
