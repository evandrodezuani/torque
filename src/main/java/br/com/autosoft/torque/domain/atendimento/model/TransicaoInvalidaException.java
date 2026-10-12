package br.com.autosoft.torque.domain.atendimento.model;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

public class TransicaoInvalidaException extends RegraDeNegocioException {

	public TransicaoInvalidaException(String mensagem) {
		super(mensagem);
	}

}
