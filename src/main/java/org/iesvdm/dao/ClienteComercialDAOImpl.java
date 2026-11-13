package org.iesvdm.dao;

import java.sql.Date;
import java.util.List;

import org.iesvdm.modelo.ClienteComercial;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ClienteComercialDAOImpl implements ClienteComercialDAO {

    private final JdbcTemplate jdbcTemplate;

    ClienteComercialDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void add(int clienteId, int comercialId, java.time.LocalDate fecha, int prioridad) {
        jdbcTemplate.update("""
                INSERT INTO cliente_has_comercial (cliente_id, comercial_id, fecha, prioridad)
                VALUES (?, ?, ?, ?)
                """,
                clienteId,
                comercialId,
                Date.valueOf(fecha),
                prioridad);
    }

    @Override
    public List<ClienteComercial> findByClienteId(int clienteId) {
        return jdbcTemplate.query("""
                SELECT chc.cliente_id, chc.comercial_id,
                       CONCAT_WS(' ', c.nombre, c.apellido1, c.apellido2) AS comercial_nombre,
                       chc.fecha, chc.prioridad
                FROM cliente_has_comercial chc
                JOIN comercial c ON c.id = chc.comercial_id
                WHERE chc.cliente_id = ?
                ORDER BY chc.prioridad, chc.comercial_id
                """,
                (rs, rowNum) -> new ClienteComercial(
                        rs.getInt("cliente_id"),
                        rs.getInt("comercial_id"),
                        rs.getString("comercial_nombre"),
                        rs.getDate("fecha") == null ? null : rs.getDate("fecha").toLocalDate(),
                        rs.getObject("prioridad", Integer.class)),
                clienteId);
    }

    @Override
    public void delete(int clienteId, int comercialId) {
        jdbcTemplate.update(
                "DELETE FROM cliente_has_comercial WHERE cliente_id = ? AND comercial_id = ?",
                clienteId,
                comercialId);
    }
}