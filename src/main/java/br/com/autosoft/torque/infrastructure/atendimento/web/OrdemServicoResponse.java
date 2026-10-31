package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.model.Orcamento;
import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.SituacaoOrcamento;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;

public record OrdemServicoResponse(UUID id, UUID codigoAcompanhamento, UUID clienteId, UUID veiculoId,
        StatusOS status, Instant abertaEm, OrcamentoResponse orcamento) {

    static OrdemServicoResponse paraResposta(OrdemServico ordemServico) {
        return new OrdemServicoResponse(ordemServico.getId(), ordemServico.getCodigoAcompanhamento(),
                ordemServico.getClienteId(), ordemServico.getVeiculoId(), ordemServico.getStatus(),
                ordemServico.getAbertaEm(),
                OrcamentoResponse.paraResposta(ordemServico.orcamentoAtual()));
    }

    record OrcamentoResponse(int numero, SituacaoOrcamento situacao, List<ItemServicoResponse> servicos,
            List<ItemPecaResponse> pecas, BigDecimal total) {

        static OrcamentoResponse paraResposta(Orcamento orcamento) {
            var servicos = orcamento.getItensServico().stream()
                    .map(item -> new ItemServicoResponse(item.servicoId(), item.descricao(),
                            item.precoUnitario().valor(), item.quantidade(), item.subtotal().valor()))
                    .toList();
            var pecas = orcamento.getItensPeca().stream()
                    .map(item -> new ItemPecaResponse(item.itemEstoqueId(), item.descricao(),
                            item.precoUnitario().valor(), item.quantidade(), item.subtotal().valor()))
                    .toList();
            return new OrcamentoResponse(orcamento.getNumero(), orcamento.getSituacao(), servicos, pecas,
                    orcamento.total().valor());
        }

    }

    record ItemServicoResponse(UUID servicoId, String descricao, BigDecimal precoUnitario, int quantidade,
            BigDecimal subtotal) {

    }

    record ItemPecaResponse(UUID itemEstoqueId, String descricao, BigDecimal precoUnitario, int quantidade,
            BigDecimal subtotal) {

    }

}
