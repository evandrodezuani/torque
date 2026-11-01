package br.com.autosoft.torque.apitest.atendimento.ordemservico;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import br.com.autosoft.torque.apitest.ApiTest;
import io.restassured.path.json.JsonPath;

/**
 * O caminho da OS, um passo por teste e na ordem: cada passo continua de onde o anterior parou.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class OrdemServicoTest extends ApiTest {

	private final OrdemServicoPopulate populate = new OrdemServicoPopulate();
	private final OrdemServicoAPI api = new OrdemServicoAPI();

	private UUID clienteId;
	private UUID veiculoId;
	private UUID servicoId;
	private UUID pecaId;
	private String osId;
	private String codigoAcompanhamento;
	private String osRejeitadaId;

	// Roda depois da limpeza do ApiTest: o cliente com a moto, a troca de óleo da moto e o óleo no estoque.
	@BeforeAll
	void prepararOCadastro() {
		clienteId = populate.inserirCliente();
		veiculoId = populate.inserirVeiculo(clienteId);
		servicoId = populate.inserirServico(populate.inserirTipoServico("Troca de óleo"), "MOTO", new BigDecimal("50.00"));
		pecaId = populate.inserirPeca("Óleo 10W30 1 L", new BigDecimal("38.00"), 10);
	}

	@Test
	@Order(1)
	void recusaAAberturaSemVeiculo() {
		api.abrirSemVeiculo(clienteId)
				.statusCode(400);
	}

	@Test
	@Order(2)
	void abreAOrdemDeServico() {
		var resposta = api.abrir(clienteId, veiculoId)
				.statusCode(201)
				.body("id", notNullValue())
				.body("status", equalTo("RECEBIDA"))
				.body("orcamento.situacao", equalTo("EM_ELABORACAO"));

		osId = resposta.extract().path("id");
		codigoAcompanhamento = resposta.extract().path("codigoAcompanhamento");
	}

	@Test
	@Order(3)
	void iniciaODiagnostico() {
		api.iniciarDiagnostico(osId)
				.statusCode(200)
				.body("status", equalTo("EM_DIAGNOSTICO"));
	}

	@Test
	@Order(4)
	void incluiOServicoEAPecaNoOrcamento() {
		api.incluirServico(osId, servicoId, 1).statusCode(200);

		api.incluirPeca(osId, pecaId, 2)
				.statusCode(200)
				.body("orcamento.servicos[0].descricao", equalTo("Troca de óleo"))
				.body("orcamento.pecas[0].quantidade", equalTo(2))
				.body("orcamento.total", equalTo(126.0f));
	}

	@Test
	@Order(5)
	void concluiODiagnosticoEReservaAPeca() {
		api.concluirDiagnostico(osId)
				.statusCode(200)
				.body("status", equalTo("AGUARDANDO_APROVACAO"))
				.body("orcamento.situacao", equalTo("PENDENTE"));

		api.consultarItemEstoque(pecaId)
				.body("saldo", equalTo(10))
				.body("reservado", equalTo(2));
	}

	@Test
	@Order(6)
	void oClienteAcompanhaSemLogin() throws Exception {
		var resposta = api.acompanharSemToken(codigoAcompanhamento);

		assertEquals(200, resposta.statusCode());
		var corpo = JsonPath.from(resposta.body());
		assertEquals("AGUARDANDO_APROVACAO", corpo.getString("status"));
		assertEquals(126.0f, corpo.getFloat("orcamento.total"));
	}

	@Test
	@Order(7)
	void aprovaOOrcamentoEBaixaOEstoque() {
		api.aprovarOrcamento(osId)
				.statusCode(200)
				.body("status", equalTo("EM_EXECUCAO"))
				.body("orcamento.situacao", equalTo("APROVADO"));

		api.consultarItemEstoque(pecaId)
				.body("saldo", equalTo(8))
				.body("reservado", equalTo(0));
	}

	@Test
	@Order(8)
	void concluiOServico() {
		api.concluirServico(osId)
				.statusCode(200)
				.body("status", equalTo("FINALIZADA"));
	}

	@Test
	@Order(9)
	void entregaOVeiculo() {
		api.entregarVeiculo(osId)
				.statusCode(200)
				.body("status", equalTo("ENTREGUE"));
	}

	@Test
	@Order(10)
	void oTempoMedioContaAOsConcluida() {
		api.tempoMedioDeExecucao("MOTO")
				.statusCode(200)
				.body("ordensConsideradas", equalTo(1));
	}

	@Test
	@Order(11)
	void rejeitarOOrcamentoLiberaAReserva() {
		osRejeitadaId = api.abrir(clienteId, veiculoId).statusCode(201).extract().path("id");
		api.iniciarDiagnostico(osRejeitadaId).statusCode(200);
		api.incluirPeca(osRejeitadaId, pecaId, 3).statusCode(200);
		api.concluirDiagnostico(osRejeitadaId).statusCode(200);
		api.consultarItemEstoque(pecaId).body("reservado", equalTo(3));

		api.rejeitarOrcamento(osRejeitadaId)
				.statusCode(200)
				.body("status", equalTo("REJEITADA"))
				.body("orcamento.situacao", equalTo("REJEITADO"));

		api.consultarItemEstoque(pecaId)
				.body("saldo", equalTo(8))
				.body("reservado", equalTo(0));
	}

	@Test
	@Order(12)
	void revisarCriaOOrcamentoSeguinte() {
		api.revisarOrcamento(osRejeitadaId)
				.statusCode(200)
				.body("status", equalTo("EM_DIAGNOSTICO"))
				.body("orcamento.numero", equalTo(2))
				.body("orcamento.situacao", equalTo("EM_ELABORACAO"))
				.body("orcamento.pecas[0].quantidade", equalTo(3));
	}

	@Test
	@Order(13)
	void acompanhamentoComCodigoQueNaoExisteDa404() throws Exception {
		assertEquals(404, api.acompanharSemToken(UUID.randomUUID().toString()).statusCode());
	}

}
