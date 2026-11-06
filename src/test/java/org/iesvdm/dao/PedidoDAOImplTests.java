package org.iesvdm.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.iesvdm.modelo.Pedido;
import org.iesvdm.modelo.PedidoDetalle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class PedidoDAOImplTests {

    private JdbcTemplate jdbcTemplate;
    private PedidoDAOImpl pedidoDAO;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:pedido;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute(
                "CREATE TABLE cliente (id INT PRIMARY KEY, nombre VARCHAR(100), apellido1 VARCHAR(100), apellido2 VARCHAR(100))");
        jdbcTemplate.execute(
                "CREATE TABLE comercial (id INT PRIMARY KEY, nombre VARCHAR(100), apellido1 VARCHAR(100), apellido2 VARCHAR(100))");
        jdbcTemplate.execute("""
                CREATE TABLE pedido (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    total DECIMAL(10, 2) NOT NULL,
                    fecha DATE,
                    id_cliente INT NOT NULL REFERENCES cliente(id),
                    id_comercial INT NOT NULL REFERENCES comercial(id)
                )
                """);
        jdbcTemplate.update("INSERT INTO cliente VALUES (1, 'Ana', 'Ruiz', NULL)");
        jdbcTemplate.update("INSERT INTO cliente VALUES (3, 'Bea', 'Solís', 'Gómez')");
        jdbcTemplate.update("INSERT INTO comercial VALUES (2, 'Luis', 'Vega', NULL)");
        jdbcTemplate.update("INSERT INTO comercial VALUES (4, 'Marta', 'Ríos', 'Pérez')");
        pedidoDAO = new PedidoDAOImpl(jdbcTemplate);
    }

    @Test
    void createUpdateListAndDetailMapExactAmountsAndNames() {
        Pedido pedido = new Pedido(0, new BigDecimal("20.10"), LocalDate.of(2026, 11, 5), 1, 2);
        pedidoDAO.create(pedido);

        assertTrue(pedido.getId() > 0);
        PedidoDetalle listado = pedidoDAO.getAllWithDetails().get(0);
        assertEquals(new BigDecimal("20.10"), listado.getTotal());
        assertEquals("Ana Ruiz", listado.getClienteNombre());
        assertEquals("Luis Vega", listado.getComercialNombre());

        pedido.setTotal(new BigDecimal("35.25"));
        pedido.setClienteId(3);
        pedido.setComercialId(4);
        pedidoDAO.update(pedido);

        PedidoDetalle detalle = pedidoDAO.findDetails(pedido.getId()).orElseThrow();
        assertEquals(new BigDecimal("35.25"), detalle.getTotal());
        assertEquals("Bea Solís Gómez", detalle.getClienteNombre());
        assertEquals("Marta Ríos Pérez", detalle.getComercialNombre());
        assertEquals(LocalDate.of(2026, 11, 5), detalle.getFecha());
    }

    @Test
    void invalidIdsAndReferencedPersonDeletesRespectForeignKeys() {
        Pedido invalido = new Pedido(0, new BigDecimal("10.00"), null, 999, 2);
        assertThrows(DataIntegrityViolationException.class, () -> pedidoDAO.create(invalido));
        Pedido comercialInvalido = new Pedido(0, new BigDecimal("10.00"), null, 1, 999);
        assertThrows(DataIntegrityViolationException.class, () -> pedidoDAO.create(comercialInvalido));

        Pedido pedido = new Pedido(0, new BigDecimal("10.00"), null, 1, 2);
        pedidoDAO.create(pedido);

        pedido.setClienteId(999);
        assertThrows(DataIntegrityViolationException.class, () -> pedidoDAO.update(pedido));
        assertEquals(1, pedidoDAO.find(pedido.getId()).orElseThrow().getClienteId());
        pedido.setClienteId(1);
        pedido.setComercialId(999);
        assertThrows(DataIntegrityViolationException.class, () -> pedidoDAO.update(pedido));
        assertEquals(2, pedidoDAO.find(pedido.getId()).orElseThrow().getComercialId());

        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM cliente WHERE id = ?", 1));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbcTemplate.update("DELETE FROM comercial WHERE id = ?", 2));
        assertTrue(pedidoDAO.find(pedido.getId()).isPresent());
        assertEquals(1, pedidoDAO.getAllWithDetails().size());
    }
}