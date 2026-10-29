package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;

record ServicoResponse(UUID id, UUID tipoServicoId, TipoVeiculo tipoVeiculo, BigDecimal preco, boolean ativo) {

	static ServicoResponse paraResposta(Servico servico) {
		return new ServicoResponse(servico.getId(), servico.getTipoServicoId(), servico.getTipoVeiculo(),
				servico.getPreco().valor(), servico.isAtivo());
	}

}
