package br.com.autosoft.torque.infrastructure.shared.security;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;

/**
 * O funcionário entra pelo e-mail, é o login dele. O cliente entra pelo e-mail do cadastro:
 */
@Service
@RequiredArgsConstructor
class AutenticacaoService {

	private final UsuarioJpaRepository usuarioRepository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;
	private final TokenService tokenService;

	public TokenGerado entrar(String email, String senha) {
		var login = email.trim().toLowerCase();
		var usuario = usuarioRepository.findByLogin(login)
				.or(() -> clienteRepository.buscarPorEmail(login)
						.flatMap(cliente -> usuarioRepository.findByClienteId(cliente.getId())))
				.filter(UsuarioEntity::isAtivo)
				.filter(encontrado -> encontrado.getSenha() != null
						&& passwordEncoder.matches(senha, encontrado.getSenha()))
				.orElseThrow(() -> new BadCredentialsException("E-mail ou senha inválidos"));
		return tokenService.gerar(usuario);
	}

}
