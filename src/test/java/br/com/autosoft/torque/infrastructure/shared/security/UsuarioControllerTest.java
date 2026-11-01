package br.com.autosoft.torque.infrastructure.shared.security;

import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.admin;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UsuarioControllerTest {

	private static final String JOAO = """
			{
			  "email": "joao@oficina.com",
			  "senha": "s3nha-do-joao",
			  "papeis": ["MECANICO", "ATENDENTE"]
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void oAdminCadastraOFuncionarioQueEntraComOEmail() throws Exception {
		mockMvc.perform(post("/usuarios").with(admin()).contentType(MediaType.APPLICATION_JSON).content(JOAO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("joao@oficina.com"))
				.andExpect(jsonPath("$.papeis[0]").value("ATENDENTE"))
				.andExpect(jsonPath("$.papeis[1]").value("MECANICO"))
				.andExpect(jsonPath("$.ativo").value(true))
				.andExpect(jsonPath("$.senha").doesNotExist());

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{ \"email\": \"joao@oficina.com\", \"senha\": \"s3nha-do-joao\" }"))
				.andExpect(status().isOk());
	}

	@Test
	void desativadoOFuncionarioNaoEntraMais() throws Exception {
		var id = cadastrarOJoao();

		mockMvc.perform(put("/usuarios/" + id).with(admin()).contentType(MediaType.APPLICATION_JSON)
						.content("{ \"papeis\": [\"MECANICO\"], \"ativo\": false }"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.ativo").value(false));

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{ \"email\": \"joao@oficina.com\", \"senha\": \"s3nha-do-joao\" }"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void listaEConsultaOsFuncionarios() throws Exception {
		var id = cadastrarOJoao();

		mockMvc.perform(get("/usuarios").with(admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[?(@.id == '" + id + "')].email").value("joao@oficina.com"));
		mockMvc.perform(get("/usuarios/" + id).with(admin()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("joao@oficina.com"));
	}

	@Test
	void recusaPapelDesconhecidoESenhaCurta() throws Exception {
		mockMvc.perform(post("/usuarios").with(admin()).contentType(MediaType.APPLICATION_JSON)
						.content(JOAO.replace("MECANICO", "FAXINEIRO")))
				.andExpect(status().isBadRequest());
		mockMvc.perform(post("/usuarios").with(admin()).contentType(MediaType.APPLICATION_JSON)
						.content(JOAO.replace("s3nha-do-joao", "curta")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void recusaOEmailRepetido() throws Exception {
		cadastrarOJoao();

		mockMvc.perform(post("/usuarios").with(admin()).contentType(MediaType.APPLICATION_JSON).content(JOAO))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Já existe usuário com este e-mail"));
	}

	private String cadastrarOJoao() throws Exception {
		var resposta = mockMvc.perform(post("/usuarios").with(admin()).contentType(MediaType.APPLICATION_JSON)
						.content(JOAO))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

}
