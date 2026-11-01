package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class UsuarioServiceTest {

	private final UsuarioJpaRepository repository = mock(UsuarioJpaRepository.class);
	private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final UsuarioService usuarioService = new UsuarioService(repository, clienteRepository, passwordEncoder);

	@Test
	void cadastraOFuncionarioComOEmailNormalizadoEASenhaCriptografada() {
		when(repository.save(any())).thenAnswer(chamada -> chamada.getArgument(0));

		var usuario = usuarioService.cadastrar(" Joao@Oficina.com ", "s3nha-do-joao", Set.of(Papel.MECANICO));

		assertThat(usuario.getLogin()).isEqualTo("joao@oficina.com");
		assertThat(usuario.getPapeis()).containsExactly(Papel.MECANICO);
		assertThat(usuario.isAtivo()).isTrue();
		assertThat(usuario.getClienteId()).isNull();
		assertThat(passwordEncoder.matches("s3nha-do-joao", usuario.getSenha())).isTrue();
	}

	@Test
	void recusaEmailRepetidoOuDeCliente() {
		when(repository.existsByLogin("joao@oficina.com")).thenReturn(true);
		when(clienteRepository.existePorEmail("maria@email.com")).thenReturn(true);

		assertThatThrownBy(() -> usuarioService.cadastrar("joao@oficina.com", "s3nha-do-joao", Set.of(Papel.MECANICO)))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Já existe usuário com este e-mail");
		assertThatThrownBy(() -> usuarioService.cadastrar("maria@email.com", "s3nha-da-maria", Set.of(Papel.ATENDENTE)))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("Este e-mail é de um cliente");
		verify(repository, never()).save(any());
	}

	@Test
	void naoDaOPapelDeCliente() {
		assertThatThrownBy(() -> usuarioService.cadastrar("joao@oficina.com", "s3nha-do-joao", Set.of(Papel.CLIENTE)))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O papel CLIENTE é dado pelo cadastro do cliente");
	}

	@Test
	void alteraOsPapeisDesativaETrocaASenhaSoQuandoInformada() {
		var joao = funcionario(Papel.MECANICO);
		var senhaAntiga = joao.getSenha();
		when(repository.findById(joao.getId())).thenReturn(Optional.of(joao));

		usuarioService.alterar(joao.getId(), Set.of(Papel.MECANICO, Papel.ALMOXARIFE), false, null);

		assertThat(joao.getPapeis()).containsExactlyInAnyOrder(Papel.MECANICO, Papel.ALMOXARIFE);
		assertThat(joao.isAtivo()).isFalse();
		assertThat(joao.getSenha()).isEqualTo(senhaAntiga);

		usuarioService.alterar(joao.getId(), Set.of(Papel.MECANICO), true, "s3nha-nova");
		assertThat(passwordEncoder.matches("s3nha-nova", joao.getSenha())).isTrue();
	}

	@Test
	void naoDesativaNemTiraOPapelDoUltimoAdmin() {
		var admin = funcionario(Papel.ADMIN);
		when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
		when(repository.countByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(1L);

		assertThatThrownBy(() -> usuarioService.alterar(admin.getId(), Set.of(Papel.ADMIN), false, null))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("É preciso manter pelo menos um ADMIN ativo");
		assertThatThrownBy(() -> usuarioService.alterar(admin.getId(), Set.of(Papel.GERENTE), true, null))
				.isInstanceOf(RegraDeNegocioException.class);
		assertThat(admin.isAtivo()).isTrue();
	}

	@Test
	void desativaUmAdminQuandoHaOutroAtivo() {
		var admin = funcionario(Papel.ADMIN);
		when(repository.findById(admin.getId())).thenReturn(Optional.of(admin));
		when(repository.countByPapeisContainingAndAtivoTrue(Papel.ADMIN)).thenReturn(2L);

		usuarioService.alterar(admin.getId(), Set.of(Papel.ADMIN), false, null);

		assertThat(admin.isAtivo()).isFalse();
	}

	@Test
	void oUsuarioDoClienteNaoEGeridoAqui() {
		var doCliente = funcionario(Papel.CLIENTE);
		doCliente.setClienteId(UUID.randomUUID());
		when(repository.findById(doCliente.getId())).thenReturn(Optional.of(doCliente));

		assertThatThrownBy(() -> usuarioService.consultar(doCliente.getId()))
				.isInstanceOf(NaoEncontradoException.class)
				.hasMessage("Usuário não encontrado");
	}

	private UsuarioEntity funcionario(Papel papel) {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin("joao@oficina.com");
		usuario.setSenha(passwordEncoder.encode("s3nha-do-joao"));
		usuario.setAtivo(true);
		usuario.setPapeis(new HashSet<>(Set.of(papel)));
		return usuario;
	}

}
