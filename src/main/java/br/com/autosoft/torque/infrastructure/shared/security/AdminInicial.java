package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * se não existe nenhum ADMIN ativo, cria um a partir de TORQUE_ADMIN_EMAIL e TORQUE_ADMIN_SENHA.
 */
@Slf4j
@Component
class AdminInicial implements ApplicationRunner {

	private final UsuarioJpaRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final String email;
	private final String senha;

	AdminInicial(UsuarioJpaRepository repository, PasswordEncoder passwordEncoder,
			@Value("${torque.admin.email:}") String email, @Value("${torque.admin.senha:}") String senha) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.email = email;
		this.senha = senha;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (repository.existsByPapeisContainingAndAtivoTrue(Papel.ADMIN)) {
			return;
		}
		if (email.isBlank() || senha.isBlank()) {
			log.warn("Nenhum ADMIN ativo e TORQUE_ADMIN_EMAIL ou TORQUE_ADMIN_SENHA não definidos");
			return;
		}

		var login = email.trim().toLowerCase();
		var admin = repository.findByLogin(login).orElseGet(() -> {
			var novo = new UsuarioEntity();
			novo.setId(UUID.randomUUID());
			novo.setLogin(login);
			return novo;
		});
		admin.setSenha(passwordEncoder.encode(senha));
		admin.setAtivo(true);
		admin.getPapeis().add(Papel.ADMIN);
		repository.save(admin);
		log.info("Usuário ADMIN pronto para o acesso: {}", login);
	}

}
