package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.Set;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.JwtException;

class TokenServiceTest {

	private final SegurancaConfig config = new SegurancaConfig();
	private final SecretKey chave = config.chaveDoToken("segredo-falso-so-para-os-testes-com-32-bytes-ou-mais");
	private final TokenService tokenService = new TokenService(config.jwtEncoder(chave));

	@Test
	void geraUmTokenDeDuasHorasComOsPapeisDoFuncionario() {
		var usuario = usuario(Set.of(Papel.MECANICO, Papel.ATENDENTE));

		var gerado = tokenService.gerar(usuario);
		var jwt = config.jwtDecoder(chave).decode(gerado.valor());

		assertThat(jwt.getSubject()).isEqualTo(usuario.getId().toString());
		assertThat(jwt.getClaimAsStringList("papeis")).containsExactly("ATENDENTE", "MECANICO");
		assertThat(jwt.hasClaim("clienteId")).isFalse();
		assertThat(Duration.between(jwt.getIssuedAt(), jwt.getExpiresAt())).isEqualTo(Duration.ofHours(2));
		assertThat(jwt.getExpiresAt()).isEqualTo(gerado.expiraEm());
	}

	@Test
	void levaOClienteIdNoTokenDoCliente() {
		var usuario = usuario(Set.of(Papel.CLIENTE));
		usuario.setClienteId(UUID.randomUUID());

		var jwt = config.jwtDecoder(chave).decode(tokenService.gerar(usuario).valor());

		assertThat(jwt.getClaimAsString("clienteId")).isEqualTo(usuario.getClienteId().toString());
	}

	@Test
	void recusaOTokenAssinadoComOutroSegredo() {
		var outraChave = config.chaveDoToken("um-outro-segredo-qualquer-com-32-bytes-ou-mais");
		var token = new TokenService(config.jwtEncoder(outraChave)).gerar(usuario(Set.of(Papel.ADMIN))).valor();

		assertThatThrownBy(() -> config.jwtDecoder(chave).decode(token)).isInstanceOf(JwtException.class);
	}

	@Test
	void naoAceitaUmSegredoCurto() {
		assertThatThrownBy(() -> config.chaveDoToken("curto"))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("JWT_SECRET precisa ter pelo menos 32 caracteres");
	}

	private UsuarioEntity usuario(Set<Papel> papeis) {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setPapeis(papeis);
		return usuario;
	}

}
