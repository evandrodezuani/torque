package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class UsuariosDeDemonstracaoTest {

	private final UsuarioJpaRepository repository = mock(UsuarioJpaRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	@Test
	void criaUmUsuarioDeCadaPapelDaOficina() {
		new UsuariosDeDemonstracao(repository, passwordEncoder, "s3nha-demo").run(null);

		var salvos = ArgumentCaptor.forClass(UsuarioEntity.class);
		verify(repository, times(4)).save(salvos.capture());
		assertThat(salvos.getAllValues())
				.extracting(UsuarioEntity::getLogin)
				.containsExactly("gerente@torque.local", "atendente@torque.local", "mecanico@torque.local",
						"almoxarife@torque.local");
		assertThat(salvos.getAllValues())
				.extracting(usuario -> usuario.getPapeis().iterator().next())
				.containsExactly(Papel.GERENTE, Papel.ATENDENTE, Papel.MECANICO, Papel.ALMOXARIFE);
		assertThat(salvos.getAllValues()).allSatisfy(usuario -> {
			assertThat(usuario.isAtivo()).isTrue();
			assertThat(passwordEncoder.matches("s3nha-demo", usuario.getSenha())).isTrue();
		});
	}

	@Test
	void voltaAoPadraoOUsuarioQueFoiAlterado() {
		var mecanico = new UsuarioEntity();
		mecanico.setId(UUID.randomUUID());
		mecanico.setLogin("mecanico@torque.local");
		mecanico.setSenha(passwordEncoder.encode("outra-senha"));
		mecanico.setAtivo(false);
		mecanico.setPapeis(new HashSet<>(Set.of(Papel.MECANICO, Papel.ALMOXARIFE)));
		when(repository.findByLogin("mecanico@torque.local")).thenReturn(Optional.of(mecanico));

		new UsuariosDeDemonstracao(repository, passwordEncoder, "s3nha-demo").run(null);

		assertThat(mecanico.isAtivo()).isTrue();
		assertThat(mecanico.getPapeis()).containsExactly(Papel.MECANICO);
		assertThat(passwordEncoder.matches("s3nha-demo", mecanico.getSenha())).isTrue();
		verify(repository).save(mecanico);
	}

	@Test
	void naoCriaSemASenhaNaVariavel() {
		new UsuariosDeDemonstracao(repository, passwordEncoder, "").run(null);

		verify(repository, never()).save(any());
	}

}
