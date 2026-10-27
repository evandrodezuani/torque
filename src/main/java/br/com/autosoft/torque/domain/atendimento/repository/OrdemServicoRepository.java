package br.com.autosoft.torque.domain.atendimento.repository;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;

import java.util.UUID;

public interface OrdemServicoRepository {

    void salvar(OrdemServico ordemServico);

    boolean existePorCliente(UUID clienteId);

}
