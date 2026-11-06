package org.iesvdm.dao;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
public class PedidoDAOImpl implements PedidoDAO {

    private final JdbcTemplate jdbcTemplate;

    PedidoDAOImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void create(Pedido pedido) {
        String sqlInsert = """
                INSERT INTO pedido (total, fecha, id_cliente, id_comercial)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        int rows = jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sqlInsert, new String[] { "id" });
            statement.setBigDecimal(1, pedido.getTotal());
            statement.setDate(2, pedido.getFecha() == null ? null : Date.valueOf(pedido.getFecha()));
            statement.setObject(3, pedido.getClienteId(), java.sql.Types.INTEGER);
            statement.setObject(4, pedido.getComercialId(), java.sql.Types.INTEGER);
            return statement;
        }, keyHolder);

        if (keyHolder.getKey() != null) {
            pedido.setId(keyHolder.getKey().intValue());
        }
        log.info("Insertados {} registros de Pedido.", rows);
    }

    @Override
    public List<Pedido> getAll() {
        List<Pedido> pedidos = jdbcTemplate.query(
                "SELECT id, total, fecha, id_cliente, id_comercial FROM pedido",
                (rs, rowNum) -> new Pedido(
                        rs.getInt("id"),
                        rs.getBigDecimal("total"),
                        toLocalDate(rs.getDate("fecha")),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_comercial")));

        log.info("Devueltos {} registros de Pedido.", pedidos.size());
        return pedidos;
    }

    @Override
    public List<PedidoDetalle> getAllWithDetails() {
        List<PedidoDetalle> pedidos = jdbcTemplate.query("""
                SELECT p.id, p.total, p.fecha, p.id_cliente, p.id_comercial,
                       CONCAT_WS(' ', c.nombre, c.apellido1, c.apellido2) AS cliente_nombre,
                       CONCAT_WS(' ', co.nombre, co.apellido1, co.apellido2) AS comercial_nombre
                FROM pedido p
                JOIN cliente c ON p.id_cliente = c.id
                JOIN comercial co ON p.id_comercial = co.id
                ORDER BY p.id
                """,
                (rs, rowNum) -> new PedidoDetalle(
                        rs.getInt("id"),
                        rs.getBigDecimal("total"),
                        toLocalDate(rs.getDate("fecha")),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_comercial"),
                        rs.getString("cliente_nombre"),
                        rs.getString("comercial_nombre")));

        log.info("Devueltos {} registros de Pedido con nombres.", pedidos.size());
        return pedidos;
    }

    @Override
    public Optional<Pedido> find(int id) {
        List<Pedido> pedidos = jdbcTemplate.query(
                "SELECT id, total, fecha, id_cliente, id_comercial FROM pedido WHERE id = ?",
                (rs, rowNum) -> new Pedido(
                        rs.getInt("id"),
                        rs.getBigDecimal("total"),
                        toLocalDate(rs.getDate("fecha")),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_comercial")),
                id);

        return pedidos.stream().findFirst();
    }

    @Override
    public Optional<PedidoDetalle> findDetails(int id) {
        List<PedidoDetalle> pedidos = jdbcTemplate.query("""
                SELECT p.id, p.total, p.fecha, p.id_cliente, p.id_comercial,
                       CONCAT_WS(' ', c.nombre, c.apellido1, c.apellido2) AS cliente_nombre,
                       CONCAT_WS(' ', co.nombre, co.apellido1, co.apellido2) AS comercial_nombre
                FROM pedido p
                JOIN cliente c ON p.id_cliente = c.id
                JOIN comercial co ON p.id_comercial = co.id
                WHERE p.id = ?
                """,
                (rs, rowNum) -> new PedidoDetalle(
                        rs.getInt("id"),
                        rs.getBigDecimal("total"),
                        toLocalDate(rs.getDate("fecha")),
                        rs.getInt("id_cliente"),
                        rs.getInt("id_comercial"),
                        rs.getString("cliente_nombre"),
                        rs.getString("comercial_nombre")),
                id);

        return pedidos.stream().findFirst();
    }

    @Override
    public void update(Pedido pedido) {
        int rows = jdbcTemplate.update("""
                UPDATE pedido
                SET total = ?, fecha = ?, id_cliente = ?, id_comercial = ?
                WHERE id = ?
                """,
                pedido.getTotal(),
                pedido.getFecha() == null ? null : Date.valueOf(pedido.getFecha()),
                pedido.getClienteId(),
                pedido.getComercialId(),
                pedido.getId());

        log.info("Update de Pedido con {} registros actualizados.", rows);
    }

    @Override
    public void delete(int id) {
        int rows = jdbcTemplate.update("DELETE FROM pedido WHERE id = ?", id);
        log.info("Delete de Pedido con {} registros eliminados.", rows);
    }

    private LocalDate toLocalDate(Date fecha) {
        return fecha == null ? null : fecha.toLocalDate();
    }
}