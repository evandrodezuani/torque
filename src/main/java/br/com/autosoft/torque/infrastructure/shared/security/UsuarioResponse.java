package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.List;
import java.util.UUID;

record UsuarioResponse(UUID id, String email, List<Papel> papeis, boolean ativo) {

	static UsuarioResponse paraResposta(UsuarioEntity usuario) {
		return new UsuarioResponse(usuario.getId(), usuario.getLogin(), usuario.getPapeis().stream().sorted().toList(),
				usuario.isAtivo());
	}

}
