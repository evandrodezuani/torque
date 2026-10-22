package br.com.autosoft.torque.infrastructure.atendimento.web;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AbrirOrdemServicoRequest(@NotNull UUID clienteId, @NotNull UUID veiculoId) {

}
