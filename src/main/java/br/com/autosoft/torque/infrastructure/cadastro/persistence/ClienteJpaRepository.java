package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface ClienteJpaRepository extends JpaRepository<ClienteEntity, UUID> {

	boolean existsByDocumento(String documento);

	boolean existsByEmail(String email);

	Optional<ClienteEntity> findByEmail(String email);

	Optional<ClienteEntity> findByDocumento(String documento);

}
