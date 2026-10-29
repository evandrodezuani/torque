package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ServicoTest {

	private final UUID trocaDeOleo = UUID.randomUUID();

	@Test
	void cadastraServicoAtivo() {
		var servico = Servico.cadastrar(trocaDeOleo, TipoVeiculo.MOTO, Dinheiro.de("50.00"));

		assertThat(servico.getId()).isNotNull();
		assertThat(servico.getTipoServicoId()).isEqualTo(trocaDeOleo);
		assertThat(servico.getTipoVeiculo()).isEqualTo(TipoVeiculo.MOTO);
		assertThat(servico.getPreco()).isEqualTo(Dinheiro.de("50.00"));
		assertThat(servico.isAtivo()).isTrue();
	}

	@Test
	void naoCadastraSemTipoDeServicoTipoDeVeiculoOuPreco() {
		assertThatThrownBy(() -> Servico.cadastrar(null, TipoVeiculo.CARRO, Dinheiro.de("80.00")))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> Servico.cadastrar(trocaDeOleo, null, Dinheiro.de("80.00")))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> Servico.cadastrar(trocaDeOleo, TipoVeiculo.CARRO, Dinheiro.ZERO))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O preço deve ser maior que zero");
	}

	@Test
	void alteraOPrecoEInativa() {
		var servico = Servico.cadastrar(trocaDeOleo, TipoVeiculo.CARRO, Dinheiro.de("80.00"));

		servico.alterar(trocaDeOleo, TipoVeiculo.CARRO, Dinheiro.de("90.00"), false);

		assertThat(servico.getPreco()).isEqualTo(Dinheiro.de("90.00"));
		assertThat(servico.isAtivo()).isFalse();
	}

	@Test
	void naoAlteraTipoDeServicoNemTipoDeVeiculo() {
		var servico = Servico.cadastrar(trocaDeOleo, TipoVeiculo.CARRO, Dinheiro.de("80.00"));

		assertThatThrownBy(() -> servico.alterar(UUID.randomUUID(), TipoVeiculo.CARRO, Dinheiro.de("80.00"), true))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O tipo de serviço e o tipo de veículo não podem ser alterados");
		assertThatThrownBy(() -> servico.alterar(trocaDeOleo, TipoVeiculo.MOTO, Dinheiro.de("80.00"), true))
				.isInstanceOf(RegraDeNegocioException.class);
	}

}
