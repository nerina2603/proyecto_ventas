package org.iesvdm.dao;

import java.util.List;
import java.util.Optional;

import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;

public interface PedidoDAO {
    void create(Pedido pedido);

    List<Pedido> getAll();

    List<PedidoDetalle> getAllWithDetails();

    Optional<Pedido> find(int id);

    Optional<PedidoDetalle> findDetails(int id);

    void update(Pedido pedido);

    void delete(int id);
}