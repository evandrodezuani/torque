package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class AdminInicialTest {

	private final UsuarioJpaRepository repository = mock(UsuarioJpaRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Test
	void criaOAdminQuandoNaoHaNenhumAdminAtivo() {
		when(repository.existsByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(false);
		when(repository.findByLogin("admin@oficina.com")).thenReturn(Optional.empty());

		new AdminInicial(repository, passwordEncoder, " Admin@Oficina.com ", "s3nha-forte").run(null);

		var admin = salvo();
		assertThat(admin.getLogin()).isEqualTo("admin@oficina.com");
		assertThat(admin.getPapeis()).containsExactly(Papel.ADMIN);
		assertThat(admin.isAtivo()).isTrue();
		assertThat(admin.getSenha()).isNotEqualTo("s3nha-forte");
		assertThat(passwordEncoder.matches("s3nha-forte", admin.getSenha())).isTrue();
	}

	@Test
	void reativaOAdminDesativadoComASenhaNova() {
		var antigo = new UsuarioEntity();
		antigo.setId(UUID.randomUUID());
		antigo.setLogin("admin@oficina.com");
		antigo.setSenha(passwordEncoder.encode("senha-esquecida"));
		antigo.setAtivo(false);
		antigo.setPapeis(new HashSet<>(Set.of(Papel.ADMIN)));
		when(repository.existsByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(false);
		when(repository.findByLogin("admin@oficina.com")).thenReturn(Optional.of(antigo));

		new AdminInicial(repository, passwordEncoder, "admin@oficina.com", "senha-nova").run(null);

		var admin = salvo();
		assertThat(admin.getId()).isEqualTo(antigo.getId());
		assertThat(admin.isAtivo()).isTrue();
		assertThat(passwordEncoder.matches("senha-nova", admin.getSenha())).isTrue();
	}

	@Test
	void naoMexeEmNadaQuandoJaExisteUmAdminAtivo() {
		when(repository.existsByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(true);

		new AdminInicial(repository, passwordEncoder, "admin@oficina.com", "s3nha-forte").run(null);

		verify(repository, never()).save(any());
	}

	@Test
	void naoCriaSemOEmailOuASenhaNasVariaveis() {
		when(repository.existsByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(false);

		new AdminInicial(repository, passwordEncoder, "admin@oficina.com", "").run(null);
		new AdminInicial(repository, passwordEncoder, "", "s3nha-forte").run(null);

		verify(repository, never()).save(any());
	}

	private UsuarioEntity salvo() {
		var salvo = ArgumentCaptor.forClass(UsuarioEntity.class);
		verify(repository).save(salvo.capture());
		return salvo.getValue();
	}

}
