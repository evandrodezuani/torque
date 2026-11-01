package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
class UsuarioService {

	private final UsuarioJpaRepository repository;
	private final ClienteRepository clienteRepository;
	private final PasswordEncoder passwordEncoder;

	@Transactional
	public UsuarioEntity cadastrar(String email, String senha, Set<Papel> papeis) {
		exigirPapeisDaOficina(papeis);
		var login = email.trim().toLowerCase();
		if (repository.existsByLogin(login)) {
			throw new RegraDeNegocioException("Já existe usuário com este e-mail");
		}
		// O login procura primeiro o funcionário; com o mesmo e-mail, o cliente não conseguiria entrar.
		if (clienteRepository.existePorEmail(login)) {
			throw new RegraDeNegocioException("Este e-mail é de um cliente");
		}

		var usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin(login);
		usuario.setSenha(passwordEncoder.encode(senha));
		usuario.setAtivo(true);
		usuario.getPapeis().addAll(papeis);
		return repository.save(usuario);
	}

	public List<UsuarioEntity> listar() {
		return repository.findByClienteIdIsNull(Sort.by("login"));
	}

	public UsuarioEntity consultar(UUID id) {
		return repository.findById(id)
				.filter(usuario -> usuario.getClienteId() == null)
				.orElseThrow(() -> new NaoEncontradoException("Usuário não encontrado"));
	}

	// A senha é opcional: só muda quando é informada (o ADMIN cria uma nova para quem esqueceu).
	@Transactional
	public UsuarioEntity alterar(UUID id, Set<Papel> papeis, boolean ativo, String senha) {
		exigirPapeisDaOficina(papeis);
		var usuario = consultar(id);
		var eraAdminAtivo = usuario.isAtivo() && usuario.getPapeis().contains(Papel.ADMIN);
		var continuaAdminAtivo = ativo && papeis.contains(Papel.ADMIN);
		if (eraAdminAtivo && !continuaAdminAtivo && repository.countByPapeisContainingAndAtivoTrue(Papel.ADMIN) == 1) {
			throw new RegraDeNegocioException("É preciso manter pelo menos um ADMIN ativo");
		}

		usuario.getPapeis().clear();
		usuario.getPapeis().addAll(papeis);
		usuario.setAtivo(ativo);
		if (senha != null) {
			usuario.setSenha(passwordEncoder.encode(senha));
		}
		return repository.save(usuario);
	}

	private void exigirPapeisDaOficina(Set<Papel> papeis) {
		if (papeis.contains(Papel.CLIENTE)) {
			throw new RegraDeNegocioException("O papel CLIENTE é dado pelo cadastro do cliente");
		}
	}

}
