package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class TipoServicoTest {

	@Test
	void cadastraERenomeiaOTipoDeServico() {
		var tipoServico = TipoServico.cadastrar("Troca de oleo");

		tipoServico.alterar("Troca de óleo");

		assertThat(tipoServico.getId()).isNotNull();
		assertThat(tipoServico.getDescricao()).isEqualTo("Troca de óleo");
	}

	@Test
	void naoAceitaDescricaoVazia() {
		assertThatThrownBy(() -> TipoServico.cadastrar(" "))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("A descrição é obrigatória");
	}

}
