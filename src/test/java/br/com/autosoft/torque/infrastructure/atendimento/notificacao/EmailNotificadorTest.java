package br.com.autosoft.torque.infrastructure.atendimento.notificacao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.autosoft.torque.application.atendimento.Acompanhamento;
import br.com.autosoft.torque.application.atendimento.Notificador;
import br.com.autosoft.torque.domain.atendimento.model.ItemPeca;
import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;

@SpringBootTest
class EmailNotificadorTest {

	@MockitoBean
	private JavaMailSender mailSender;

	@Autowired
	private Notificador notificador;

	@Test
	void enviaOOrcamentoComOsBotoesEOLinkParaONavegador() throws Exception {
		when(mailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
		var maria = Cliente.cadastrar(new Documento("52998224725"), "Maria Souza", "maria@email.com", null);
		var moto = Veiculo.cadastrar(maria.getId(), new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022);
		var ordemServico = OrdemServico.abrir(maria.getId(), moto.getId(), Instant.now());
		ordemServico.iniciarDiagnostico();
		ordemServico.incluirServico(new ItemServico(UUID.randomUUID(), "Troca de óleo", Dinheiro.de("50.00"), 1));
		ordemServico.incluirPeca(new ItemPeca(UUID.randomUUID(), "Óleo 10W30 1 L", Dinheiro.de("38.00"), 2));
		ordemServico.concluirDiagnostico(Instant.now(), Map.of());

		notificador.orcamentoDisponivel(maria, new Acompanhamento(ordemServico, moto));

		var enviada = ArgumentCaptor.forClass(MimeMessage.class);
		verify(mailSender).send(enviada.capture());
		var mensagem = enviada.getValue();
		var urlDaOs = "http://localhost:8080/portal/" + ordemServico.getCodigoAcompanhamento();
		assertThat(mensagem.getAllRecipients()[0]).hasToString("maria@email.com");
		assertThat(mensagem.getSubject()).isEqualTo("Orçamento disponível para o seu CG 160");
		assertThat((String) mensagem.getContent()).contains(
				"Maria Souza", "BRA2E19", "Troca de óleo", "R$ 76,00", "R$ 126,00",
				"action=\"" + urlDaOs + "/aprovar\"",
				"action=\"" + urlDaOs + "/rejeitar\"",
				"href=\"" + urlDaOs + "\"");
	}

}
