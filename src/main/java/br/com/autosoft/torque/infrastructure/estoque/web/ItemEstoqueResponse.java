package br.com.autosoft.torque.infrastructure.estoque.web;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;

record ItemEstoqueResponse(UUID id, TipoItemEstoque tipo, String descricao, BigDecimal preco, int saldo,
		int reservado) {

	static ItemEstoqueResponse paraResposta(ItemEstoque item) {
		return new ItemEstoqueResponse(item.getId(), item.getTipo(), item.getDescricao(), item.getPreco().valor(),
				item.getSaldo(), item.getReservado());
	}

}
