package br.com.autosoft.torque.domain.atendimento.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

/**
 * Linha de serviço do orçamento. Descrição e preço são copiados do cadastro no momento da inclusão.
 */
public record ItemServico(UUID servicoId, String descricao, Dinheiro precoUnitario, int quantidade) {

	public ItemServico {
		if (servicoId == null || descricao == null || descricao.isBlank() || precoUnitario == null) {
			throw new RegraDeNegocioException("Serviço, descrição e preço são obrigatórios");
		}
		if (quantidade <= 0) {
			throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
		}
	}

	public Dinheiro subtotal() {
		return precoUnitario.multiplicar(quantidade);
	}

}
