package br.com.autosoft.torque.application.cadastro;

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

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ServicoServiceTest {

	private final ServicoRepository repository = mock(ServicoRepository.class);
	private final TipoServicoRepository tipoServicoRepository = mock(TipoServicoRepository.class);
	private final ServicoService servicoService = new ServicoService(repository, tipoServicoRepository);

	private final TipoServico trocaDeOleo = TipoServico.cadastrar("Troca de óleo");

	@Test
	void cadastraOServicoParaOTipoDeVeiculo() {
		when(tipoServicoRepository.buscarPorId(trocaDeOleo.getId())).thenReturn(Optional.of(trocaDeOleo));

		var servico = servicoService.cadastrar(trocaDeOleo.getId(), TipoVeiculo.CARRO, new BigDecimal("80"));

		assertThat(servico.getPreco()).isEqualTo(Dinheiro.de("80.00"));
		verify(repository).salvar(servico);
	}

	@Test
	void naoCadastraParaTipoDeServicoQueNaoExiste() {
		var id = UUID.randomUUID();
		when(tipoServicoRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> servicoService.cadastrar(id, TipoVeiculo.CARRO, new BigDecimal("80")))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Tipo de serviço não encontrado");
	}

	@Test
	void recusaOMesmoServicoParaOMesmoTipoDeVeiculo() {
		when(tipoServicoRepository.buscarPorId(trocaDeOleo.getId())).thenReturn(Optional.of(trocaDeOleo));
		when(repository.existePorTipoServicoETipoVeiculo(trocaDeOleo.getId(), TipoVeiculo.CARRO)).thenReturn(true);

		assertThatThrownBy(() -> servicoService.cadastrar(trocaDeOleo.getId(), TipoVeiculo.CARRO, new BigDecimal("80")))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe serviço deste tipo para este tipo de veículo");
		verify(repository, never()).salvar(any());
	}

	@Test
	void inativaOServico() {
		var servico = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.CARRO, Dinheiro.de("80.00"));
		when(repository.buscarPorId(servico.getId())).thenReturn(Optional.of(servico));

		servicoService.alterar(servico.getId(), trocaDeOleo.getId(), TipoVeiculo.CARRO, new BigDecimal("80"), false);

		assertThat(servico.isAtivo()).isFalse();
		verify(repository).salvar(servico);
	}

}
