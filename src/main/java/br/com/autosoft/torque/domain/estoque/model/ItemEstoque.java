package br.com.autosoft.torque.domain.estoque.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

@Getter
public class ItemEstoque {

	private final UUID id;
	private final TipoItemEstoque tipo;
	private String descricao;
	private Dinheiro preco;
	private int saldo;
	private int reservado;

	public ItemEstoque(UUID id, TipoItemEstoque tipo, String descricao, Dinheiro preco, int saldo, int reservado) {
		this.id = id;
		this.tipo = tipo;
		this.descricao = descricao;
		this.preco = preco;
		this.saldo = saldo;
		this.reservado = reservado;
	}

	public static ItemEstoque cadastrar(TipoItemEstoque tipo, String descricao, Dinheiro preco) {
		if (tipo == null) {
			throw new RegraDeNegocioException("O tipo do item é obrigatório");
		}

		var item = new ItemEstoque(UUID.randomUUID(), tipo, null, null, 0, 0);
		item.alterar(tipo, descricao, preco);
		return item;
	}

	public void alterar(TipoItemEstoque tipo, String descricao, Dinheiro preco) {
		if (this.tipo != tipo) {
			throw new RegraDeNegocioException("O tipo do item não pode ser alterado");
		}
		if (descricao == null || descricao.isBlank()) {
			throw new RegraDeNegocioException("A descrição é obrigatória");
		}
		if (preco == null || Dinheiro.ZERO.equals(preco)) {
			throw new RegraDeNegocioException("O preço deve ser maior que zero");
		}

		this.descricao = descricao;
		this.preco = preco;
	}

	public void registrarEntrada(int quantidade) {
		exigirQuantidadePositiva(quantidade);
		this.saldo += quantidade;
	}

	public int disponivel() {
		return saldo - reservado;
	}

	public void reservar(int quantidade) {
		exigirQuantidadePositiva(quantidade);
		if (quantidade > disponivel()) {
			throw new RegraDeNegocioException("Estoque insuficiente para " + descricao);
		}
		this.reservado += quantidade;
	}

	public void baixar(int quantidade) {
		exigirQuantidadePositiva(quantidade);
		if (quantidade > reservado) {
			throw new RegraDeNegocioException("A baixa não pode ser maior que a quantidade reservada");
		}
		this.saldo -= quantidade;
		this.reservado -= quantidade;
	}

	public void liberarReserva(int quantidade) {
		exigirQuantidadePositiva(quantidade);
		if (quantidade > reservado) {
			throw new RegraDeNegocioException("A liberação não pode ser maior que a quantidade reservada");
		}
		this.reservado -= quantidade;
	}

	private void exigirQuantidadePositiva(int quantidade) {
		if (quantidade <= 0) {
			throw new RegraDeNegocioException("A quantidade deve ser maior que zero");
		}
	}

}
