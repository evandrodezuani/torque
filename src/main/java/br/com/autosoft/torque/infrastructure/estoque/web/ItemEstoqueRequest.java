package br.com.autosoft.torque.infrastructure.estoque.web;

import java.math.BigDecimal;

import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record ItemEstoqueRequest(
		@NotNull TipoItemEstoque tipo,
		@NotBlank @Size(max = 100) String descricao,
		@NotNull BigDecimal preco) {

}
