package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
class ItemPecaEmbeddable {

	private UUID itemEstoqueId;
	private String descricao;
	private BigDecimal precoUnitario;
	private int quantidade;

}
