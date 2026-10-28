package br.com.autosoft.torque.domain.cadastro.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;

public interface TipoServicoRepository {

	void salvar(TipoServico tipoServico);

	Optional<TipoServico> buscarPorId(UUID id);

	List<TipoServico> listar();

	boolean existePorDescricao(String descricao);

	void remover(UUID id);

}
