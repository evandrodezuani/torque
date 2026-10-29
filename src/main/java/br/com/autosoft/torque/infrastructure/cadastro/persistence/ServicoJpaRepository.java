package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;

interface ServicoJpaRepository extends JpaRepository<ServicoEntity, UUID> {

	boolean existsByTipoServicoIdAndTipoVeiculo(UUID tipoServicoId, TipoVeiculo tipoVeiculo);

	boolean existsByTipoServicoId(UUID tipoServicoId);

}
