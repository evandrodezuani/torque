package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

record IncluirPecaRequest(@NotNull UUID itemEstoqueId, @NotNull Integer quantidade) {

}
