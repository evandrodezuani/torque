package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record DefinirSenhaRequest(

		@NotNull UUID token,
		@NotBlank @Size(min = 8, max = 72) String senha) {

}
