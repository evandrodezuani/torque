package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class TipoServicoRepositoryImpl implements TipoServicoRepository {

	private final TipoServicoJpaRepository jpaRepository;

	@Override
	public void salvar(TipoServico tipoServico) {
		jpaRepository.save(paraEntity(tipoServico));
	}

	@Override
	public Optional<TipoServico> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	public List<TipoServico> listar() {
		return jpaRepository.findAll().stream().map(this::paraDominio).toList();
	}

	@Override
	public boolean existePorDescricao(String descricao) {
		return jpaRepository.existsByDescricao(descricao);
	}

	@Override
	public void remover(UUID id) {
		jpaRepository.deleteById(id);
	}

	private TipoServicoEntity paraEntity(TipoServico tipoServico) {
		var entity = new TipoServicoEntity();
		entity.setId(tipoServico.getId());
		entity.setDescricao(tipoServico.getDescricao());
		return entity;
	}

	private TipoServico paraDominio(TipoServicoEntity entity) {
		return new TipoServico(entity.getId(), entity.getDescricao());
	}

}
