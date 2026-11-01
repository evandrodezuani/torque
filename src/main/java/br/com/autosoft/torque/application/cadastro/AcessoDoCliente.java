package br.com.autosoft.torque.application.cadastro;

import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;

/**
 * O acesso do cliente à API: o usuário dele e o link, enviado por e-mail, para ele criar a própria senha.
 */
public interface AcessoDoCliente {

	void liberar(Cliente cliente);

	void remover(UUID clienteId);

}
