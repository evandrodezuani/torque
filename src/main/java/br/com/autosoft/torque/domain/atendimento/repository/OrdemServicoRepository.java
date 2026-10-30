package br.com.autosoft.torque.domain.atendimento.repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrdemServicoRepository {

    void salvar(OrdemServico ordemServico);

    Optional<OrdemServico> buscarPorId(UUID id);

    List<OrdemServico> listar();

    boolean existeEmAndamentoPorCliente(UUID clienteId);

    boolean existeEmAndamentoPorVeiculo(UUID veiculoId);

    void removerPorCliente(UUID clienteId);

    void removerPorVeiculo(UUID veiculoId);

}
