package br.com.autosoft.torque.application.cadastro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ClienteServiceTest {

	private final ClienteRepository repository = mock(ClienteRepository.class);
	private final OrdemServicoRepository ordemServicoRepository = mock(OrdemServicoRepository.class);
	private final VeiculoRepository veiculoRepository = mock(VeiculoRepository.class);
	private final AcessoDoCliente acessoDoCliente = mock(AcessoDoCliente.class);
	private final ClienteService clienteService = new ClienteService(repository,ordemServicoRepository, veiculoRepository,
			acessoDoCliente);

	private final Cliente mockClient = Cliente.cadastrar(new Documento("529.982.247-25"), "Maria Souza", "maria@email.com", null);

	@Test
	void cadastraESalvaOCliente() {
		var cliente = clienteService.cadastrar("529.982.247-25", "Maria Souza", "maria@email.com", null);

		assertThat(cliente.getDocumento()).isEqualTo(new Documento("52998224725"));
		verify(repository).salvar(cliente);
		verify(acessoDoCliente).liberar(cliente);
	}

	@Test
	void recusaDocumentoJaCadastrado() {
		when(repository.existePorDocumento(new Documento("52998224725"))).thenReturn(true);

		assertThatThrownBy(() -> clienteService.cadastrar("529.982.247-25", "Maria Souza", "maria@email.com", null))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe cliente com este documento");
		verify(repository, never()).salvar(any());
		verify(acessoDoCliente, never()).liberar(any());
	}

	@Test
	void recusaEmailJaCadastradoMesmoComOutraCaixa() {
		when(repository.existePorEmail("maria@email.com")).thenReturn(true);

		assertThatThrownBy(() -> clienteService.cadastrar("529.982.247-25", "Maria Souza", "Maria@Email.com", null))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe cliente com este e-mail");
		verify(repository, never()).salvar(any());
	}

	@Test
	void naoAlteraParaOEmailDeOutroCliente() {
		when(repository.buscarPorId(mockClient.getId())).thenReturn(Optional.of(mockClient));
		when(repository.existePorEmail("contato@amiga.com")).thenReturn(true);

		assertThatThrownBy(() -> clienteService.alterar(mockClient.getId(), "52998224725", "Maria Souza",
				"contato@amiga.com", null))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe cliente com este e-mail");
		verify(repository, never()).salvar(any());
	}

	@Test
	void avisaQuandoOClienteNaoExiste() {
		var id = UUID.randomUUID();
		when(repository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> clienteService.consultar(id))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Cliente não encontrado");
	}

	@Test
	void alteraESalvaOCliente() {
		when(repository.buscarPorId(mockClient.getId())).thenReturn(Optional.of(mockClient));

		clienteService.alterar(mockClient.getId(), "52998224725", "Maria S. Lima", "maria@email.com", null);

		assertThat(mockClient.getNome()).isEqualTo("Maria S. Lima");
		verify(repository).salvar(mockClient);
	}

	@Test
	void naoRemoveClienteComOrdemDeServicoEmAndamento() {
		when(repository.buscarPorId(mockClient.getId())).thenReturn(Optional.of(mockClient));
		when(ordemServicoRepository.existeEmAndamentoPorCliente(mockClient.getId())).thenReturn(true);

		assertThatThrownBy(() -> clienteService.remover(mockClient.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O cliente tem ordens de serviço em andamento e não pode ser removido");
		verify(repository, never()).remover(any());
	}

	@Test
	void removeOClienteComOHistorico() {
		when(repository.buscarPorId(mockClient.getId())).thenReturn(Optional.of(mockClient));

		clienteService.remover(mockClient.getId());

		var ordem = inOrder(ordemServicoRepository, veiculoRepository, acessoDoCliente, repository);
		ordem.verify(ordemServicoRepository).removerPorCliente(mockClient.getId());
		ordem.verify(veiculoRepository).removerPorCliente(mockClient.getId());
		ordem.verify(acessoDoCliente).remover(mockClient.getId());
		ordem.verify(repository).remover(mockClient.getId());
	}
}
