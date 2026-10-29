package br.com.autosoft.torque.domain.cadastro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;

public interface ServicoRepository {

	void salvar(Servico servico);

	Optional<Servico> buscarPorId(UUID id);

	List<Servico> listar();

	boolean existePorTipoServicoETipoVeiculo(UUID tipoServicoId, TipoVeiculo tipoVeiculo);

	boolean existePorTipoServico(UUID tipoServicoId);

	void remover(UUID id);

}
