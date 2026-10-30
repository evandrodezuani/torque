package br.com.autosoft.torque.application.atendimento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.atendimento.model.ItemPeca;
import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.model.TransicaoInvalidaException;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class OrdemServicoServiceTest {

	private final OrdemServicoRepository repository = mock(OrdemServicoRepository.class);
	private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
	private final VeiculoRepository veiculoRepository = mock(VeiculoRepository.class);
	private final ServicoRepository servicoRepository = mock(ServicoRepository.class);
	private final TipoServicoRepository tipoServicoRepository = mock(TipoServicoRepository.class);
	private final ItemEstoqueRepository itemEstoqueRepository = mock(ItemEstoqueRepository.class);
	private final OrdemServicoService ordemServicoService = new OrdemServicoService(repository, clienteRepository,
			veiculoRepository, servicoRepository, tipoServicoRepository, itemEstoqueRepository);

	private final Cliente maria = Cliente.cadastrar(new Documento("52998224725"), "Maria Souza", "maria@email.com", null);
	private final Veiculo motoDaMaria = Veiculo.cadastrar(maria.getId(), new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda",
			"CG 160", 2022);
	private final TipoServico trocaDeOleo = TipoServico.cadastrar("Troca de óleo");

	@Test
	void abreAOrdemDeServicoESalva() {
		when(clienteRepository.buscarPorId(maria.getId())).thenReturn(Optional.of(maria));
		when(veiculoRepository.buscarPorId(motoDaMaria.getId())).thenReturn(Optional.of(motoDaMaria));

		var ordemServico = ordemServicoService.abrir(maria.getId(), motoDaMaria.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.RECEBIDA);
		verify(repository).salvar(ordemServico);
	}

	@Test
	void naoAbreParaClienteQueNaoExiste() {
		var id = UUID.randomUUID();
		when(clienteRepository.buscarPorId(id)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> ordemServicoService.abrir(id, motoDaMaria.getId()))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Cliente não encontrado");
		verify(repository, never()).salvar(any());
	}

	@Test
	void naoAbreComVeiculoDeOutroCliente() {
		var outroCliente = Cliente.cadastrar(new Documento("11222333000181"), "Oficina Amiga", "contato@amiga.com", null);
		when(clienteRepository.buscarPorId(outroCliente.getId())).thenReturn(Optional.of(outroCliente));
		when(veiculoRepository.buscarPorId(motoDaMaria.getId())).thenReturn(Optional.of(motoDaMaria));

		assertThatThrownBy(() -> ordemServicoService.abrir(outroCliente.getId(), motoDaMaria.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O veículo não pertence ao cliente");
		verify(repository, never()).salvar(any());
	}

	@Test
	void iniciaODiagnosticoESalva() {
		var ordemServico = OrdemServico.abrir(maria.getId(), motoDaMaria.getId(), Instant.now());
		when(repository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));

		ordemServicoService.iniciarDiagnostico(ordemServico.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
		verify(repository).salvar(ordemServico);
	}

	@Test
	void naoIniciaODiagnosticoDuasVezes() {
		var ordemServico = OrdemServico.abrir(maria.getId(), motoDaMaria.getId(), Instant.now());
		ordemServico.iniciarDiagnostico();
		when(repository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));

		assertThatThrownBy(() -> ordemServicoService.iniciarDiagnostico(ordemServico.getId()))
				.isInstanceOf(TransicaoInvalidaException.class);
		verify(repository, never()).salvar(any());
	}

	@Test
	void incluiServicoCopiandoADescricaoEOPrecoDoCadastro() {
		var ordemServico = osEmDiagnostico();
		var servico = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"));
		when(servicoRepository.buscarPorId(servico.getId())).thenReturn(Optional.of(servico));
		when(tipoServicoRepository.buscarPorId(trocaDeOleo.getId())).thenReturn(Optional.of(trocaDeOleo));

		ordemServicoService.incluirServico(ordemServico.getId(), servico.getId(), 1);

		assertThat(ordemServico.orcamentoAtual().getItensServico())
				.containsExactly(new ItemServico(servico.getId(), "Troca de óleo", Dinheiro.de("50.00"), 1));
		verify(repository).salvar(ordemServico);
	}

	@Test
	void naoIncluiServicoInativo() {
		var ordemServico = osEmDiagnostico();
		var servico = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"));
		servico.alterar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"), false);
		when(servicoRepository.buscarPorId(servico.getId())).thenReturn(Optional.of(servico));

		assertThatThrownBy(() -> ordemServicoService.incluirServico(ordemServico.getId(), servico.getId(), 1))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O serviço está inativo");
		verify(repository, never()).salvar(any());
	}

	@Test
	void naoIncluiServicoDeOutroTipoDeVeiculo() {
		var ordemServico = osEmDiagnostico();
		var servicoParaCarro = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.CARRO, Dinheiro.de("80.00"));
		when(servicoRepository.buscarPorId(servicoParaCarro.getId())).thenReturn(Optional.of(servicoParaCarro));

		assertThatThrownBy(() -> ordemServicoService.incluirServico(ordemServico.getId(), servicoParaCarro.getId(), 1))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O serviço não é para este tipo de veículo");
		verify(repository, never()).salvar(any());
	}

	@Test
	void incluiPecaCopiandoADescricaoEOPrecoDoEstoque() {
		var ordemServico = osEmDiagnostico();
		var oleo = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 10W30 1 L", Dinheiro.de("38.00"));
		when(itemEstoqueRepository.buscarPorId(oleo.getId())).thenReturn(Optional.of(oleo));

		ordemServicoService.incluirPeca(ordemServico.getId(), oleo.getId(), 2);

		assertThat(ordemServico.orcamentoAtual().getItensPeca())
				.containsExactly(new ItemPeca(oleo.getId(), "Óleo 10W30 1 L", Dinheiro.de("38.00"), 2));
		verify(repository).salvar(ordemServico);
	}

	@Test
	void concluiODiagnosticoReservandoAsPecas() {
		var ordemServico = osEmDiagnostico();
		var oleo = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 10W30 1 L", Dinheiro.de("38.00"));
		oleo.registrarEntrada(10);
		when(itemEstoqueRepository.buscarPorId(oleo.getId())).thenReturn(Optional.of(oleo));
		ordemServico.incluirPeca(new ItemPeca(oleo.getId(), "Óleo 10W30 1 L", Dinheiro.de("38.00"), 2));

		ordemServicoService.concluirDiagnostico(ordemServico.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
		assertThat(oleo.getReservado()).isEqualTo(2);
		verify(itemEstoqueRepository).salvar(oleo);
		verify(repository).salvar(ordemServico);
	}

	@Test
	void naoSalvaAOsQuandoFaltaEstoque() {
		var ordemServico = osEmDiagnostico();
		var filtro = ItemEstoque.cadastrar(TipoItemEstoque.PECA, "Filtro de óleo", Dinheiro.de("35.00"));
		filtro.registrarEntrada(1);
		when(itemEstoqueRepository.buscarPorId(filtro.getId())).thenReturn(Optional.of(filtro));
		ordemServico.incluirPeca(new ItemPeca(filtro.getId(), "Filtro de óleo", Dinheiro.de("35.00"), 2));

		assertThatThrownBy(() -> ordemServicoService.concluirDiagnostico(ordemServico.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Estoque insuficiente para Filtro de óleo");
		verify(repository, never()).salvar(any());
	}

	@Test
	void concluiODiagnosticoComOPrecoAtualDoCatalogo() {
		var ordemServico = osEmDiagnostico();
		var servico = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"));
		ordemServico.incluirServico(new ItemServico(servico.getId(), "Troca de óleo", Dinheiro.de("50.00"), 1));
		servico.alterar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("65.00"), true);
		when(servicoRepository.buscarPorId(servico.getId())).thenReturn(Optional.of(servico));

		ordemServicoService.concluirDiagnostico(ordemServico.getId());

		assertThat(ordemServico.orcamentoAtual().getItensServico().getFirst().precoUnitario())
				.isEqualTo(Dinheiro.de("65.00"));
	}

	@Test
	void naoConcluiODiagnosticoComServicoInativado() {
		var ordemServico = osEmDiagnostico();
		var servico = Servico.cadastrar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"));
		ordemServico.incluirServico(new ItemServico(servico.getId(), "Troca de óleo", Dinheiro.de("50.00"), 1));
		servico.alterar(trocaDeOleo.getId(), TipoVeiculo.MOTO, Dinheiro.de("50.00"), false);
		when(servicoRepository.buscarPorId(servico.getId())).thenReturn(Optional.of(servico));

		assertThatThrownBy(() -> ordemServicoService.concluirDiagnostico(ordemServico.getId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O serviço Troca de óleo está inativo");
		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
		verify(repository, never()).salvar(any());
	}

	@Test
	void aprovarIniciaAExecucaoEBaixaOEstoque() {
		var oleo = oleoComSaldo(10);
		var ordemServico = osAguardandoAprovacao(oleo);

		ordemServicoService.aprovarOrcamento(ordemServico.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
		assertThat(oleo.getSaldo()).isEqualTo(8);
		assertThat(oleo.getReservado()).isZero();
		verify(itemEstoqueRepository).salvar(oleo);
		verify(repository).salvar(ordemServico);
	}

	@Test
	void rejeitarLiberaAReservaSemMexerNoSaldo() {
		var oleo = oleoComSaldo(10);
		var ordemServico = osAguardandoAprovacao(oleo);

		ordemServicoService.rejeitarOrcamento(ordemServico.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.REJEITADA);
		assertThat(oleo.getSaldo()).isEqualTo(10);
		assertThat(oleo.getReservado()).isZero();
		verify(repository).salvar(ordemServico);
	}

	@Test
	void revisarCriaOOrcamentoSeguinte() {
		var oleo = oleoComSaldo(10);
		var ordemServico = osAguardandoAprovacao(oleo);
		ordemServicoService.rejeitarOrcamento(ordemServico.getId());

		ordemServicoService.revisarOrcamento(ordemServico.getId());

		assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
		assertThat(ordemServico.orcamentoAtual().getNumero()).isEqualTo(2);
		assertThat(ordemServico.orcamentoAtual().getItensPeca()).hasSize(1);
	}

	@Test
	void naoAprovaOrcamentoQueAindaNaoFoiEnviado() {
		var ordemServico = osEmDiagnostico();

		assertThatThrownBy(() -> ordemServicoService.aprovarOrcamento(ordemServico.getId()))
				.isInstanceOf(TransicaoInvalidaException.class);
		verify(repository, never()).salvar(any());
	}

	private ItemEstoque oleoComSaldo(int saldo) {
		var oleo = ItemEstoque.cadastrar(TipoItemEstoque.INSUMO, "Óleo 10W30 1 L", Dinheiro.de("38.00"));
		oleo.registrarEntrada(saldo);
		when(itemEstoqueRepository.buscarPorId(oleo.getId())).thenReturn(Optional.of(oleo));
		return oleo;
	}

	private OrdemServico osAguardandoAprovacao(ItemEstoque oleo) {
		var ordemServico = osEmDiagnostico();
		ordemServico.incluirPeca(new ItemPeca(oleo.getId(), "Óleo 10W30 1 L", Dinheiro.de("38.00"), 2));
		ordemServico.concluirDiagnostico(Instant.now(), Map.of());
		oleo.reservar(2);
		return ordemServico;
	}

	private OrdemServico osEmDiagnostico() {
		var ordemServico = OrdemServico.abrir(maria.getId(), motoDaMaria.getId(), Instant.now());
		ordemServico.iniciarDiagnostico();
		when(repository.buscarPorId(ordemServico.getId())).thenReturn(Optional.of(ordemServico));
		when(veiculoRepository.buscarPorId(motoDaMaria.getId())).thenReturn(Optional.of(motoDaMaria));
		return ordemServico;
	}

}
