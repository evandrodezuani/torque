package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

record IncluirServicoRequest(@NotNull UUID servicoId, @NotNull Integer quantidade) {

}
