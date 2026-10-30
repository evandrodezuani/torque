package br.com.autosoft.torque.domain.atendimento.repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    void salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID id);

    /**
     * Ordena pela data de abertura. Com o status nulo, lista as ordens de serviço de todos os status.
     */
    List<OrdemServico> listar(StatusOS status, boolean maisAntigasPrimeiro);

    boolean existeEmAndamentoPorCliente(UUID clienteId);

    boolean existeEmAndamentoPorVeiculo(UUID veiculoId);

    boolean existeComServico(UUID servicoId);

    boolean existeComItemEstoque(UUID itemEstoqueId);

    void removerPorCliente(UUID clienteId);

    void removerPorVeiculo(UUID veiculoId);

}
