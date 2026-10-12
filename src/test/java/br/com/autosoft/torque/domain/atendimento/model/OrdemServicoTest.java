package br.com.autosoft.torque.domain.atendimento.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class OrdemServicoTest {

	private static final Instant AGORA = Instant.parse("2026-10-12T10:00:00Z");

	private final ItemServico trocaDeOleo = new ItemServico(UUID.randomUUID(), "Troca de óleo", Dinheiro.de("80.00"), 1);
	private final ItemPeca filtro = new ItemPeca(UUID.randomUUID(), "Filtro de óleo", Dinheiro.de("45.00"), 1);
	private final ItemPeca oleo = new ItemPeca(UUID.randomUUID(), "Óleo 5W30 (litro)", Dinheiro.de("38.00"), 4);

	@Test
	void osAbertaNasceRecebidaComOrcamentoEmElaboracao() {
		OrdemServico os = novaOS();

		assertThat(os.getStatus()).isEqualTo(StatusOS.RECEBIDA);
		assertThat(os.getOrcamentos()).hasSize(1);
		assertThat(os.orcamentoAtual().getNumero()).isEqualTo(1);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.EM_ELABORACAO);
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
		OrdemServico os = novaOS();

		os.iniciarDiagnostico();
		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);

		os.incluirServico(trocaDeOleo);
		os.concluirDiagnostico(AGORA);
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
		OrdemServico os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);
		os.incluirPeca(oleo);

		assertThat(os.orcamentoAtual().total()).isEqualTo(Dinheiro.de("277.00"));
	}

	@Test
	void concluirDiagnosticoEnviaOOrcamento() {
		OrdemServico os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);

		os.concluirDiagnostico(AGORA);

		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.PENDENTE);
		assertThat(os.orcamentoAtual().getEnviadoEm()).isEqualTo(AGORA);
	}

	@Test
	void naoConcluiDiagnosticoComOrcamentoVazio() {
		OrdemServico os = osEmDiagnostico();

		assertThatThrownBy(() -> os.concluirDiagnostico(AGORA)).isInstanceOf(RegraDeNegocioException.class);
		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
	}

	@Test
	void naoIncluiItemDepoisQueOOrcamentoFoiEnviado() {
		OrdemServico os = osAguardandoAprovacao();

		assertThatThrownBy(() -> os.incluirPeca(filtro)).isInstanceOf(RegraDeNegocioException.class);
		assertThatThrownBy(() -> os.incluirServico(trocaDeOleo)).isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void aprovacaoRegistraARespostaEIniciaAExecucao() {
		OrdemServico os = osAguardandoAprovacao();

		os.aprovarOrcamento(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_EXECUCAO);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.APROVADO);
		assertThat(os.orcamentoAtual().getRespondidoEm()).isEqualTo(AGORA);
	}

	@Test
	void rejeicaoRegistraARespostaELevaAOSParaRejeitada() {
		OrdemServico os = osAguardandoAprovacao();

		os.rejeitarOrcamento(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.REJEITADA);
		assertThat(os.orcamentoAtual().getSituacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
		assertThat(os.orcamentoAtual().getRespondidoEm()).isEqualTo(AGORA);
	}

	@Test
	void revisaoCriaNovoOrcamentoComAsLinhasDoAnteriorEPreservaOHistorico() {
		OrdemServico os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.incluirPeca(filtro);
		os.concluirDiagnostico(AGORA);
		os.rejeitarOrcamento(AGORA);

		os.revisarOrcamento();
		os.incluirPeca(oleo);

		assertThat(os.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
		assertThat(os.getOrcamentos()).hasSize(2);

		Orcamento rejeitado = os.getOrcamentos().getFirst();
		assertThat(rejeitado.getSituacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
		assertThat(rejeitado.getItensPeca()).containsExactly(filtro);
		assertThat(rejeitado.total()).isEqualTo(Dinheiro.de("125.00"));

		Orcamento revisado = os.orcamentoAtual();
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
		OrdemServico os = osAguardandoAprovacao();
		os.rejeitarOrcamento(AGORA);

		os.entregarVeiculo(AGORA);

		assertThat(os.getStatus()).isEqualTo(StatusOS.ENTREGUE);
		assertThat(os.tempoDeExecucao()).isEmpty();
	}

	@Test
	void transicoesForaDeOrdemSaoRecusadasEMantemOStatus() {
		OrdemServico recebida = novaOS();
		assertThatThrownBy(() -> recebida.concluirDiagnostico(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.aprovarOrcamento(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.rejeitarOrcamento(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.concluirServico(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> recebida.entregarVeiculo(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThat(recebida.getStatus()).isEqualTo(StatusOS.RECEBIDA);

		OrdemServico aguardando = osAguardandoAprovacao();
		assertThatThrownBy(aguardando::iniciarDiagnostico).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> aguardando.concluirServico(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThat(aguardando.getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
	}

	@Test
	void osEntregueNaoAceitaMaisNenhumComando() {
		OrdemServico os = osAguardandoAprovacao();
		os.aprovarOrcamento(AGORA);
		os.concluirServico(AGORA);
		os.entregarVeiculo(AGORA);

		assertThatThrownBy(os::iniciarDiagnostico).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(() -> os.entregarVeiculo(AGORA)).isInstanceOf(TransicaoInvalidaException.class);
		assertThatThrownBy(os::revisarOrcamento).isInstanceOf(TransicaoInvalidaException.class);
	}

	@Test
	void tempoDeExecucaoContaDaAprovacaoAteAConclusaoDoServico() {
		OrdemServico os = osAguardandoAprovacao();
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
		OrdemServico os = novaOS();
		os.iniciarDiagnostico();
		return os;
	}

	private OrdemServico osAguardandoAprovacao() {
		OrdemServico os = osEmDiagnostico();
		os.incluirServico(trocaDeOleo);
		os.concluirDiagnostico(AGORA);
		return os;
	}

}
