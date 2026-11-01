package br.com.autosoft.torque.apitest.cadastro.cliente;

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
 * As chamadas aos endpoints de /clientes.
 */
public class ClienteAPI {

	public ValidatableResponse cadastrar(String documento, String nome, String email) {
		return given()
				.contentType(ContentType.JSON)
				.body(cliente(documento, nome, email))
		.when()
				.post("/clientes")
		.then();
	}

	public ValidatableResponse consultar(UUID id) {
		return given().when().get("/clientes/" + id).then();
	}

	public ValidatableResponse alterar(UUID id, String documento, String nome, String email) {
		return given()
				.contentType(ContentType.JSON)
				.body(cliente(documento, nome, email))
		.when()
				.put("/clientes/" + id)
		.then();
	}

	public ValidatableResponse remover(UUID id) {
		return given().when().delete("/clientes/" + id).then();
	}

	// Sem o REST Assured, que já manda o token do ADMIN em toda requisição. Devolve o status HTTP.
	public int listarSemToken() throws IOException, InterruptedException {
		var pedido = HttpRequest.newBuilder(URI.create(RestAssured.baseURI + "/clientes")).GET().build();
		return HttpClient.newHttpClient().send(pedido, HttpResponse.BodyHandlers.discarding()).statusCode();
	}

	private String cliente(String documento, String nome, String email) {
		return """
				{
				  "documento": "%s",
				  "nome": "%s",
				  "email": "%s"
				}
				""".formatted(documento, nome, email);
	}

}
