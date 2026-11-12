package org.iesvdm.dao;

import java.util.List;
import java.util.Optional;
import org.iesvdm.modelo.Cliente;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteDAO {
	public void create(Cliente cliente);

	public List<Cliente> getAll();

	public List<Cliente> findByCity(String ciudad);

	public Optional<Cliente> find(int id);

	public void update(Cliente cliente);

	public void delete(int id);
}
