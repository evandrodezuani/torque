package br.com.autosoft.torque.apitest.seguranca.permissao;

import static io.restassured.RestAssured.given;

import java.util.UUID;

import io.restassured.http.ContentType;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;

/**
 * As chamadas dos testes de permissão. O cadastro do usuário vai com o token do ADMIN, que o ApiTest já coloca;
 */
public class PermissaoAPI {

	public ValidatableResponse cadastrarUsuario(String email, String senha, String papel) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{ "email": "%s", "senha": "%s", "papeis": ["%s"] }
						""".formatted(email, senha, papel))
		.when()
				.post("/usuarios")
		.then();
	}

	public ValidatableResponse entrar(String email, String senha) {
		return given()
				.contentType(ContentType.JSON)
				.body("""
						{ "email": "%s", "senha": "%s" }
						""".formatted(email, senha))
		.when()
				.post("/auth/login")
		.then();
	}

	public ValidatableResponse listarClientes(String token) {
		return como(token).when().get("/clientes").then();
	}

	public ValidatableResponse abrirOrdemServico(String token, UUID clienteId, UUID veiculoId) {
		return como(token)
				.contentType(ContentType.JSON)
				.body("""
						{ "clienteId": "%s", "veiculoId": "%s" }
						""".formatted(clienteId, veiculoId))
		.when()
				.post("/ordens-servico")
		.then();
	}

	public ValidatableResponse tempoMedioDeExecucao(String token) {
		return como(token).when().get("/ordens-servico/tempo-medio-execucao").then();
	}

	public ValidatableResponse listarUsuarios(String token) {
		return como(token).when().get("/usuarios").then();
	}

	// O token de quem está sendo testado substitui o do ADMIN (ver o HeaderConfig no ApiTest).
	private RequestSpecification como(String token) {
		return given().header("Authorization", "Bearer " + token);
	}

}
