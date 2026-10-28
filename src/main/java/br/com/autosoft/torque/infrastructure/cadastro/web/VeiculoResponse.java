package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Veiculo;

record VeiculoResponse(UUID id, UUID clienteId, String placa, String marca, String modelo, int ano) {

    static VeiculoResponse paraResposta(Veiculo veiculo) {
        return new VeiculoResponse(veiculo.getId(), veiculo.getClienteId(), veiculo.getPlaca().numero(),
                veiculo.getMarca(), veiculo.getModelo(), veiculo.getAno());
    }

}
