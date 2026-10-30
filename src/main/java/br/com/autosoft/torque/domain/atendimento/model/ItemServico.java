package br.com.autosoft.torque.domain.atendimento.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

/**
 * Linha de serviço do orçamento. Descrição e preço são copiados do cadastro no momento da inclusão.
 */
public record ItemServico(UUID servicoId, String descricao, Dinheiro precoUnitario, int quantidade)
		implements ItemOrcamento<ItemServico> {

	public ItemServico {
		if (servicoId == null || descricao == null || descricao.isBlank() || precoUnitario == null) {
			throw new RegraDeNegocioException("Serviço, descrição e preço são obrigatórios");
		}
		if (quantidade <= 0) {
			throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
		}
	}

	@Override
	public UUID referencia() {
		return servicoId;
	}

	@Override
	public ItemServico somar(int quantidade) {
		return new ItemServico(servicoId, descricao, precoUnitario, this.quantidade + quantidade);
	}

	@Override
	public ItemServico comPreco(Dinheiro precoUnitario) {
		return new ItemServico(servicoId, descricao, precoUnitario, quantidade);
	}

	@Override
	public Dinheiro subtotal() {
		return precoUnitario.multiplicar(quantidade);
	}

}
