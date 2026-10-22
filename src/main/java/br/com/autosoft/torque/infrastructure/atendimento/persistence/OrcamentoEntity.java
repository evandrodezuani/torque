package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import br.com.autosoft.torque.domain.atendimento.model.SituacaoOrcamento;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orcamento")
@Getter
@Setter
public class OrcamentoEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ordem_servico_id")
	private OrdemServicoEntity ordemServico;
	private int numero;
	@Enumerated(EnumType.STRING)
	private SituacaoOrcamento situacao;
	private Instant enviadoEm;
	private Instant respondidoEm;

}
