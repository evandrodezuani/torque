package br.com.autosoft.torque.infrastructure.cadastro.web;

import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record VeiculoRequest(
        @NotBlank String placa,
        @NotNull TipoVeiculo tipo,
        @NotBlank @Size(max = 60) String marca,
        @NotBlank @Size(max = 60) String modelo,
        @NotNull Integer ano) {

}
