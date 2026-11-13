package org.iesvdm.dao;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.iesvdm.modelo.ClienteComercial;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

class ClienteComercialDAOImplTests {

    private JdbcTemplate jdbcTemplate;
    private ClienteComercialDAOImpl clienteComercialDAO;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:h2:mem:cliente_comercial;MODE=MySQL;DB_CLOSE_DELAY=-1", "sa", "");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("DROP ALL OBJECTS");
        jdbcTemplate.execute("CREATE TABLE cliente (id INT PRIMARY KEY)");
        jdbcTemplate.execute("""
                CREATE TABLE comercial (
                    id INT PRIMARY KEY,
                    nombre VARCHAR(100),
                    apellido1 VARCHAR(100),
                    apellido2 VARCHAR(100)
                )
                """);
        jdbcTemplate.execute("""
                CREATE TABLE cliente_has_comercial (
                    cliente_id INT NOT NULL REFERENCES cliente(id),
                    comercial_id INT NOT NULL REFERENCES comercial(id),
                    fecha DATE NOT NULL,
                    prioridad INT NOT NULL,
                    PRIMARY KEY (cliente_id, comercial_id)
                )
                """);
        jdbcTemplate.update("INSERT INTO cliente VALUES (1)");
        jdbcTemplate.update("INSERT INTO comercial VALUES (2, 'Ana', 'Ruiz', NULL)");
        clienteComercialDAO = new ClienteComercialDAOImpl(jdbcTemplate);
    }

    @Test
    void addsListsAndRemovesCommercialForClient() {
        clienteComercialDAO.add(1, 2, LocalDate.of(2026, 11, 20), 1);

        var vinculados = clienteComercialDAO.findByClienteId(1);
        assertEquals(1, vinculados.size());
        assertEquals(2, vinculados.get(0).getComercialId());
        assertEquals("Ana Ruiz", vinculados.get(0).getComercialNombre());
        assertEquals(LocalDate.of(2026, 11, 20), vinculados.get(0).getFecha());
        assertEquals(1, vinculados.get(0).getPrioridad());

        clienteComercialDAO.delete(1, 2);

        assertEquals(0, clienteComercialDAO.findByClienteId(1).size());
    }
}