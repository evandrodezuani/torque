package br.com.autosoft.torque.infrastructure.estoque.persistence;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ItemEstoqueJpaRepository extends JpaRepository<ItemEstoqueEntity, UUID> {

	boolean existsByDescricao(String descricao);

}
