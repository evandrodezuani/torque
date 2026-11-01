package br.com.autosoft.torque.infrastructure.shared.security;

import jakarta.validation.constraints.NotBlank;

record LoginRequest(

		@NotBlank String email,
		@NotBlank String senha) {

}
