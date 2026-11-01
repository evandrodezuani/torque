package br.com.autosoft.torque.infrastructure.estoque.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.almoxarife;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ItemEstoqueControllerTest {

	private static final String FILTRO_DE_OLEO = """
			{
			  "tipo": "PECA",
			  "descricao": "Filtro de óleo",
			  "preco": 35.00
			}
			""";

	@Autowired
	private MockMvc mockMvc;

	@Test
	void cadastraComSaldoZerado() throws Exception {
		mockMvc.perform(post("/itens-estoque").with(almoxarife()).contentType(MediaType.APPLICATION_JSON).content(FILTRO_DE_OLEO))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.tipo").value("PECA"))
				.andExpect(jsonPath("$.saldo").value(0))
				.andExpect(jsonPath("$.reservado").value(0));
	}

	@Test
	void recusaDescricaoRepetida() throws Exception {
		cadastrar(FILTRO_DE_OLEO);

		mockMvc.perform(post("/itens-estoque").with(almoxarife()).contentType(MediaType.APPLICATION_JSON).content(FILTRO_DE_OLEO))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("Já existe item de estoque com esta descrição"));
	}

	@Test
	void recusaTipoDesconhecido() throws Exception {
		mockMvc.perform(post("/itens-estoque").with(almoxarife()).contentType(MediaType.APPLICATION_JSON)
						.content(FILTRO_DE_OLEO.replace("PECA", "FERRAMENTA")))
				.andExpect(status().isBadRequest());
	}

	@Test
	void entradasSomamNoSaldo() throws Exception {
		var id = cadastrar(FILTRO_DE_OLEO);

		registrarEntrada(id, 10).andExpect(status().isOk());
		registrarEntrada(id, 5)
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.saldo").value(15));
	}

	@Test
	void recusaEntradaSemQuantidadePositiva() throws Exception {
		var id = cadastrar(FILTRO_DE_OLEO);

		registrarEntrada(id, 0)
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("A quantidade deve ser maior que zero"));
	}

	@Test
	void alteraOPreco() throws Exception {
		var id = cadastrar(FILTRO_DE_OLEO);

		mockMvc.perform(put("/itens-estoque/" + id).with(almoxarife()).contentType(MediaType.APPLICATION_JSON)
						.content(FILTRO_DE_OLEO.replace("35.00", "38.00")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.preco").value(38.00));
	}

	@Test
	void removeOItem() throws Exception {
		var id = cadastrar(FILTRO_DE_OLEO);

		mockMvc.perform(delete("/itens-estoque/" + id).with(almoxarife())).andExpect(status().isNoContent());
		mockMvc.perform(get("/itens-estoque/" + id).with(almoxarife()))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.detail").value("Item de estoque não encontrado"));
	}

	private String cadastrar(String json) throws Exception {
		var resposta = mockMvc.perform(post("/itens-estoque").with(almoxarife()).contentType(MediaType.APPLICATION_JSON).content(json))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return JsonPath.read(resposta, "$.id");
	}

	private ResultActions registrarEntrada(String id, int quantidade) throws Exception {
		return mockMvc.perform(post("/itens-estoque/" + id + "/entradas").with(almoxarife()).contentType(MediaType.APPLICATION_JSON)
				.content("{ \"quantidade\": " + quantidade + " }"));
	}

}
