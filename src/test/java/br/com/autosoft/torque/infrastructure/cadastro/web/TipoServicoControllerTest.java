package br.com.autosoft.torque.infrastructure.cadastro.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.gerente;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TipoServicoControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cadastraERenomeiaOTipoDeServico() throws Exception {
		String id = cadastrar("Troca de oleo");

		mockMvc.perform(put("/tipos-servico/" + id).with(gerente()).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "descricao": "Troca de óleo" }
								"""))
				.andExpect(status().isOk());

		mockMvc.perform(get("/tipos-servico/" + id).with(gerente()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.descricao").value("Troca de óleo"));
	}

	@Test
	void recusaDescricaoRepetida() throws Exception {
		cadastrar("Troca de óleo");

		mockMvc.perform(post("/tipos-servico").with(gerente()).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "descricao": "Troca de óleo" }
								"""))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Já existe tipo de serviço com esta descrição"));
	}

	@Test
	void removeOTipoDeServico() throws Exception {
		var id = cadastrar("Troca de óleo");

		mockMvc.perform(delete("/tipos-servico/" + id).with(gerente())).andExpect(status().isNoContent());
		mockMvc.perform(get("/tipos-servico/" + id).with(gerente()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Tipo de serviço não encontrado"));
	}

	private String cadastrar(String descricao) throws Exception {
		var resposta = mockMvc.perform(post("/tipos-servico").with(gerente()).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{ "descricao": "%s" }
								""".formatted(descricao)))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

}
