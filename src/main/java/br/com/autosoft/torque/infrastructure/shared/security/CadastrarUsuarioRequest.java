package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

record CadastrarUsuarioRequest(

		@NotBlank @Email @Size(max = 100) String email,
		@NotBlank @Size(min = 8, max = 72) String senha,
		@NotEmpty Set<Papel> papeis) {

}
