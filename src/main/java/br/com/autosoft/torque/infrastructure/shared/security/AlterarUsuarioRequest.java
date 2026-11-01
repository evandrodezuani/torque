package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.Set;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

record AlterarUsuarioRequest(

		@NotEmpty Set<Papel> papeis,
		@NotNull Boolean ativo,
		@Size(min = 8, max = 72) String senha) {

}
