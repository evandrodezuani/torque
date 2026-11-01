package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;

class AutenticacaoServiceTest {

	private final UsuarioJpaRepository usuarioRepository = mock(UsuarioJpaRepository.class);
	private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final TokenService tokenService = mock(TokenService.class);
	private final AutenticacaoService autenticacaoService = new AutenticacaoService(usuarioRepository,
			clienteRepository, passwordEncoder, tokenService);

	private final TokenGerado token = new TokenGerado("um-token", Instant.now());

	@Test
	void entraOFuncionarioPeloEmail() {
		var mecanico = usuario("joao@oficina.com", "s3nha", Papel.MECANICO);
		when(usuarioRepository.findByLogin("joao@oficina.com")).thenReturn(Optional.of(mecanico));
		when(tokenService.gerar(mecanico)).thenReturn(token);

		assertThat(autenticacaoService.entrar(" Joao@Oficina.com ", "s3nha")).isEqualTo(token);
	}

	@Test
	void entraOClientePeloEmailDoCadastro() {
		var maria = Cliente.cadastrar(new Documento("529.982.247-25"), "Maria Souza", "maria@email.com", null);
		var usuario = usuario("52998224725", "s3nha", Papel.CLIENTE);
		usuario.setClienteId(maria.getId());
		when(usuarioRepository.findByLogin("maria@email.com")).thenReturn(Optional.empty());
		when(clienteRepository.buscarPorEmail("maria@email.com")).thenReturn(Optional.of(maria));
		when(usuarioRepository.findByClienteId(maria.getId())).thenReturn(Optional.of(usuario));
		when(tokenService.gerar(usuario)).thenReturn(token);

		assertThat(autenticacaoService.entrar("maria@email.com", "s3nha")).isEqualTo(token);
	}

	@Test
	void recusaASenhaErrada() {
		when(usuarioRepository.findByLogin("joao@oficina.com"))
				.thenReturn(Optional.of(usuario("joao@oficina.com", "s3nha", Papel.MECANICO)));

		assertRecusado("joao@oficina.com", "outra-senha");
	}

	@Test
	void recusaOUsuarioInativo() {
		var inativo = usuario("joao@oficina.com", "s3nha", Papel.MECANICO);
		inativo.setAtivo(false);
		when(usuarioRepository.findByLogin("joao@oficina.com")).thenReturn(Optional.of(inativo));

		assertRecusado("joao@oficina.com", "s3nha");
	}

	@Test
	void recusaOClienteQueAindaNaoDefiniuASenha() {
		var maria = Cliente.cadastrar(new Documento("529.982.247-25"), "Maria Souza", "maria@email.com", null);
		var semSenha = usuario("52998224725", null, Papel.CLIENTE);
		when(clienteRepository.buscarPorEmail("maria@email.com")).thenReturn(Optional.of(maria));
		when(usuarioRepository.findByClienteId(maria.getId())).thenReturn(Optional.of(semSenha));

		assertRecusado("maria@email.com", "qualquer");
	}

	@Test
	void recusaOEmailQueNaoExiste() {
		assertRecusado("ninguem@email.com", "s3nha");
	}

	private void assertRecusado(String email, String senha) {
		assertThatThrownBy(() -> autenticacaoService.entrar(email, senha))
				.isInstanceOf(BadCredentialsException.class)
				.hasMessage("E-mail ou senha inválidos");
		verify(tokenService, never()).gerar(any());
	}

	private UsuarioEntity usuario(String login, String senha, Papel papel) {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin(login);
		usuario.setSenha(senha == null ? null : passwordEncoder.encode(senha));
		usuario.setAtivo(true);
		usuario.setPapeis(Set.of(papel));
		return usuario;
	}

}
