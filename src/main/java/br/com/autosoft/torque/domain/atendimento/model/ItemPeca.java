package br.com.autosoft.torque.domain.atendimento.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

/**
 * Linha de peça ou insumo do orçamento. Referencia o item de estoque por identidade;
 * descrição e preço são copiados no momento da inclusão.
 */
public record ItemPeca(UUID itemEstoqueId, String descricao, Dinheiro precoUnitario, int quantidade)
		implements ItemOrcamento<ItemPeca> {

	public ItemPeca {
		if (itemEstoqueId == null || descricao == null || descricao.isBlank() || precoUnitario == null) {
			throw new RegraDeNegocioException("Item de estoque, descrição e preço são obrigatórios");
		}
		if (quantidade <= 0) {
			throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
		}
	}

	@Override
	public UUID referencia() {
		return itemEstoqueId;
	}

	@Override
	public ItemPeca somar(int quantidade) {
		return new ItemPeca(itemEstoqueId, descricao, precoUnitario, this.quantidade + quantidade);
	}

	@Override
	public ItemPeca comPreco(Dinheiro precoUnitario) {
		return new ItemPeca(itemEstoqueId, descricao, precoUnitario, quantidade);
	}

	@Override
	public Dinheiro subtotal() {
		return precoUnitario.multiplicar(quantidade);
	}

}
