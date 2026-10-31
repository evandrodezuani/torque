package br.com.autosoft.torque.domain.atendimento.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;

/**
 * o tipo genérico faz o somar devolver o mesmo tipo de item.
 */
public interface ItemOrcamento<T extends ItemOrcamento<T>> {

	UUID referencia();

	String descricao();

	int quantidade();

	Dinheiro precoUnitario();

	T somar(int quantidade);

	T comPreco(Dinheiro precoUnitario);

	Dinheiro subtotal();

}
