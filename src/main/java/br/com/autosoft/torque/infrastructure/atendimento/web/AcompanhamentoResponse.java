package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import br.com.autosoft.torque.application.atendimento.Acompanhamento;
import br.com.autosoft.torque.domain.atendimento.model.ItemOrcamento;
import br.com.autosoft.torque.domain.atendimento.model.SituacaoOrcamento;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;

/**
 * sem os ids internos da OS, do cliente, dos serviços e do estoque.
 */
record AcompanhamentoResponse(StatusOS status, Instant abertaEm, VeiculoResponse veiculo, OrcamentoResponse orcamento) {

    static AcompanhamentoResponse paraResposta(Acompanhamento acompanhamento) {
        var ordemServico = acompanhamento.ordemServico();
        var veiculo = acompanhamento.veiculo();
        var orcamento = ordemServico.orcamentoAtual();
        return new AcompanhamentoResponse(ordemServico.getStatus(), ordemServico.getAbertaEm(),
                new VeiculoResponse(veiculo.getPlaca().numero(), veiculo.getMarca(), veiculo.getModelo()),
                new OrcamentoResponse(orcamento.getNumero(), orcamento.getSituacao(),
                        orcamento.getItensServico().stream().map(ItemResponse::paraResposta).toList(),
                        orcamento.getItensPeca().stream().map(ItemResponse::paraResposta).toList(),
                        orcamento.total().valor()));
    }

    record VeiculoResponse(String placa, String marca, String modelo) {

    }

    record OrcamentoResponse(int numero, SituacaoOrcamento situacao, List<ItemResponse> servicos,
            List<ItemResponse> pecas, BigDecimal total) {

    }

    record ItemResponse(String descricao, int quantidade, BigDecimal precoUnitario, BigDecimal subtotal) {

        static ItemResponse paraResposta(ItemOrcamento<?> item) {
            return new ItemResponse(item.descricao(), item.quantidade(), item.precoUnitario().valor(),
                    item.subtotal().valor());
        }

    }

}
