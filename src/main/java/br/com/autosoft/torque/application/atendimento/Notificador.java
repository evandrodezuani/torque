package br.com.autosoft.torque.application.atendimento;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;

/**
 * Avisa o cliente de que o orçamento está disponível.
 */
public interface Notificador {

	void orcamentoDisponivel(Cliente cliente, Acompanhamento acompanhamento);

}
