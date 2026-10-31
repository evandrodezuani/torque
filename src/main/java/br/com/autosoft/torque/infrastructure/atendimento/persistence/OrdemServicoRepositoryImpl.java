package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import br.com.autosoft.torque.domain.atendimento.model.ItemPeca;
import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.atendimento.model.Orcamento;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
class OrdemServicoRepositoryImpl implements OrdemServicoRepository {

	private final OrdemServicoJpaRepository jpaRepository;

	@Override
	public void salvar(OrdemServico ordemServico) {
		jpaRepository.save(paraEntity(ordemServico));
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrdemServico> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<OrdemServico> buscarPorCodigoAcompanhamento(UUID codigoAcompanhamento) {
		return jpaRepository.findByCodigoAcompanhamento(codigoAcompanhamento).map(this::paraDominio);
	}

	@Override
	@Transactional(readOnly = true)
	public List<OrdemServico> listar(StatusOS status, boolean maisAntigasPrimeiro) {
		var ordem = Sort.by(maisAntigasPrimeiro ? Sort.Direction.ASC : Sort.Direction.DESC, "abertaEm");
		var entities = status == null ? jpaRepository.findAll(ordem) : jpaRepository.findByStatus(status, ordem);
		return entities.stream().map(this::paraDominio).toList();
	}

	@Override
	public boolean existeEmAndamentoPorCliente(UUID clienteId) {
		return jpaRepository.existsByClienteIdAndStatusNot(clienteId, StatusOS.ENTREGUE);
	}

	@Override
	public boolean existeEmAndamentoPorVeiculo(UUID veiculoId) {
		return jpaRepository.existsByVeiculoIdAndStatusNot(veiculoId, StatusOS.ENTREGUE);
	}

	@Override
	public boolean existeComServico(UUID servicoId) {
		return jpaRepository.existsByOrcamentosItensServicoServicoId(servicoId);
	}

	@Override
	public boolean existeComItemEstoque(UUID itemEstoqueId) {
		return jpaRepository.existsByOrcamentosItensPecaItemEstoqueId(itemEstoqueId);
	}

	@Override
	public void removerPorCliente(UUID clienteId) {
		jpaRepository.deleteByClienteId(clienteId);
	}

	@Override
	public void removerPorVeiculo(UUID veiculoId) {
		jpaRepository.deleteByVeiculoId(veiculoId);
	}

	private OrdemServicoEntity paraEntity(OrdemServico os) {
		var entity = new OrdemServicoEntity();
		entity.setId(os.getId());
		entity.setCodigoAcompanhamento(os.getCodigoAcompanhamento());
		entity.setClienteId(os.getClienteId());
		entity.setVeiculoId(os.getVeiculoId());
		entity.setStatus(os.getStatus());
		entity.setAbertaEm(os.getAbertaEm());
		entity.setExecucaoIniciadaEm(os.getExecucaoIniciadaEm());
		entity.setExecucaoConcluidaEm(os.getExecucaoConcluidaEm());
		entity.setEntregueEm(os.getEntregueEm());
		for (var orcamento : os.getOrcamentos()) {
			var orcamentoEntity = new OrcamentoEntity();
			orcamentoEntity.setId(orcamento.getId());
			orcamentoEntity.setOrdemServico(entity);
			orcamentoEntity.setNumero(orcamento.getNumero());
			orcamentoEntity.setSituacao(orcamento.getSituacao());
			orcamentoEntity.setEnviadoEm(orcamento.getEnviadoEm());
			orcamentoEntity.setRespondidoEm(orcamento.getRespondidoEm());
			for (var item : orcamento.getItensServico()) {
				var itemEntity = new ItemServicoEmbeddable();
				itemEntity.setServicoId(item.servicoId());
				itemEntity.setDescricao(item.descricao());
				itemEntity.setPrecoUnitario(item.precoUnitario().valor());
				itemEntity.setQuantidade(item.quantidade());
				orcamentoEntity.getItensServico().add(itemEntity);
			}
			for (var item : orcamento.getItensPeca()) {
				var itemEntity = new ItemPecaEmbeddable();
				itemEntity.setItemEstoqueId(item.itemEstoqueId());
				itemEntity.setDescricao(item.descricao());
				itemEntity.setPrecoUnitario(item.precoUnitario().valor());
				itemEntity.setQuantidade(item.quantidade());
				orcamentoEntity.getItensPeca().add(itemEntity);
			}
			entity.getOrcamentos().add(orcamentoEntity);
		}
		return entity;
	}

	private OrdemServico paraDominio(OrdemServicoEntity entity) {
		var orcamentos = entity.getOrcamentos().stream().map(this::paraDominio).toList();
		return new OrdemServico(entity.getId(), entity.getCodigoAcompanhamento(), entity.getClienteId(),
				entity.getVeiculoId(), entity.getAbertaEm(), entity.getStatus(), entity.getExecucaoIniciadaEm(),
				entity.getExecucaoConcluidaEm(), entity.getEntregueEm(), orcamentos);
	}

	private Orcamento paraDominio(OrcamentoEntity entity) {
		var itensServico = entity.getItensServico().stream()
				.map(item -> new ItemServico(item.getServicoId(), item.getDescricao(),
						new Dinheiro(item.getPrecoUnitario()), item.getQuantidade()))
				.toList();
		var itensPeca = entity.getItensPeca().stream()
				.map(item -> new ItemPeca(item.getItemEstoqueId(), item.getDescricao(),
						new Dinheiro(item.getPrecoUnitario()), item.getQuantidade()))
				.toList();
		return new Orcamento(entity.getId(), entity.getNumero(), entity.getSituacao(), entity.getEnviadoEm(),
				entity.getRespondidoEm(), itensServico, itensPeca);
	}

}
