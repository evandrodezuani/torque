package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@SpringBootTest
@Transactional
class EmailDeAcessoTest {

	@MockitoBean
	private JavaMailSender mailSender;

	@Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private EmailDeAcesso emailDeAcesso;

	@Test
	void enviaOLinkParaCriarASenha() throws Exception {
		when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
		var maria = Cliente.cadastrar(new Documento("52998224725"), "Maria Souza", "maria@email.com", null);
		clienteRepository.salvar(maria);
		var token = UUID.randomUUID();

		emailDeAcesso.linkGerado(new LinkDeSenhaGerado(maria.getId(), token));

		var enviada = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(enviada.capture());
		var mensagem = enviada.getValue();
		assertThat(mensagem.getAllRecipients()[0]).hasToString("maria@email.com");
		assertThat(mensagem.getSubject()).isEqualTo("Crie a sua senha de acesso à oficina");
		assertThat((String) mensagem.getContent()).contains("Maria Souza", "24 horas",
				"href=\"http://localhost:8080/portal/senha?token=" + token + "\"");
	}

}
