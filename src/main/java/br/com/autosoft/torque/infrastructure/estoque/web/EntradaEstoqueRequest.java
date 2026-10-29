package br.com.autosoft.torque.infrastructure.estoque.web;

import jakarta.validation.constraints.NotNull;

record EntradaEstoqueRequest(@NotNull Integer quantidade) {

}
