package br.com.autosoft.torque.infrastructure.shared.security;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.application.cadastro.AcessoDoCliente;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

/**
 * O usuário do cliente nasce no cadastro, sem senha. O cliente cria a senha pelo link do e-mail,
 * que vale por 24 horas e uma única vez.
 */
@Service
@RequiredArgsConstructor
class AcessoDoClienteService implements AcessoDoCliente {

	private static final Duration VALIDADE_DO_LINK = Duration.ofHours(24);

	private final UsuarioJpaRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;
	private final ApplicationEventPublisher eventos;

	@Override
	public void liberar(Cliente cliente) {
		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin(cliente.getDocumento().numero());
		usuario.setAtivo(true);
		usuario.setClienteId(cliente.getId());
		usuario.getPapeis().add(Papel.CLIENTE);
		gerarLink(usuario);
	}

	@Override
	public void remover(UUID clienteId) {
		usuarioRepository.deleteByClienteId(clienteId);
	}

	@Transactional
	public void definirSenha(UUID token, String senha) {
		var usuario = usuarioRepository.findByTokenSenha(token)
				.filter(encontrado -> encontrado.getTokenSenhaExpiraEm().isAfter(Instant.now()))
				.orElseThrow(() -> new RegraDeNegocioException("O link é inválido ou expirou. Peça um novo link."));
		usuario.setSenha(passwordEncoder.encode(senha));
		usuario.setTokenSenha(null);
		usuario.setTokenSenhaExpiraEm(null);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public void enviarNovoLink(String emailOuDocumento) {
		buscarCliente(emailOuDocumento)
				.flatMap(cliente -> usuarioRepository.findByClienteId(cliente.getId()))
				.filter(UsuarioEntity::isAtivo)
				.ifPresent(this::gerarLink);
	}

	private Optional<Cliente> buscarCliente(String emailOuDocumento) {
		var valor = emailOuDocumento.trim();
		if (valor.contains("@")) {
			return clienteRepository.buscarPorEmail(valor.toLowerCase());
		}
		try {
			return clienteRepository.buscarPorDocumento(new Documento(valor));
		} catch (RegraDeNegocioException documentoInvalido) {
			return Optional.empty();
		}
	}

	private void gerarLink(UsuarioEntity usuario) {
		usuario.setTokenSenha(UUID.randomUUID());
		usuario.setTokenSenhaExpiraEm(Instant.now().plus(VALIDADE_DO_LINK));
		usuarioRepository.save(usuario);
		eventos.publishEvent(new LinkDeSenhaGerado(usuario.getClienteId(), usuario.getTokenSenha()));
	}

}
