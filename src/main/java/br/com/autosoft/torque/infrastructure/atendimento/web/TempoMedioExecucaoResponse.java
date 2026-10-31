package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;

import br.com.autosoft.torque.domain.atendimento.model.TempoMedioDeExecucao;

record TempoMedioExecucaoResponse(long ordensConsideradas, Long tempoMedioEmMinutos, BigDecimal tempoMedioEmDias,
		String tempoMedio) {

	private static final long MINUTOS_POR_DIA = 24 * 60;

	static TempoMedioExecucaoResponse paraResposta(TempoMedioDeExecucao tempoMedio) {
		if (tempoMedio.tempoMedio() == null) {
			return new TempoMedioExecucaoResponse(tempoMedio.ordensConsideradas(), null, null, null);
		}
		long minutos = Math.round(tempoMedio.tempoMedio().toSeconds() / 60.0);
		var dias = BigDecimal.valueOf(minutos).divide(BigDecimal.valueOf(MINUTOS_POR_DIA), 2, RoundingMode.HALF_UP);
		return new TempoMedioExecucaoResponse(tempoMedio.ordensConsideradas(), minutos, dias, paraTexto(minutos));
	}

	private static String paraTexto(long minutos) {
		long dias = minutos / MINUTOS_POR_DIA;
		long horas = minutos % MINUTOS_POR_DIA / 60;
		long restante = minutos % 60;

		var partes = new ArrayList<String>();
		if (dias > 0) {
			partes.add(dias + "d");
		}

		if (horas > 0) {
			partes.add(horas + "h");
		}

		if (restante > 0 || partes.isEmpty()) {
			partes.add(restante + "min");
		}

		return String.join(" ", partes);
	}

}
