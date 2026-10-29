package br.com.autosoft.torque.infrastructure.estoque.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "item_estoque")
@Getter
@Setter
class ItemEstoqueEntity {

	@Id
	private UUID id;

	@Enumerated(EnumType.STRING)
	private TipoItemEstoque tipo;

	private String descricao;

	private BigDecimal preco;

	private int saldo;

	private int reservado;

}
