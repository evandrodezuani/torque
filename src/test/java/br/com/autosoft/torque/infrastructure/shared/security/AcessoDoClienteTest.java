package br.com.autosoft.torque.infrastructure.shared.security;

import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.atendente;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AcessoDoClienteTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioJpaRepository repository;

	@Test
	void oClienteCadastradoCriaASenhaPeloLinkEEntraPeloEmail() throws Exception {
		var clienteId = cadastrarAMaria();
		var usuario = repository.findByClienteId(clienteId).orElseThrow();
		assertThat(usuario.getSenha()).isNull();

		mockMvc.perform(login("maria@email.com", "s3nha-da-maria")).andExpect(status().isUnauthorized());

		mockMvc.perform(definirSenha(usuario.getTokenSenha(), "s3nha-da-maria")).andExpect(status().isNoContent());

		var resposta = mockMvc.perform(login("maria@email.com", "s3nha-da-maria"))
				.andExpect(status().isOk())
				.andReturn().getResponse().getContentAsString();
		String token = JsonPath.read(resposta, "$.token");
		mockMvc.perform(get("/ordens-servico").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	@Test
	void oLinkSoPodeSerUsadoUmaVez() throws Exception {
		var link = repository.findByClienteId(cadastrarAMaria()).orElseThrow().getTokenSenha();
		mockMvc.perform(definirSenha(link, "s3nha-da-maria")).andExpect(status().isNoContent());

		mockMvc.perform(definirSenha(link, "outra-s3nha"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(jsonPath("$.detail").value("O link é inválido ou expirou. Peça um novo link."));
	}

	@Test
	void recusaSenhaCurta() throws Exception {
		var link = repository.findByClienteId(cadastrarAMaria()).orElseThrow().getTokenSenha();

		mockMvc.perform(definirSenha(link, "curta")).andExpect(status().isBadRequest());
	}

	@Test
	void oNovoLinkTemAMesmaRespostaExistaOuNaoOCadastro() throws Exception {
		var clienteId = cadastrarAMaria();
		var linkAntigo = repository.findByClienteId(clienteId).orElseThrow().getTokenSenha();

		for (var pedido : new String[] { "maria@email.com", "ninguem@email.com" }) {
			mockMvc.perform(novoLink(pedido))
					.andExpect(status().isAccepted())
					.andExpect(jsonPath("$.mensagem")
							.value("Se houver um cadastro com estes dados, enviaremos um link para o e-mail cadastrado"));
		}
		assertThat(repository.findByClienteId(clienteId).orElseThrow().getTokenSenha()).isNotEqualTo(linkAntigo);
	}

	@Test
	void removerOClienteRemoveOUsuarioDele() throws Exception {
		var clienteId = cadastrarAMaria();

		mockMvc.perform(delete("/clientes/" + clienteId).with(atendente())).andExpect(status().isNoContent());

		assertThat(repository.findByClienteId(clienteId)).isEmpty();
	}

	private UUID cadastrarAMaria() throws Exception {
		var resposta = mockMvc.perform(post("/clientes").with(atendente()).contentType(MediaType.APPLICATION_JSON)
						.content("""
								{
								  "documento": "529.982.247-25",
								  "nome": "Maria Souza",
								  "email": "maria@email.com"
								}
								"""))
				.andExpect(status().isCreated())
				.andReturn().getResponse().getContentAsString();
		return UUID.fromString(JsonPath.read(resposta, "$.id"));
	}

	private static RequestBuilder login(String email, String senha) {
		return post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"email\": \"%s\", \"senha\": \"%s\" }".formatted(email, senha));
	}

	private static RequestBuilder definirSenha(UUID token, String senha) {
		return post("/auth/clientes/definir-senha").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"token\": \"%s\", \"senha\": \"%s\" }".formatted(token, senha));
	}

	private static RequestBuilder novoLink(String emailOuDocumento) {
		return post("/auth/clientes/novo-link").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"emailOuDocumento\": \"%s\" }".formatted(emailOuDocumento));
	}

}
