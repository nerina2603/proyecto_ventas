package org.iesvdm.servicios;

import java.util.List;
import java.util.Optional;

import org.iesvdm.dao.PedidoDAO;
import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.stereotype.Service;

@Service
public class PedidoService {

    private final PedidoDAO pedidoDAO;

    public PedidoService(PedidoDAO pedidoDAO) {
        this.pedidoDAO = pedidoDAO;
    }

    public List<PedidoDetalle> listAllWithDetails() {
        return pedidoDAO.getAllWithDetails();
    }

    public Optional<PedidoDetalle> findDetails(int id) {
        return pedidoDAO.findDetails(id);
    }

    public Optional<Pedido> find(int id) {
        return pedidoDAO.find(id);
    }

    public void create(Pedido pedido) {
        pedidoDAO.create(pedido);
    }

    public void update(Pedido pedido) {
        pedidoDAO.update(pedido);
    }

    public void delete(int id) {
        pedidoDAO.delete(id);
    }
}