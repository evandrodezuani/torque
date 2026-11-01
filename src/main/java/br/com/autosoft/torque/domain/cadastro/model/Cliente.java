package br.com.autosoft.torque.domain.cadastro.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

@Getter
public class Cliente {

	private final UUID id;
	private final Documento documento;
	private String nome;
	private String email;
	private String telefone;

	public Cliente(UUID id, Documento documento, String nome, String email, String telefone) {
		this.id = id;
		this.documento = documento;
		this.nome = nome;
		this.email = email;
		this.telefone = telefone;
	}

	public static Cliente cadastrar(Documento documento, String nome, String email, String telefone) {
		if (documento == null) {
			throw new RegraDeNegocioException("O documento é obrigatório");
		}

		var cliente = new Cliente(UUID.randomUUID(), documento, null, null, null);
		cliente.alterar(documento, nome, email, telefone);
		return cliente;
	}

	public void alterar(Documento documento, String nome, String email, String telefone) {
		if (!this.documento.equals(documento)) {
			throw new RegraDeNegocioException("O documento não pode ser alterado");
		}

		if (nome == null || nome.isBlank()) {
			throw new RegraDeNegocioException("O nome é obrigatório");
		}

		if (email == null || email.isBlank()) {
			throw new RegraDeNegocioException("O email é obrigatório");
		}

		this.nome = nome;
		this.email = email.trim().toLowerCase();
		this.telefone = telefone;
	}

}
