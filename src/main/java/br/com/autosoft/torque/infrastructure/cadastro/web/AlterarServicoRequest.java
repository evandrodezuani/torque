package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import jakarta.validation.constraints.NotNull;

record AlterarServicoRequest(
		@NotNull UUID tipoServicoId,
		@NotNull TipoVeiculo tipoVeiculo,
		@NotNull BigDecimal preco,
		@NotNull Boolean ativo) {

}
