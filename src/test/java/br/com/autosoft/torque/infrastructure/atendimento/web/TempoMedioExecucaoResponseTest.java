package br.com.autosoft.torque.infrastructure.atendimento.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Duration;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.atendimento.model.TempoMedioDeExecucao;

class TempoMedioExecucaoResponseTest {

	@Test
	void mostraMinutosDiasETexto() {
		var resposta = TempoMedioExecucaoResponse.paraResposta(new TempoMedioDeExecucao(12, Duration.ofMinutes(2000)));

		assertThat(resposta.ordensConsideradas()).isEqualTo(12);
		assertThat(resposta.tempoMedioEmMinutos()).isEqualTo(2000);
		assertThat(resposta.tempoMedioEmDias()).isEqualByComparingTo(new BigDecimal("1.39"));
		assertThat(resposta.tempoMedio()).isEqualTo("1d 9h 20min");
	}

	@Test
	void omiteAsPartesQueSaoZero() {
		assertThat(texto(Duration.ofMinutes(45))).isEqualTo("45min");
		assertThat(texto(Duration.ofMinutes(200))).isEqualTo("3h 20min");
		assertThat(texto(Duration.ofHours(2))).isEqualTo("2h");
		assertThat(texto(Duration.ofHours(36))).isEqualTo("1d 12h");
		assertThat(texto(Duration.ofDays(2))).isEqualTo("2d");
	}

	@Test
	void diasComDuasCasasDecimais() {
		assertThat(dias(Duration.ofHours(36))).isEqualByComparingTo(new BigDecimal("1.50"));
		assertThat(dias(Duration.ofMinutes(45))).isEqualByComparingTo(new BigDecimal("0.03"));
	}

	@Test
	void arredondaOsSegundosParaOMinutoMaisProximo() {
		assertThat(texto(Duration.ofSeconds(89))).isEqualTo("1min");
		assertThat(texto(Duration.ofSeconds(29))).isEqualTo("0min");
	}

	@Test
	void semOsConcluidaOsTemposFicamNulos() {
		var resposta = TempoMedioExecucaoResponse.paraResposta(new TempoMedioDeExecucao(0, null));

		assertThat(resposta.ordensConsideradas()).isZero();
		assertThat(resposta.tempoMedioEmMinutos()).isNull();
		assertThat(resposta.tempoMedioEmDias()).isNull();
		assertThat(resposta.tempoMedio()).isNull();
	}

	private String texto(Duration tempo) {
		return TempoMedioExecucaoResponse.paraResposta(new TempoMedioDeExecucao(1, tempo)).tempoMedio();
	}

	private BigDecimal dias(Duration tempo) {
		return TempoMedioExecucaoResponse.paraResposta(new TempoMedioDeExecucao(1, tempo)).tempoMedioEmDias();
	}

}
