package br.com.autosoft.torque.infrastructure.estoque.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;

@SpringBootTest
@Transactional
class ItemEstoqueRepositoryImplTest {

	@Autowired
	private ItemEstoqueRepository repository;

	@Test
	void salvaEBuscaOItemDeVolta() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 5W30", Dinheiro.de("40.00"));
		item.registrarEntrada(12);
		item.reservar(3);

		repository.salvar(item);
		var lido = repository.buscarPorId(item.getId()).orElseThrow();

		assertThat(lido.getTipo()).isEqualTo(TipoItemEstoque.INSUMO);
		assertThat(lido.getDescricao()).isEqualTo("Óleo 5W30");
		assertThat(lido.getPreco()).isEqualTo(Dinheiro.de("40.00"));
		assertThat(lido.getSaldo()).isEqualTo(12);
		assertThat(lido.getReservado()).isEqualTo(3);
	}

	@Test
	void sabeSeADescricaoJaExiste() {
		repository.salvar(ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00")));

		assertThat(repository.existePorDescricao("Filtro de óleo")).isTrue();
		assertThat(repository.existePorDescricao("Pastilha de freio")).isFalse();
	}

}
