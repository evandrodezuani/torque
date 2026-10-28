package br.com.autosoft.torque.domain.cadastro.model;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

public record Placa(String numero) {

    public Placa {
        if (numero == null || numero.isBlank()) {
            throw new RegraDeNegocioException("A placa é obrigatória");
        }
        numero = numero.replaceAll("[-\\s]", "").toUpperCase();
        if (!numero.matches("[A-Z]{3}[0-9][A-Z0-9][0-9]{2}")) {
            throw new RegraDeNegocioException("Placa inválida");
        }
    }

}
