package br.com.autosoft.torque.apitest.atendimento.ordemservico;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;

/**
 * As chamadas aos endpoints que a OS usa
 */
public class OrdemServicoAPI {

	public ValidatableResponse abrir(UUID clienteId, UUID veiculoId) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{
						  "clienteId": "%s",
						  "veiculoId": "%s"
						}
						""".formatted(clienteId, veiculoId))
		.when()
				.post("/ordens-servico")
		.then();
	}

	public ValidatableResponse abrirSemVeiculo(UUID clienteId) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{ "clienteId": "%s" }
						""".formatted(clienteId))
		.when()
				.post("/ordens-servico")
		.then();
	}

	public ValidatableResponse consultar(String id) {
		return given().when().get("/ordens-servico/" + id).then();
	}

	public ValidatableResponse iniciarDiagnostico(String id) {
		return acao(id, "iniciar-diagnostico");
	}

	public ValidatableResponse incluirServico(String id, UUID servicoId, int quantidade) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{ "servicoId": "%s", "quantidade": %d }
						""".formatted(servicoId, quantidade))
		.when()
				.post("/ordens-servico/" + id + "/servicos")
		.then();
	}

	public ValidatableResponse incluirPeca(String id, UUID itemEstoqueId, int quantidade) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{ "itemEstoqueId": "%s", "quantidade": %d }
						""".formatted(itemEstoqueId, quantidade))
		.when()
				.post("/ordens-servico/" + id + "/pecas")
		.then();
	}

	public ValidatableResponse concluirDiagnostico(String id) {
		return acao(id, "concluir-diagnostico");
	}

	public ValidatableResponse aprovarOrcamento(String id) {
		return acao(id, "aprovar-orcamento");
	}

	public ValidatableResponse rejeitarOrcamento(String id) {
		return acao(id, "rejeitar-orcamento");
	}

	public ValidatableResponse revisarOrcamento(String id) {
		return acao(id, "revisar-orcamento");
	}

	public ValidatableResponse concluirServico(String id) {
		return acao(id, "concluir-servico");
	}

	public ValidatableResponse entregarVeiculo(String id) {
		return acao(id, "entregar-veiculo");
	}

	public ValidatableResponse tempoMedioDeExecucao(String tipoVeiculo) {
		return given().queryParam("tipoVeiculo", tipoVeiculo).when().get("/ordens-servico/tempo-medio-execucao").then();
	}

	// O saldo e a reserva da peça, para conferir o que a OS fez no estoque.
	public ValidatableResponse consultarItemEstoque(UUID itemEstoqueId) {
		return given().when().get("/itens-estoque/" + itemEstoqueId).then();
	}

	// O acompanhamento é do cliente, sem login: vai sem o REST Assured, que já manda o token do ADMIN.
	public HttpResponse<String> acompanharSemToken(String codigo) throws IOException, InterruptedException {
		var pedido = HttpRequest.newBuilder(URI.create(RestAssured.baseURI + "/acompanhamento/" + codigo)).GET().build();
		return HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.ofString());
	}

	// As ações da OS são um POST sem corpo em /ordens-servico/{id}/{acao}.
	private ValidatableResponse acao(String id, String acao) {
		return given().when().post("/ordens-servico/" + id + "/" + acao).then();
	}

}
