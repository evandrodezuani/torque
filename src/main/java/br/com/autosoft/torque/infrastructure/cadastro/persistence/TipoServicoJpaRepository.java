package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface TipoServicoJpaRepository extends JpaRepository<TipoServicoEntity, UUID> {

	boolean existsByDescricao(String descricao);

}
