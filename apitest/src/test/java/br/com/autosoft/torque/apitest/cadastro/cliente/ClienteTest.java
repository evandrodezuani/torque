package br.com.autosoft.torque.apitest.cadastro.cliente;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.apitest.ApiTest;
import br.com.caelum.stella.validation.CPFValidator;

class ClienteTest extends ApiTest {

	private final ClientePopulate populate = new ClientePopulate();
	private final ClienteAPI api = new ClienteAPI();

	@Test
	void cadastraOCliente() {
		var cpf = new CPFValidator().generateRandomValid();

		api.cadastrar(cpf, "Maria Souza", emailNovo())
				.statusCode(201)
				.body("id", notNullValue())
				.body("documento", equalTo(cpf))
				.body("nome", equalTo("Maria Souza"));
	}

	@Test
	void consultaOCliente() {
		var id = populate.inserirCliente();

		api.consultar(id)
				.statusCode(200)
				.body("nome", equalTo("Maria Souza"));
	}

	@Test
	void alteraOCliente() {
		var cpf = new CPFValidator().generateRandomValid();
		var email = emailNovo();
		var id = populate.inserirCliente(cpf, email);

		api.alterar(id, cpf, "Maria S. Lima", email)
				.statusCode(200)
				.body("nome", equalTo("Maria S. Lima"));
	}

	@Test
	void removeOCliente() {
		var id = populate.inserirCliente();

		api.remover(id).statusCode(204);

		api.consultar(id).statusCode(404);
	}

	@Test
	void recusaCpfInvalido() {
		api.cadastrar("529.982.247-00", "Maria Souza", emailNovo())
				.statusCode(422)
				.body("detail", equalTo("CPF ou CNPJ inválido"));
	}

	@Test
	void semTokenNaoEntra() throws Exception {
		assertEquals(401, api.listarSemToken());
	}

}
