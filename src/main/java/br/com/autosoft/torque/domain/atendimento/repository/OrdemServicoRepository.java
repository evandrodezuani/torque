package br.com.autosoft.torque.domain.atendimento.repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;

import java.util.UUID;

public interface OrdemServicoRepository {

    void salvar(OrdemServico ordemServico);

    boolean existeEmAndamentoPorCliente(UUID clienteId);

    boolean existeEmAndamentoPorVeiculo(UUID veiculoId);

    void removerPorCliente(UUID clienteId);

    void removerPorVeiculo(UUID veiculoId);

}
