package br.com.autosoft.torque.infrastructure.shared.security;

import jakarta.validation.constraints.NotBlank;

record NovoLinkRequest(@NotBlank String emailOuDocumento) {

}
