package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface UsuarioJpaRepository extends JpaRepository<UsuarioEntity, UUID> {

	Optional<UsuarioEntity> findByLogin(String login);

	Optional<UsuarioEntity> findByClienteId(UUID clienteId);

	Optional<UsuarioEntity> findByTokenSenha(UUID tokenSenha);

	boolean existsByLogin(String login);

	boolean existsByPapeisContainingAndAtivoTrue(Papel papel);

	void deleteByClienteId(UUID clienteId);

	List<UsuarioEntity> findByClienteIdIsNull(Sort sort);

	long countByPapeisContainingAndAtivoTrue(Papel papel);

}
