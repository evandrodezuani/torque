package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;

@SpringBootTest
@Transactional
class UsuarioJpaRepositoryTest {

	@Autowired
	private UsuarioJpaRepository repository;

	@Autowired
	private EntityManager entityManager;

	@Test
	void guardaOUsuarioComMaisDeUmPapel() {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin("joao.silva@oficina.com");
		usuario.setSenha("hash-da-senha");
		usuario.setAtivo(true);
		usuario.setPapeis(Set.of(Papel.ATENDENTE, Papel.MECANICO));
		repository.save(usuario);
		entityManager.flush();
		entityManager.clear();

		var lido = repository.findByLogin("joao.silva@oficina.com").orElseThrow();

		assertThat(lido.getPapeis()).containsExactlyInAnyOrder(Papel.ATENDENTE, Papel.MECANICO);
		assertThat(lido.getClienteId()).isNull();
		assertThat(repository.existsByLogin("joao.silva@oficina.com")).isTrue();
		assertThat(repository.existsByLogin("maria@oficina.com")).isFalse();
	}

}
