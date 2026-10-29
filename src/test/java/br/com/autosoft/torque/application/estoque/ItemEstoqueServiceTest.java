package br.com.autosoft.torque.application.estoque;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ItemEstoqueServiceTest {

	private final ItemEstoqueRepository repository = mock(ItemEstoqueRepository.class);
	private final ItemEstoqueService itemEstoqueService = new ItemEstoqueService(repository);

	@Test
	void cadastraOItem() {
		var item = itemEstoqueService.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", new BigDecimal("35"));

		assertThat(item.getPreco()).isEqualTo(Dinheiro.de("35.00"));
		verify(repository).salvar(item);
	}

	@Test
	void recusaDescricaoRepetida() {
		when(repository.existePorDescricao("Filtro de óleo")).thenReturn(true);

		assertThatThrownBy(() -> itemEstoqueService.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", new BigDecimal("35")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe item de estoque com esta descrição");
		verify(repository, never()).salvar(any());
	}

	@Test
	void registraAEntradaESalva() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));
		when(repository.buscarPorId(item.getId())).thenReturn(Optional.of(item));

		itemEstoqueService.registrarEntrada(item.getId(), 10);

		assertThat(item.getSaldo()).isEqualTo(10);
		verify(repository).salvar(item);
	}

	@Test
	void naoRegistraEntradaEmItemQueNaoExiste() {
		var id = UUID.randomUUID();
		when(repository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> itemEstoqueService.registrarEntrada(id, 10))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Item de estoque não encontrado");
	}

	@Test
	void naoRemoveItemComQuantidadeReservada() {
		var item = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));
		item.registrarEntrada(10);
		item.reservar(2);
		when(repository.buscarPorId(item.getId())).thenReturn(Optional.of(item));

		assertThatThrownBy(() -> itemEstoqueService.remover(item.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O item tem quantidade reservada e não pode ser removido");
		verify(repository, never()).remover(any());
	}

}
