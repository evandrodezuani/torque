package br.com.autosoft.torque.apitest.seguranca.permissao;

import static org.hamcrest.Matchers.equalTo;

import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;

import br.com.autosoft.torque.apitest.ApiTest;

/**
 * A matriz de papéis na aplicação de pé, com o token de verdade: o ADMIN cadastra um mecânico, o mecânico entra
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PermissaoTest extends ApiTest {

	private static final String EMAIL_DO_MECANICO = "mecanico@apitest.local";
	private static final String SENHA_DO_MECANICO = "s3nha-do-mecanico";
	private static final String SEM_PERMISSAO = "Você não tem permissão para esta ação";

	private final PermissaoPopulate populate = new PermissaoPopulate();
	private final PermissaoAPI api = new PermissaoAPI();

	private UUID clienteId;
	private UUID veiculoId;
	private String tokenDoMecanico;

	// Um cliente com o veículo de verdade: o 403 da abertura da OS vem do papel, e não de um dado inválido.
	@BeforeAll
	void prepararOCadastro() {
		clienteId = populate.inserirCliente();
		veiculoId = populate.inserirVeiculo(clienteId);
	}

	@Test
	@Order(1)
	void oAdminCadastraOMecanico() {
		api.cadastrarUsuario(EMAIL_DO_MECANICO, SENHA_DO_MECANICO, "MECANICO")
				.statusCode(201)
				.body("email", equalTo(EMAIL_DO_MECANICO))
				.body("papeis[0]", equalTo("MECANICO"));
	}

	@Test
	@Order(2)
	void oMecanicoEntraComOEmailEASenha() {
		tokenDoMecanico = api.entrar(EMAIL_DO_MECANICO, SENHA_DO_MECANICO)
				.statusCode(200)
				.extract().path("token");
	}

	@Test
	@Order(3)
	void oMecanicoConsultaOsClientes() {
		api.listarClientes(tokenDoMecanico)
				.statusCode(200);
	}

	@Test
	@Order(4)
	void oMecanicoNaoAbreOrdemDeServico() {
		api.abrirOrdemServico(tokenDoMecanico, clienteId, veiculoId)
				.statusCode(403)
				.body("detail", equalTo(SEM_PERMISSAO));
	}

	@Test
	@Order(5)
	void oMecanicoNaoVeOTempoMedio() {
		api.tempoMedioDeExecucao(tokenDoMecanico)
				.statusCode(403)
				.body("detail", equalTo(SEM_PERMISSAO));
	}

	@Test
	@Order(6)
	void oMecanicoNaoGerenciaOsUsuarios() {
		api.listarUsuarios(tokenDoMecanico)
				.statusCode(403)
				.body("detail", equalTo(SEM_PERMISSAO));
	}

}
