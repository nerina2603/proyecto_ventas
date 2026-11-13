package org.iesvdm.dao;

import java.time.LocalDate;
import java.util.List;

import org.iesvdm.modelo.ClienteComercial;

public interface ClienteComercialDAO {
    void add(int clienteId, int comercialId, LocalDate fecha, int prioridad);

    List<ClienteComercial> findByClienteId(int clienteId);

    void delete(int clienteId, int comercialId);
}