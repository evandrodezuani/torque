package br.com.autosoft.torque.domain.atendimento.repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.model.TempoMedioDeExecucao;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    void salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID id);

    Optional<OrdemServico> buscarPorCodigoAcompanhamento(UUID codigoAcompanhamento);

    /**
     * Ordena pela data de abertura. Com o status nulo, lista as ordens de serviço de todos os status.
     */
    List<OrdemServico> listar(StatusOS status, boolean maisAntigasPrimeiro);

    /**
     * Considera as OS com o serviço concluído a partir de {@code concluidasDesde} e antes de
     * {@code concluidasAntesDe}, do tipo de veículo informado
     */
    TempoMedioDeExecucao tempoMedioDeExecucao(TipoVeiculo tipoVeiculo, Instant concluidasDesde,
            Instant concluidasAntesDe);

    boolean existeEmAndamentoPorCliente(UUID clienteId);

    boolean existeEmAndamentoPorVeiculo(UUID veiculoId);

    boolean existeComServico(UUID servicoId);

    boolean existeComItemEstoque(UUID itemEstoqueId);

    void removerPorCliente(UUID clienteId);

    void removerPorVeiculo(UUID veiculoId);

}
