package br.com.autosoft.torque.apitest;

import static io.restassured.RestAssured.given;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.config.HeaderConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;

/**
 * O banco é limpo no começo e no fim de cada classe: os testes da mesma classe podem usar os registros uns dos outros.
 */
public abstract class ApiTest {

	private static final Random SORTEIO = new Random();
	private static final Properties CONFIGURACAO = carregarConfiguracao();

	// A limpeza do começo tira o lixo de uma execução interrompida no meio.
	@BeforeAll
	static void limparEEntrarComoAdmin() {
		Populate.limpar();

		RestAssured.baseURI = "http://localhost:" + variavel("SERVER_PORT");
		RestAssured.requestSpecification = null;

		String token = given()
				.contentType(ContentType.JSON)
				.body("""
						{ "email": "%s", "senha": "%s" }
						""".formatted(variavel("TORQUE_ADMIN_EMAIL"), variavel("TORQUE_ADMIN_SENHA")))
		.when()
				.post("/auth/login")
		.then()
				.statusCode(200)
				.extract().path("token");

		RestAssured.requestSpecification = new RequestSpecBuilder()
				.addHeader("Authorization", "Bearer " + token)
				.build();
		// Um teste que entra como outro usuário manda o próprio Authorization, que substitui o do ADMIN.
		// Sem isso, o REST Assured somaria os dois cabeçalhos na mesma requisição.
		RestAssured.config = RestAssuredConfig.config()
				.headerConfig(HeaderConfig.headerConfig().overwriteHeadersWithName("Authorization"));
	}

	@AfterAll
	static void limparOQueAClasseGravou() {
		Populate.limpar();
	}

	// O e-mail do cliente é único: cada execução usa um novo, para os testes poderem rodar de novo no mesmo banco.
	public static String emailNovo() {
		return "cliente." + UUID.randomUUID() + "@email.com";
	}

	// A placa também é única. Formato Mercosul: três letras, um número, uma letra e dois números.
	public static String placaNova() {
		return "" + letra() + letra() + letra() + SORTEIO.nextInt(10) + letra() + SORTEIO.nextInt(10) + SORTEIO.nextInt(10);
	}

	private static char letra() {
		return (char) ('A' + SORTEIO.nextInt(26));
	}

	// A variável de ambiente ganha do arquivo: o Jenkins usa as variáveis, e a máquina local, o application.properties.
	public static String variavel(String nome) {
		var valor = System.getenv().getOrDefault(nome, CONFIGURACAO.getProperty(nome));
		if (valor == null || valor.isBlank()) {
			throw new IllegalStateException("Defina " + nome + " no apitest/src/test/resources/application.properties"
					+ " ou como variável de ambiente (o mesmo valor da aplicação testada)");
		}
		return valor;
	}

	private static Properties carregarConfiguracao() {
		var configuracao = new Properties();
		try (var arquivo = ApiTest.class.getResourceAsStream("/application.properties")) {
			if (arquivo != null) {
				configuracao.load(new InputStreamReader(arquivo, StandardCharsets.UTF_8));
			}
		} catch (IOException e) {
			throw new UncheckedIOException("Não foi possível ler o application.properties dos testes de API", e);
		}
		return configuracao;
	}

}
