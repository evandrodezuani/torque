package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import br.com.autosoft.torque.domain.atendimento.model.StatusOS;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ordem_servico")
@Getter
@Setter
public class OrdemServicoEntity {

	@Id
	private UUID id;
	private UUID clienteId;
	private UUID veiculoId;
	@Enumerated(EnumType.STRING)
	private StatusOS status;
	private Instant abertaEm;
	private Instant execucaoIniciadaEm;
	private Instant execucaoConcluidaEm;
	private Instant entregueEm;
	@OneToMany(mappedBy = "ordemServico", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<OrcamentoEntity> orcamentos = new ArrayList<OrcamentoEntity>();

}
