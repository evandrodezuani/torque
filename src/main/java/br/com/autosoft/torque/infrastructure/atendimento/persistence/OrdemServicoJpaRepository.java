package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface OrdemServicoJpaRepository extends JpaRepository<OrdemServicoEntity, UUID> {

    boolean existsByClienteId(UUID clienteId);

}
