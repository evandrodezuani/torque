package br.com.autosoft.torque.domain.cadastro.model;

import java.util.UUID;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

@Getter
public class TipoServico {

	private final UUID id;
	private String descricao;

	public TipoServico(UUID id, String descricao) {
		this.id = id;
		this.descricao = descricao;
	}

	public static TipoServico cadastrar(String descricao) {
		var tipoServico = new TipoServico(UUID.randomUUID(), null);
		tipoServico.alterar(descricao);
		return tipoServico;
	}

	public void alterar(String descricao) {
		if (descricao == null || descricao.isBlank()) {
			throw new RegraDeNegocioException("A descrição é obrigatória");
		}
		this.descricao = descricao;
	}

}
