package br.com.autosoft.torque.domain.atendimento.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class OrdemServicoTest {

	private static final Instant AGORA = Instant.parse("2026-10-12T10:00:00Z");

	private final ItemServico trocaDeOleo = new ItemServico(UUID.randomUUID(), "Troca de óleo", Dinheiro.de("80.00"), 1);
	private final ItemPeca filtro = new ItemPeca(UUID.randomUUID(), "Filtro de óleo", Dinheiro.de("45.00"), 1);
	private final ItemPeca oleo = new ItemPeca(UUID.randomUUID(), "Óleo 5W30 (litro)", Dinheiro.de("38.00"), 4);

	@Test
	void osAbertaNasceRecebidaComOrcamentoEmElaboracao() {
		var os = novaOS();

		assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
		assertThat(os.getOrcamentos()).hasSize(1);
		assertThat(os.orcamentoAtual().getNumero()).isEqualTo(1);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.EM_ELABORACAO);
	}

	@Test
	void abrirGeraUmCodigoDeAcompanhamentoDiferenteDoId() {
		var os = novaOS();

		assertThat(os.getCodigoAcompanhamento()).isNotNull().isNotEqualTo(os.getId());
		assertThat(novaOS().getCodigoAcompanhamento()).isNotEqualTo(os.getCodigoAcompanhamento());
	}

	@Test
	void naoAbreOSSemClienteOuVeiculo() {
		assertThatThrownBy(() -> OrdemServico.abrir(null, UUID.randomUUID(), AGORA))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> OrdemServico.abrir(UUID.randomUUID(), null, AGORA))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void fluxoCompletoPercorreTodosOsStatusAteEntregue() {
		var os = novaOS();

		os.iniciarDiagnostico();
		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);

		os.incluirServico(trocaDeOleo);
		os.concluirDiagnostico(AGORA, Map.of());
		assertThat(os.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);

		os.aprovarOrcamento(AGORA);
		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);

		os.concluirServico(AGORA);
		assertThat(os.getStatus()).isEqualTo(StatusOS.FINALIZADA);

		os.entregarVeiculo(AGORA);
		assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
		assertThat(os.getEntregueEm()).isEqualTo(AGORA);
	}

	@Test
	void orcamentoSomaServicosPecasEInsumos() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);
		os.incluirPeca(oleo);

		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.de("277.00"));
	}

	@Test
	void concluirDiagnosticoEnviaOOrcamento() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);

		os.concluirDiagnostico(AGORA, Map.of());

		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.PENDENTE);
		assertThat(os.orcamentoAtual().getEnviadoEm()).isEqualTo(AGORA);
	}

	@Test
	void envioUsaOPrecoVigenteEMantemADescricao() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);

		os.concluirDiagnostico(AGORA, Map.of(
				trocaDeOleo.servicoId(), Dinheiro.de("90.00"),
				filtro.itemEstoqueId(), Dinheiro.de("40.00")));

		assertThat(os.orcamentoAtual().getItensServico())
				.containsExactly(new ItemServico(trocaDeOleo.servicoId(), "Troca de óleo", Dinheiro.de("90.00"), 1));
		assertThat(os.orcamentoAtual().getItensPeca())
				.containsExactly(new ItemPeca(filtro.itemEstoqueId(), "Filtro de óleo", Dinheiro.de("40.00"), 1));
		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.de("130.00"));
	}

	@Test
	void naoConcluiDiagnosticoComOrcamentoVazio() {
		var os = osEmDiagnostico();

		assertThatThrownBy(() -> os.concluirDiagnostico(AGORA, Map.of())).isInstanceOf(RegraDeNegocioException.class);
		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
	}

	@Test
	void naoIncluiItemAntesDeIniciarODiagnostico() {
		var os = novaOS();

		assertThatThrownBy(() -> os.incluirServico(trocaDeOleo))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Só é possível incluir serviços numa OS em diagnóstico");
		assertThatThrownBy(() -> os.incluirPeca(filtro))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Só é possível incluir peças e insumos numa OS em diagnóstico");
		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.ZERO);
	}

	@Test
	void mesmoServicoIncluidoDeNovoSomaNaMesmaLinha() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);

		os.incluirServico(new ItemServico(trocaDeOleo.servicoId(), "Troca de óleo", Dinheiro.de("90.00"), 2));

		assertThat(os.orcamentoAtual().getItensServico())
				.containsExactly(new ItemServico(trocaDeOleo.servicoId(), "Troca de óleo", Dinheiro.de("80.00"), 3));
	}

	@Test
	void mesmaPecaIncluidaDeNovoSomaNaMesmaLinha() {
		var os = osEmDiagnostico();
		os.incluirPeca(oleo);
		os.incluirPeca(filtro);

		os.incluirPeca(oleo);

		assertThat(os.orcamentoAtual().getItensPeca()).containsExactly(oleo.somar(4), filtro);
		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.de("349.00"));
	}

	@Test
	void removeALinhaDoOrcamento() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);
		os.incluirPeca(oleo);

		os.removerPeca(oleo.itemEstoqueId());
		os.removerServico(trocaDeOleo.servicoId());

		assertThat(os.orcamentoAtual().getItensServico()).isEmpty();
		assertThat(os.orcamentoAtual().getItensPeca()).containsExactly(filtro);
		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.de("45.00"));
	}

	@Test
	void naoRemoveItemQueNaoEstaNoOrcamento() {
		var os = osEmDiagnostico();
		os.incluirPeca(filtro);

		assertThatThrownBy(() -> os.removerPeca(oleo.itemEstoqueId()))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Peça ou insumo não encontrado no orçamento");
		assertThatThrownBy(() -> os.removerServico(trocaDeOleo.servicoId()))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Serviço não encontrado no orçamento");
	}

	@Test
	void naoRemoveItemDepoisQueOOrcamentoFoiEnviado() {
		var os = osAguardandoAprovacao();

		assertThatThrownBy(() -> os.removerServico(trocaDeOleo.servicoId()))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Só é possível remover serviços numa OS em diagnóstico");
		assertThat(os.orcamentoAtual().getItensServico()).containsExactly(trocaDeOleo);
	}

	@Test
	void naoIncluiItemDepoisQueOOrcamentoFoiEnviado() {
		var os = osAguardandoAprovacao();

		assertThatThrownBy(() -> os.incluirPeca(filtro)).isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> os.incluirServico(trocaDeOleo)).isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void aprovacaoRegistraARespostaEIniciaAExecucao() {
		var os = osAguardandoAprovacao();

		os.aprovarOrcamento(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.APROVADO);
		assertThat(os.orcamentoAtual().getRespondidoEm()).isEqualTo(AGORA);
	}

	@Test
	void rejeicaoRegistraARespostaELevaAOSParaRejeitada() {
		var os = osAguardandoAprovacao();

		os.rejeitarOrcamento(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.REJEITADA);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
		assertThat(os.orcamentoAtual().getRespondidoEm()).isEqualTo(AGORA);
	}

	@Test
	void revisaoCriaNovoOrcamentoComAsLinhasDoAnteriorEPreservaOHistorico() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);
		os.concluirDiagnostico(AGORA, Map.of());
		os.rejeitarOrcamento(AGORA);

		os.revisarOrcamento();
		os.incluirPeca(oleo);

		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
		assertThat(os.getOrcamentos()).hasSize(2);

		var rejeitado = os.getOrcamentos().getFirst();
		assertThat(rejeitado.getSituacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
		assertThat(rejeitado.getItensPeca()).containsExactly(filtro);
		assertThat(rejeitado.total()).isEqualTo(Dinheiro.de("125.00"));

		var revisado = os.orcamentoAtual();
		assertThat(revisado.getNumero()).isEqualTo(2);
		assertThat(revisado.getSituacao()).isEqualTo(SituacaoOrcamento.EM_ELABORACAO);
		assertThat(revisado.getItensServico()).containsExactly(trocaDeOleo);
		assertThat(revisado.getItensPeca()).containsExactly(filtro, oleo);
		assertThat(revisado.total()).isEqualTo(Dinheiro.de("277.00"));
	}

	@Test
	void soRevisaOrcamentoDeOSRejeitada() {
		assertThatThrownBy(() -> novaOS().revisarOrcamento()).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> osAguardandoAprovacao().revisarOrcamento())
				.isInstanceOf(TransicaoInvalidaException.class);
	}

	@Test
	void veiculoDeOSRejeitadaPodeSerEntregueSemExecucao() {
		var os = osAguardandoAprovacao();
		os.rejeitarOrcamento(AGORA);

		os.entregarVeiculo(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
		assertThat(os.tempoDeExecucao()).isEmpty();
	}

	@Test
	void transicoesForaDeOrdemSaoRecusadasEMantemOStatus() {
		var recebida = novaOS();
		assertThatThrownBy(() -> recebida.concluirDiagnostico(AGORA, Map.of())).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.aprovarOrcamento(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.rejeitarOrcamento(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.concluirServico(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThat(recebida.getStatus()).isEqualTo(StatusOS.RECEBIDA);

		var aguardando = osAguardandoAprovacao();
		assertThatThrownBy(aguardando::iniciarDiagnostico).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> aguardando.concluirServico(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThat(aguardando.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
	}

	@Test
	void veiculoPodeSerDevolvidoAntesDoOrcamento() {
		var recebida = novaOS();
		recebida.entregarVeiculo(AGORA);
		assertThat(recebida.getStatus()).isEqualTo(StatusOS.ENTREGUE);

		var emDiagnostico = osEmDiagnostico();
		emDiagnostico.incluirPeca(filtro);
		emDiagnostico.entregarVeiculo(AGORA);
		assertThat(emDiagnostico.getStatus()).isEqualTo(StatusOS.ENTREGUE);
		assertThat(emDiagnostico.tempoDeExecucao()).isEmpty();
	}

	@Test
	void naoEntregaComOrcamentoAguardandoAprovacaoNemEmExecucao() {
		var aguardando = osAguardandoAprovacao();
		assertThatThrownBy(() -> aguardando.entregarVeiculo(AGORA))
				.isInstanceOf(TransicaoInvalidaException.class)
				.hasMessage("Só é possível entregar o veículo de uma OS recebida, em diagnóstico, rejeitada ou finalizada");

		var emExecucao = osAguardandoAprovacao();
		emExecucao.aprovarOrcamento(AGORA);
		assertThatThrownBy(() -> emExecucao.entregarVeiculo(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThat(emExecucao.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
	}

	@Test
	void osEntregueNaoAceitaMaisNenhumComando() {
		var os = osAguardandoAprovacao();
		os.aprovarOrcamento(AGORA);
		os.concluirServico(AGORA);
		os.entregarVeiculo(AGORA);

		assertThatThrownBy(os::iniciarDiagnostico).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> os.entregarVeiculo(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(os::revisarOrcamento).isInstanceOf(TransicaoInvalidaException.class);
	}

	@Test
	void tempoDeExecucaoContaDaAprovacaoAteAConclusaoDoServico() {
		var os = osAguardandoAprovacao();
		assertThat(os.tempoDeExecucao()).isEmpty();

		os.aprovarOrcamento(AGORA);
		assertThat(os.tempoDeExecucao()).isEmpty();

		os.concluirServico(AGORA.plus(Duration.ofHours(5)));
		assertThat(os.tempoDeExecucao()).contains(Duration.ofHours(5));
	}

	private OrdemServico novaOS() {
		return OrdemServico.abrir(UUID.randomUUID(), UUID.randomUUID(), AGORA);
	}

	private OrdemServico osEmDiagnostico() {
		var os = novaOS();
		os.iniciarDiagnostico();
		return os;
	}

	private OrdemServico osAguardandoAprovacao() {
		var os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.concluirDiagnostico(AGORA, Map.of());
		return os;
	}

}
