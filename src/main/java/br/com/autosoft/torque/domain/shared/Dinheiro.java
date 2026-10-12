package br.com.autosoft.torque.domain.shared;

import java.math.BigDecimal;
import java.math.RoundingMode;

public record Dinheiro(BigDecimal valor) {

	public static final Dinheiro ZERO = new Dinheiro(BigDecimal.ZERO);

	public Dinheiro {
		if (valor == null) {
			throw new RegraDeNegocioException("O valor é obrigatório");
		}
		if (valor.signum() < 0) {
			throw new RegraDeNegocioException("O valor não pode ser negativo");
		}
		valor = valor.setScale(2, RoundingMode.HALF_EVEN);
	}

	public static Dinheiro de(String valor) {
		return new Dinheiro(new BigDecimal(valor));
	}

	public Dinheiro somar(Dinheiro outro) {
		return new Dinheiro(valor.add(outro.valor));
	}

	public Dinheiro multiplicar(int quantidade) {
		return new Dinheiro(valor.multiply(BigDecimal.valueOf(quantidade)));
	}

}
