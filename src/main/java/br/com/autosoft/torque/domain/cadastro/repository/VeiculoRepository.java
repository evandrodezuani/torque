package br.com.autosoft.torque.domain.cadastro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;

public interface VeiculoRepository {

	void salvar(Veiculo veiculo);

	Optional<Veiculo> buscarPorId(UUID id);

	List<Veiculo> listarPorCliente(UUID clienteId);

	boolean existePorPlaca(Placa placa);

	void remover(UUID id);

	void removerPorCliente(UUID clienteId);

}
