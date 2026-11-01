package br.com.autosoft.torque.infrastructure.shared.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import java.util.UUID;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

public final class UsuariosDeTeste {

	private UsuariosDeTeste() {
	}

	public static RequestPostProcessor admin() {
		return com(Papel.ADMIN);
	}

	public static RequestPostProcessor gerente() {
		return com(Papel.GERENTE);
	}

	public static RequestPostProcessor atendente() {
		return com(Papel.ATENDENTE);
	}

	public static RequestPostProcessor mecanico() {
		return com(Papel.MECANICO);
	}

	public static RequestPostProcessor almoxarife() {
		return com(Papel.ALMOXARIFE);
	}

	// Como o token de verdade, o do cliente leva o id do cadastro dele.
	public static RequestPostProcessor cliente(String clienteId) {
		return jwt().jwt(token -> token.claim(TokenService.CLAIM_CLIENTE_ID, clienteId))
				.authorities(new SimpleGrantedAuthority("ROLE_" + Papel.CLIENTE.name()));
	}

	public static RequestPostProcessor com(Papel papel) {
		if (papel == Papel.CLIENTE) {
			return cliente(UUID.randomUUID().toString());
		}
		return jwt().authorities(new SimpleGrantedAuthority("ROLE_" + papel.name()));
	}

}
