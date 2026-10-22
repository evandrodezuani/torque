package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.time.Instant;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;

public record OrdemServicoResponse(UUID id, UUID clienteId, UUID veiculoId, StatusOS status, Instant abertaEm) {

    static OrdemServicoResponse paraResposta(OrdemServico ordemServico) {
        return new OrdemServicoResponse(ordemServico.getId(), ordemServico.getClienteId(), ordemServico.getVeiculoId(),
                ordemServico.getStatus(), ordemServico.getAbertaEm());
    }

}
