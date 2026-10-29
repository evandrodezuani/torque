package br.com.autosoft.torque.infrastructure.cadastro.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

record TipoServicoRequest(@NotBlank @Size(max = 100) String descricao) {

}
