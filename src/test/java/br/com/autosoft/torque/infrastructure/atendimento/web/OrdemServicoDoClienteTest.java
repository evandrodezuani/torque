package br.com.autosoft.torque.infrastructure.atendimento.web;

import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.atendente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.cliente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.mecanico;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

/**
 * O cliente logado vê e responde só as próprias OS. A OS de outro cliente dá 404
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
		"insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
		"insert into cliente (id, documento, nome, email) values ('9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d', '11222333000181', 'Oficina Amiga', 'contato@amiga.com')",
		"insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
		"insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')",
		"insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'MOTO', 50.00, true)"
})
class OrdemServicoDoClienteTest {

	private static final String MARIA = "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b";
	private static final String OUTRO_CLIENTE = "9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void oClienteListaSoAsOsDele() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(get("/ordens-servico").with(cliente(MARIA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(1))
				.andExpect(jsonPath("$[0].id").value(id));
		mockMvc.perform(get("/ordens-servico").with(cliente(OUTRO_CLIENTE)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void aOsDeOutroClienteNaoEncontrada() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(get("/ordens-servico/" + id).with(cliente(MARIA))).andExpect(status().isOk());
		mockMvc.perform(get("/ordens-servico/" + id).with(cliente(OUTRO_CLIENTE)))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Ordem de serviço não encontrada"));
	}

	@Test
	void naoAprovaNemRejeitaOOrcamentoDeOutroCliente() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(cliente(OUTRO_CLIENTE)))
				.andExpect(status().isNotFound());
		mockMvc.perform(post("/ordens-servico/" + id + "/rejeitar-orcamento").with(cliente(OUTRO_CLIENTE)))
				.andExpect(status().isNotFound());

		mockMvc.perform(get("/ordens-servico/" + id).with(atendente()))
				.andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
	}

	@Test
	void oClienteAprovaOOrcamentoDele() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(cliente(MARIA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
	}

	@Test
	void oClienteRejeitaOOrcamentoDele() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(post("/ordens-servico/" + id + "/rejeitar-orcamento").with(cliente(MARIA)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("REJEITADA"));
	}

	@Test
	void oClienteNaoMexeNoTrabalhoDaOficinaNemNaOsDele() throws Exception {
		var id = abrirComOrcamentoEnviado();

		mockMvc.perform(post("/ordens-servico/" + id + "/revisar-orcamento").with(cliente(MARIA)))
				.andExpect(status().isForbidden());
		mockMvc.perform(post("/ordens-servico/" + id + "/entregar-veiculo").with(cliente(MARIA)))
				.andExpect(status().isForbidden());
	}

	private String abrirComOrcamentoEnviado() throws Exception {
		var resposta = mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
								  "veiculoId": "7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		String id = JsonPath.read(resposta, "$.id");
		mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico()))
				.andExpect(status().isOk());
		mockMvc.perform(post("/ordens-servico/" + id + "/servicos").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"servicoId\": \"6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e\", \"quantidade\": 1 }"))
				.andExpect(status().isOk());
		mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
				.andExpect(status().isOk());
		return id;
	}

}
