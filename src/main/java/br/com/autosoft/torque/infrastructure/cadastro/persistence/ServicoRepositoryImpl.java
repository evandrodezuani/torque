package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ServicoRepositoryImpl implements ServicoRepository {

	private final ServicoJpaRepository jpaRepository;

	@Override
	public void salvar(Servico servico) {
		jpaRepository.save(paraEntity(servico));
	}

	@Override
	public Optional<Servico> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	public List<Servico> listar() {
		return jpaRepository.findAll().stream().map(this::paraDominio).toList();
	}

	@Override
	public boolean existePorTipoServicoETipoVeiculo(UUID tipoServicoId, TipoVeiculo tipoVeiculo) {
		return jpaRepository.existsByTipoServicoIdAndTipoVeiculo(tipoServicoId, tipoVeiculo);
	}

	@Override
	public boolean existePorTipoServico(UUID tipoServicoId) {
		return jpaRepository.existsByTipoServicoId(tipoServicoId);
	}

	@Override
	public void remover(UUID id) {
		jpaRepository.deleteById(id);
	}

	private ServicoEntity paraEntity(Servico servico) {
		var entity = new ServicoEntity();
		entity.setId(servico.getId());
		entity.setTipoServicoId(servico.getTipoServicoId());
		entity.setTipoVeiculo(servico.getTipoVeiculo());
		entity.setPreco(servico.getPreco().valor());
		entity.setAtivo(servico.isAtivo());
		return entity;
	}

	private Servico paraDominio(ServicoEntity entity) {
		return new Servico(entity.getId(), entity.getTipoServicoId(), entity.getTipoVeiculo(),
				new Dinheiro(entity.getPreco()), entity.isAtivo());
	}

}
