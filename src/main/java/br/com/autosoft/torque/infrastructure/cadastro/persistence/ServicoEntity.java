package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "servico")
@Getter
@Setter
class ServicoEntity {

	@Id
	private UUID id;

	private UUID tipoServicoId;

	@Enumerated(EnumType.STRING)
	private TipoVeiculo tipoVeiculo;

	private BigDecimal preco;

	private boolean ativo;

}
