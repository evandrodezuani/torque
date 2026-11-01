package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import lombok.extern.slf4j.Slf4j;

/**
 * Envia o link para o cliente criar a senha, se o cadastro for desfeito, nada é enviado.
 */
@Slf4j
@Component
class EmailDeAcesso {

	private final ClienteRepository clienteRepository;
	private final JavaMailSender mailSender;
	private final ITemplateEngine templateEngine;
	private final String remetente;
	private final String urlPortal;

	EmailDeAcesso(ClienteRepository clienteRepository, JavaMailSender mailSender, ITemplateEngine templateEngine,
			@Value("${torque.email.remetente}") String remetente, @Value("${torque.portal.url}") String urlPortal) {
		this.clienteRepository = clienteRepository;
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.remetente = remetente;
		this.urlPortal = urlPortal;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void linkGerado(LinkDeSenhaGerado evento) {
		try {
			var cliente = clienteRepository.buscarPorId(evento.clienteId()).orElseThrow();
			var contexto = new Context(Locale.of("pt", "BR"));
			contexto.setVariable("cliente", cliente);
			contexto.setVariable("urlDaSenha", urlPortal + "/senha?token=" + evento.token());

			var mensagem = mailSender.createMimeMessage();
			var helper = new MimeMessageHelper(mensagem, "UTF-8");
			helper.setFrom(remetente);
			helper.setTo(cliente.getEmail());
			helper.setSubject("Crie a sua senha de acesso à oficina");
			helper.setText(templateEngine.process("email/acesso-cliente", contexto), true);
			mailSender.send(mensagem);
		} catch (Exception e) {
			log.warn("Não foi possível enviar o link de acesso ao cliente {}", evento.clienteId(), e);
		}
	}

}
