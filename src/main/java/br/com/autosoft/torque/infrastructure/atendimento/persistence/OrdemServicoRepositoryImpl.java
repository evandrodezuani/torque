package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import lombok.RequiredArgsConstructor;

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
	public boolean existePorCliente(UUID clienteId) {
		return jpaRepository.existsByClienteId(clienteId);
	}

	private OrdemServicoEntity paraEntity(OrdemServico os) {
		var entity = new OrdemServicoEntity();
		entity.setId(os.getId());
		entity.setClienteId(os.getClienteId());
		entity.setVeiculoId(os.getVeiculoId());
		entity.setStatus(os.getStatus());
		entity.setAbertaEm(os.getAbertaEm());
		entity.setExecucaoIniciadaEm(os.getExecucaoIniciadaEm());
		entity.setExecucaoConcluidaEm(os.getExecucaoConcluidaEm());
		entity.setEntregueEm(os.getEntregueEm());
		for (var orcamento : os.getOrcamentos()) {
			var orcamentoEntity = new OrcamentoEntity();
			orcamentoEntity.setOrdemServico(entity);
			orcamentoEntity.setNumero(orcamento.getNumero());
			orcamentoEntity.setSituacao(orcamento.getSituacao());
			orcamentoEntity.setEnviadoEm(orcamento.getEnviadoEm());
			orcamentoEntity.setRespondidoEm(orcamento.getRespondidoEm());
			entity.getOrcamentos().add(orcamentoEntity);
		}
		return entity;
	}

}
