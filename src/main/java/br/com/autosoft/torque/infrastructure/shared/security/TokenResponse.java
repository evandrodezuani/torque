package br.com.autosoft.torque.infrastructure.shared.security;

import java.time.Instant;

record TokenResponse(String token, String tipo, Instant expiraEm) {

	static TokenResponse paraResposta(TokenGerado token) {
		return new TokenResponse(token.valor(), "Bearer", token.expiraEm());
	}

}
