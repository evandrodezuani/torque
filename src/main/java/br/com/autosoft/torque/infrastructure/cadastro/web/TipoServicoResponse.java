package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;

record TipoServicoResponse(UUID id, String descricao) {

	static TipoServicoResponse paraResposta(TipoServico tipoServico) {
		return new TipoServicoResponse(tipoServico.getId(), tipoServico.getDescricao());
	}

}
