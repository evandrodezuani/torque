package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import java.util.List;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    List<OrdemServicoEntity> findByStatus(StatusOS status, Sort sort);

    boolean existsByClienteIdAndStatusNot(UUID clienteId, StatusOS status);

    boolean existsByVeiculoIdAndStatusNot(UUID veiculoId, StatusOS status);

    boolean existsByOrcamentosItensServicoServicoId(UUID servicoId);

    boolean existsByOrcamentosItensPecaItemEstoqueId(UUID itemEstoqueId);

    void deleteByClienteId(UUID clienteId);

    void deleteByVeiculoId(UUID veiculoId);

}
