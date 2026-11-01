package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.Optional;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

/**
 * O id do cadastro de quem está logado como cliente, lido do token. Para um funcionário, vazio.
 */
public final class ClienteLogado {

	private static final String PAPEL_CLIENTE = "ROLE_" + Papel.CLIENTE.name();

	private ClienteLogado() {
	}

	public static Optional<UUID> id(Authentication autenticacao) {
		var ehCliente = autenticacao.getAuthorities().stream()
				.anyMatch(autoridade -> autoridade.getAuthority().equals(PAPEL_CLIENTE));
		if (!ehCliente) {
			return Optional.empty();
		}
		// Um token de cliente sem o clienteId não pode virar um acesso de funcionário.
		if (autenticacao instanceof JwtAuthenticationToken token && token.getToken().hasClaim(TokenService.CLAIM_CLIENTE_ID)) {
			return Optional.of(UUID.fromString(token.getToken().getClaimAsString(TokenService.CLAIM_CLIENTE_ID)));
		}
		throw new AccessDeniedException("Token de cliente sem o cadastro do cliente");
	}

}
