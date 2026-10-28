package br.com.autosoft.torque.application.cadastro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class TipoServicoServiceTest {

	private final TipoServicoRepository repository = mock(TipoServicoRepository.class);
	private final ServicoRepository servicoRepository = mock(ServicoRepository.class);
	private final TipoServicoService tipoServicoService = new TipoServicoService(repository, servicoRepository);

	private final TipoServico trocaDeOleo = TipoServico.cadastrar("Troca de óleo");

	@Test
	void cadastraESalvaOTipoDeServico() {
		var tipoServico = tipoServicoService.cadastrar("Alinhamento");

		assertThat(tipoServico.getDescricao()).isEqualTo("Alinhamento");
		verify(repository).salvar(tipoServico);
	}

	@Test
	void recusaDescricaoJaCadastrada() {
		when(repository.existePorDescricao("Troca de óleo")).thenReturn(true);

		assertThatThrownBy(() -> tipoServicoService.cadastrar("Troca de óleo"))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe tipo de serviço com esta descrição");
		verify(repository, never()).salvar(any());
	}

	@Test
	void naoRenomeiaParaUmaDescricaoQueJaExiste() {
		when(repository.buscarPorId(trocaDeOleo.getId())).thenReturn(Optional.of(trocaDeOleo));
		when(repository.existePorDescricao("Alinhamento")).thenReturn(true);

		assertThatThrownBy(() -> tipoServicoService.alterar(trocaDeOleo.getId(), "Alinhamento"))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe tipo de serviço com esta descrição");
	}

	@Test
	void naoRemoveTipoDeServicoComServicos() {
		when(repository.buscarPorId(trocaDeOleo.getId())).thenReturn(Optional.of(trocaDeOleo));
		when(servicoRepository.existePorTipoServico(trocaDeOleo.getId())).thenReturn(true);

		assertThatThrownBy(() -> tipoServicoService.remover(trocaDeOleo.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O tipo de serviço tem serviços cadastrados e não pode ser removido");
		verify(repository, never()).remover(any());
	}

}
