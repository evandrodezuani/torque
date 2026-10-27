package br.com.autosoft.torque.domain.cadastro.model;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import br.com.caelum.stella.validation.CNPJValidator;
import br.com.caelum.stella.validation.CPFValidator;

public record Documento(String numero) {

	public Documento {
		if (numero == null || numero.isBlank()) {
			throw new RegraDeNegocioException("O documento é obrigatório");
		}
		numero = numero.replaceAll("[.\\-/\\s]", "").toUpperCase();
		if (!valido(numero)) {
			throw new RegraDeNegocioException("CPF ou CNPJ inválido");
		}
	}

	private static boolean valido(String numero) {
		if (numero.length() == 11) {
			return new CPFValidator().invalidMessagesFor(numero).isEmpty();
		}
		if (numero.length() == 14) {
			return new CNPJValidator().invalidMessagesFor(numero).isEmpty();
		}
		return false;
	}

}
