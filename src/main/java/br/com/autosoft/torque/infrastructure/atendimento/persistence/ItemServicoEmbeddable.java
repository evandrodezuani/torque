package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
class ItemServicoEmbeddable {

	private UUID servicoId;
	private String descricao;
	private BigDecimal precoUnitario;
	private int quantidade;

}
