package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * Na subida, deixa um usuário de cada papel da oficina para a demonstração, com a senha de TORQUE_DEMO_SENHA.
 * A cada subida eles voltam ao padrão: ativos, com essa senha e só com o próprio papel.
 */
@Slf4j
@Component
class UsuariosDeDemonstracao implements ApplicationRunner {

	private final UsuarioJpaRepository repository;
	private final PasswordEncoder passwordEncoder;
	private final String senha;

	UsuariosDeDemonstracao(UsuarioJpaRepository repository, PasswordEncoder passwordEncoder,
			@Value("${torque.demo.senha:}") String senha) {
		this.repository = repository;
		this.passwordEncoder = passwordEncoder;
		this.senha = senha;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (senha.isBlank()) {
			log.warn("TORQUE_DEMO_SENHA não definida: os usuários de demonstração não foram criados");
			return;
		}
		deixarPronto("gerente@torque.local", Papel.GERENTE);
		deixarPronto("atendente@torque.local", Papel.ATENDENTE);
		deixarPronto("mecanico@torque.local", Papel.MECANICO);
		deixarPronto("almoxarife@torque.local", Papel.ALMOXARIFE);
		log.info("Usuários de demonstração prontos: gerente, atendente, mecanico e almoxarife @torque.local");
	}

	private void deixarPronto(String login, Papel papel) {
		var usuario = repository.findByLogin(login).orElseGet(() -> {
			var novo = new UsuarioEntity();
			novo.setId(UUID.randomUUID());
			novo.setLogin(login);
			return novo;
		});
		usuario.setSenha(passwordEncoder.encode(senha));
		usuario.setAtivo(true);
		usuario.getPapeis().clear();
		usuario.getPapeis().add(papel);
		repository.save(usuario);
	}

}
