package br.com.autosoft.torque.infrastructure.cadastro.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')")
class ServicoControllerTest {

	private static final String TROCA_DE_OLEO_CARRO = """
			{
			  "tipoServicoId": "5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
			  "tipoVeiculo": "CARRO",
			  "preco": 80.00
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cadastraOMesmoServicoParaCarroEParaMoto() throws Exception {
		cadastrar(TROCA_DE_OLEO_CARRO);
		cadastrar(TROCA_DE_OLEO_CARRO.replace("CARRO", "MOTO").replace("80.00", "50.00"));

		mockMvc.perform(get("/servicos"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2));
	}

	@Test
	void recusaOMesmoServicoParaOMesmoTipoDeVeiculo() throws Exception {
		cadastrar(TROCA_DE_OLEO_CARRO);

		mockMvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON).content(TROCA_DE_OLEO_CARRO))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Já existe serviço deste tipo para este tipo de veículo"));
	}

	@Test
	void recusaTipoDeVeiculoDesconhecido() throws Exception {
		mockMvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON)
						.content(TROCA_DE_OLEO_CARRO.replace("CARRO", "BICICLETA")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void inativaOServico() throws Exception {
		String id = cadastrar(TROCA_DE_OLEO_CARRO);

		mockMvc.perform(put("/servicos/" + id).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "tipoServicoId": "5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
								  "tipoVeiculo": "CARRO",
								  "preco": 90.00,
								  "ativo": false
								}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ativo").value(false))
				.andExpect(jsonPath("$.preco").value(90.00));
	}

	@Test
	void naoRemoveTipoDeServicoQueTemServico() throws Exception {
		cadastrar(TROCA_DE_OLEO_CARRO);

		mockMvc.perform(delete("/tipos-servico/5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("O tipo de serviço tem serviços cadastrados e não pode ser removido"));
	}

	@Test
	void removeOServico() throws Exception {
		String id = cadastrar(TROCA_DE_OLEO_CARRO);

		mockMvc.perform(delete("/servicos/" + id)).andExpect(status().isNoContent());
		mockMvc.perform(get("/servicos/" + id))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Serviço não encontrado"));
	}

	private String cadastrar(String json) throws Exception {
		String resposta = mockMvc.perform(post("/servicos").contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

}
