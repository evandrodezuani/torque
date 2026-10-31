package br.com.autosoft.torque.domain.atendimento.model;

import java.time.Duration;

/**
 * Média do tempo entre a aprovação do orçamento e a conclusão do serviço, entre as OS concluídas
 */
public record TempoMedioDeExecucao(long ordensConsideradas, Duration tempoMedio) {

}
