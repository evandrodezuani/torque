package br.com.autosoft.torque.domain.cadastro.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

@Getter
public class Servico {

	private final UUID id;
	private final UUID tipoServicoId;
	private final TipoVeiculo tipoVeiculo;
	private Dinheiro preco;
	private boolean ativo;

	public Servico(UUID id, UUID tipoServicoId, TipoVeiculo tipoVeiculo, Dinheiro preco, boolean ativo) {
		this.id = id;
		this.tipoServicoId = tipoServicoId;
		this.tipoVeiculo = tipoVeiculo;
		this.preco = preco;
		this.ativo = ativo;
	}

	public static Servico cadastrar(UUID tipoServicoId, TipoVeiculo tipoVeiculo, Dinheiro preco) {
		if (tipoServicoId == null) {
			throw new RegraDeNegocioException("O tipo de serviço é obrigatório");
		}
		if (tipoVeiculo == null) {
			throw new RegraDeNegocioException("O tipo de veículo é obrigatório");
		}

		var servico = new Servico(UUID.randomUUID(), tipoServicoId, tipoVeiculo, null, true);
		servico.alterar(tipoServicoId, tipoVeiculo, preco, true);
		return servico;
	}

	public void alterar(UUID tipoServicoId, TipoVeiculo tipoVeiculo, Dinheiro preco, boolean ativo) {
		if (!this.tipoServicoId.equals(tipoServicoId) || this.tipoVeiculo != tipoVeiculo) {
			throw new RegraDeNegocioException("O tipo de serviço e o tipo de veículo não podem ser alterados");
		}

		if (preco == null || preco.equals(Dinheiro.ZERO)) {
			throw new RegraDeNegocioException("O preço deve ser maior que zero");
		}

		this.preco = preco;
		this.ativo = ativo;
	}

}
