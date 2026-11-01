package br.com.autosoft.torque.infrastructure.shared.security;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
class TokenService {

	static final String CLAIM_PAPEIS = "papeis";
	static final String CLAIM_CLIENTE_ID = "clienteId";
	private static final Duration VALIDADE = Duration.ofHours(2);

	private final JwtEncoder encoder;

	public TokenGerado gerar(UsuarioEntity usuario) {
		var agora = Instant.now().truncatedTo(ChronoUnit.SECONDS);
		var expiraEm = agora.plus(VALIDADE);
		var claims = JwtClaimsSet.builder()
				.issuer("torque")
				.subject(usuario.getId().toString())
				.issuedAt(agora)
				.expiresAt(expiraEm)
				.claim(CLAIM_PAPEIS, usuario.getPapeis().stream().map(Enum::name).sorted().toList());
		if (usuario.getClienteId() != null) {
			claims.claim(CLAIM_CLIENTE_ID, usuario.getClienteId().toString());
		}

		var cabecalho = JwsHeader.with(MacAlgorithm.HS256).build();
		var token = encoder.encode(JwtEncoderParameters.from(cabecalho, claims.build()));
		return new TokenGerado(token.getTokenValue(), expiraEm);
	}

}
