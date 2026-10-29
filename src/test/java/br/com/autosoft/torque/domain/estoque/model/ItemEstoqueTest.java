package br.com.autosoft.torque.domain.estoque.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ItemEstoqueTest {

	@Test
	void cadastraComSaldoEReservaZerados() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));

		assertThat(item.getId()).isNotNull();
		assertThat(item.getTipo()).isEqualTo(TipoItemEstoque.PECA);
		assertThat(item.getPreco()).isEqualTo(Dinheiro.de("35.00"));
		assertThat(item.getSaldo()).isZero();
		assertThat(item.getReservado()).isZero();
	}

	@Test
	void naoCadastraSemTipoDescricaoOuPreco() {
		assertThatThrownBy(() -> ItemEstoque.cadastrar(null, "Filtro de óleo", Dinheiro.de("35.00")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O tipo do item é obrigatório");
		assertThatThrownBy(() -> ItemEstoque.cadastrar(TipoItemEstoque.PECA, " ", Dinheiro.de("35.00")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("A descrição é obrigatória");
		assertThatThrownBy(() -> ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.ZERO))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O preço deve ser maior que zero");
	}

	@Test
	void alteraDescricaoEPrecoSemMexerNoSaldo() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 5W30", Dinheiro.de("40.00"));
		item.registrarEntrada(10);

		item.alterar(TipoItemEstoque.INSUMO, "Óleo 5W30 1 L", Dinheiro.de("42.00"));

		assertThat(item.getDescricao()).isEqualTo("Óleo 5W30 1 L");
		assertThat(item.getPreco()).isEqualTo(Dinheiro.de("42.00"));
		assertThat(item.getSaldo()).isEqualTo(10);
	}

	@Test
	void naoAlteraOTipo() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 5W30", Dinheiro.de("40.00"));

		assertThatThrownBy(() -> item.alterar(TipoItemEstoque.PECA, "Óleo 5W30", Dinheiro.de("40.00")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O tipo do item não pode ser alterado");
	}

	@Test
	void entradaSomaNoSaldo() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));

		item.registrarEntrada(10);
		item.registrarEntrada(5);

		assertThat(item.getSaldo()).isEqualTo(15);
	}

	@Test
	void recusaEntradaSemQuantidade() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));

		assertThatThrownBy(() -> item.registrarEntrada(0))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("A quantidade deve ser maior que zero");
		assertThatThrownBy(() -> item.registrarEntrada(-3))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void reservaDiminuiODisponivelSemMexerNoSaldo() {
		var item = filtroComSaldo(10);

		item.reservar(4);

		assertThat(item.getSaldo()).isEqualTo(10);
		assertThat(item.getReservado()).isEqualTo(4);
		assertThat(item.disponivel()).isEqualTo(6);
	}

	@Test
	void naoReservaMaisQueODisponivel() {
		var item = filtroComSaldo(10);
		item.reservar(8);

		assertThatThrownBy(() -> item.reservar(3))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Estoque insuficiente para Filtro de óleo");
		assertThat(item.getReservado()).isEqualTo(8);
	}

	@Test
	void baixaTiraDoSaldoEDaReserva() {
		var item = filtroComSaldo(10);
		item.reservar(4);

		item.baixar(4);

		assertThat(item.getSaldo()).isEqualTo(6);
		assertThat(item.getReservado()).isZero();
		assertThat(item.disponivel()).isEqualTo(6);
	}

	@Test
	void naoBaixaMaisQueOReservado() {
		var item = filtroComSaldo(10);
		item.reservar(2);

		assertThatThrownBy(() -> item.baixar(3))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("A baixa não pode ser maior que a quantidade reservada");
		assertThat(item.getSaldo()).isEqualTo(10);
	}

	@Test
	void liberacaoDevolveAoDisponivel() {
		var item = filtroComSaldo(10);
		item.reservar(4);

		item.liberarReserva(4);

		assertThat(item.getSaldo()).isEqualTo(10);
		assertThat(item.getReservado()).isZero();
		assertThat(item.disponivel()).isEqualTo(10);
	}

	@Test
	void naoLiberaMaisQueOReservado() {
		var item = filtroComSaldo(10);
		item.reservar(1);

		assertThatThrownBy(() -> item.liberarReserva(2))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("A liberação não pode ser maior que a quantidade reservada");
	}

	@Test
	void recusaQuantidadeZeroNaReservaNaBaixaENaLiberacao() {
		var item = filtroComSaldo(10);

		assertThatThrownBy(() -> item.reservar(0)).hasMessage("A quantidade deve ser maior que zero");
		assertThatThrownBy(() -> item.baixar(0)).hasMessage("A quantidade deve ser maior que zero");
		assertThatThrownBy(() -> item.liberarReserva(-1)).hasMessage("A quantidade deve ser maior que zero");
	}

	private ItemEstoque filtroComSaldo(int saldo) {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));
		item.registrarEntrada(saldo);
		return item;
	}

}
