package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class AcessoDoClienteServiceTest {

	private final UsuarioJpaRepository usuarioRepository = mock(UsuarioJpaRepository.class);
	private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
	private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
	private final ApplicationEventPublisher eventos = mock(ApplicationEventPublisher.class);
	private final AcessoDoClienteService acessoDoCliente = new AcessoDoClienteService(usuarioRepository,
			clienteRepository, passwordEncoder, eventos);

	private final Cliente maria = Cliente.cadastrar(new Documento("529.982.247-25"), "Maria Souza", "maria@email.com",
			null);

	@Test
	void liberaOClienteSemSenhaEComUmLinkDe24Horas() {
		acessoDoCliente.liberar(maria);

		var usuario = salvo();
		assertThat(usuario.getLogin()).isEqualTo("52998224725");
		assertThat(usuario.getClienteId()).isEqualTo(maria.getId());
		assertThat(usuario.getPapeis()).containsExactly(Papel.CLIENTE);
		assertThat(usuario.isAtivo()).isTrue();
		assertThat(usuario.getSenha()).isNull();
		assertThat(usuario.getTokenSenha()).isNotNull();
		assertThat(usuario.getTokenSenhaExpiraEm()).isCloseTo(Instant.now().plus(Duration.ofHours(24)),
				within(1, ChronoUnit.MINUTES));
		verify(eventos).publishEvent(new LinkDeSenhaGerado(maria.getId(), usuario.getTokenSenha()));
	}

	@Test
	void defineASenhaEInvalidaOLink() {
		var usuario = usuarioDaMaria();
		when(usuarioRepository.findByTokenSenha(usuario.getTokenSenha())).thenReturn(Optional.of(usuario));

		acessoDoCliente.definirSenha(usuario.getTokenSenha(), "s3nha-forte");

		assertThat(passwordEncoder.matches("s3nha-forte", usuario.getSenha())).isTrue();
		assertThat(usuario.getTokenSenha()).isNull();
		assertThat(usuario.getTokenSenhaExpiraEm()).isNull();
		verify(usuarioRepository).save(usuario);
	}

	@Test
	void recusaOLinkVencido() {
		var usuario = usuarioDaMaria();
		usuario.setTokenSenhaExpiraEm(Instant.now().minusSeconds(1));
		when(usuarioRepository.findByTokenSenha(usuario.getTokenSenha())).thenReturn(Optional.of(usuario));

		assertThatThrownBy(() -> acessoDoCliente.definirSenha(usuario.getTokenSenha(), "s3nha-forte"))
				.isInstanceOf(RegraDeNegocioException.class)
				.hasMessage("O link é inválido ou expirou. Peça um novo link.");
		assertThat(usuario.getSenha()).isNull();
	}

	@Test
	void recusaOLinkQueNaoExiste() {
		assertThatThrownBy(() -> acessoDoCliente.definirSenha(UUID.randomUUID(), "s3nha-forte"))
				.isInstanceOf(RegraDeNegocioException.class);
	}

	@Test
	void enviaUmNovoLinkPeloEmailOuPeloDocumento() {
		var usuario = usuarioDaMaria();
		var linkAntigo = usuario.getTokenSenha();
		when(clienteRepository.buscarPorEmail("maria@email.com")).thenReturn(Optional.of(maria));
		when(clienteRepository.buscarPorDocumento(new Documento("52998224725"))).thenReturn(Optional.of(maria));
		when(usuarioRepository.findByClienteId(maria.getId())).thenReturn(Optional.of(usuario));

		acessoDoCliente.enviarNovoLink(" Maria@Email.com ");
		assertThat(usuario.getTokenSenha()).isNotEqualTo(linkAntigo);

		var segundoLink = usuario.getTokenSenha();
		acessoDoCliente.enviarNovoLink("529.982.247-25");
		assertThat(usuario.getTokenSenha()).isNotEqualTo(segundoLink);
	}

	@Test
	void naoFazNadaParaCadastroInexistenteDocumentoInvalidoOuUsuarioInativo() {
		var inativo = usuarioDaMaria();
		inativo.setAtivo(false);
		when(clienteRepository.buscarPorEmail("maria@email.com")).thenReturn(Optional.of(maria));
		when(usuarioRepository.findByClienteId(maria.getId())).thenReturn(Optional.of(inativo));
		var oficinaAmiga = Cliente.cadastrar(new Documento("11.222.333/0001-81"), "Oficina Amiga", "contato@amiga.com",
				null);
		when(clienteRepository.buscarPorEmail("contato@amiga.com")).thenReturn(Optional.of(oficinaAmiga));

		acessoDoCliente.enviarNovoLink("ninguem@email.com");
		acessoDoCliente.enviarNovoLink("123");
		acessoDoCliente.enviarNovoLink("maria@email.com");
		acessoDoCliente.enviarNovoLink("contato@amiga.com");

		verify(usuarioRepository, never()).save(any());
		verify(eventos, never()).publishEvent(any());
	}

	@Test
	void removeOUsuarioJuntoComOCliente() {
		acessoDoCliente.remover(maria.getId());

		verify(usuarioRepository).deleteByClienteId(maria.getId());
	}

	private UsuarioEntity usuarioDaMaria() {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin("52998224725");
		usuario.setAtivo(true);
		usuario.setClienteId(maria.getId());
		usuario.setPapeis(new HashSet<>(Set.of(Papel.CLIENTE)));
		usuario.setTokenSenha(UUID.randomUUID());
		usuario.setTokenSenhaExpiraEm(Instant.now().plus(Duration.ofHours(24)));
		return usuario;
	}

	private UsuarioEntity salvo() {
		var salvo = ArgumentCaptor.forClass(UsuarioEntity.class);
		verify(usuarioRepository).save(salvo.capture());
		return salvo.getValue();
	}

}
