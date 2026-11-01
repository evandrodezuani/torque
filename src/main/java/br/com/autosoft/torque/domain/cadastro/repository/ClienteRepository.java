package br.com.autosoft.torque.domain.cadastro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;

public interface ClienteRepository {

	void salvar(Cliente cliente);

	Optional<Cliente> buscarPorId(UUID id);

	List<Cliente> listar();

	boolean existePorDocumento(Documento documento);

	boolean existePorEmail(String email);

	Optional<Cliente> buscarPorEmail(String email);

	Optional<Cliente> buscarPorDocumento(Documento documento);

	void remover(UUID id);

}
