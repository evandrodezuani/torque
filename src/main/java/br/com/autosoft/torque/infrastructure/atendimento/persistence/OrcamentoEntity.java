package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import br.com.autosoft.torque.domain.atendimento.model.SituacaoOrcamento;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orcamento")
@Getter
@Setter
public class OrcamentoEntity {

	@Id
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ordem_servico_id")
	private OrdemServicoEntity ordemServico;
	private int numero;
	@Enumerated(EnumType.STRING)
	private SituacaoOrcamento situacao;
	private Instant enviadoEm;
	private Instant respondidoEm;
	@ElementCollection
	@CollectionTable(name = "orcamento_item_servico", joinColumns = @JoinColumn(name = "orcamento_id"))
	private List<ItemServicoEmbeddable> itensServico = new ArrayList<>();
	@ElementCollection
	@CollectionTable(name = "orcamento_item_peca", joinColumns = @JoinColumn(name = "orcamento_id"))
	private List<ItemPecaEmbeddable> itensPeca = new ArrayList<>();

}
