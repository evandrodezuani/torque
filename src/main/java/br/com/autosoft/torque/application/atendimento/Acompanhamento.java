package br.com.autosoft.torque.application.atendimento;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;

/**
 *  ordem de serviço e o veículo.
 */
public record Acompanhamento(OrdemServico ordemServico, Veiculo veiculo) {

}
