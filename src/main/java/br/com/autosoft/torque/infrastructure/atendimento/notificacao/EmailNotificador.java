package br.com.autosoft.torque.infrastructure.atendimento.notificacao;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.ITemplateEngine;
import org.thymeleaf.context.Context;

import br.com.autosoft.torque.application.atendimento.Acompanhamento;
import br.com.autosoft.torque.application.atendimento.Notificador;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import jakarta.mail.MessagingException;

@Component
class EmailNotificador implements Notificador {

	private final JavaMailSender mailSender;
	private final ITemplateEngine templateEngine;
	private final String remetente;
	private final String urlPortal;

	EmailNotificador(JavaMailSender mailSender, ITemplateEngine templateEngine,
			@Value("${torque.email.remetente}") String remetente, @Value("${torque.portal.url}") String urlPortal) {
		this.mailSender = mailSender;
		this.templateEngine = templateEngine;
		this.remetente = remetente;
		this.urlPortal = urlPortal;
	}

	@Override
	public void orcamentoDisponivel(Cliente cliente, Acompanhamento acompanhamento) {
		var ordemServico = acompanhamento.ordemServico();
		var contexto = new Context(Locale.of("pt", "BR"));
		contexto.setVariable("cliente", cliente);
		contexto.setVariable("veiculo", acompanhamento.veiculo());
		contexto.setVariable("orcamento", ordemServico.orcamentoAtual());
		contexto.setVariable("urlDaOs", urlPortal + "/" + ordemServico.getCodigoAcompanhamento());

		try {
			var mensagem = mailSender.createMimeMessage();
			var helper = new MimeMessageHelper(mensagem, "UTF-8");
			helper.setFrom(remetente);
			helper.setTo(cliente.getEmail());
			helper.setSubject("Orçamento disponível para o seu " + acompanhamento.veiculo().getModelo());
			helper.setText(templateEngine.process("email/orcamento-disponivel", contexto), true);
			mailSender.send(mensagem);
		} catch (MessagingException e) {
			throw new IllegalStateException("Não foi possível montar o e-mail do orçamento", e);
		}
	}

}
