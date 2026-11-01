package br.com.autosoft.torque.infrastructure.shared.security;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioJpaRepository repository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void cadastraOAtendente() {
		var atendente = new UsuarioEntity();
		atendente.setId(UUID.randomUUID());
		atendente.setLogin("ana@oficina.com");
		atendente.setSenha(passwordEncoder.encode("s3nha-da-ana"));
		atendente.setAtivo(true);
		atendente.setPapeis(Set.of(Papel.ATENDENTE));
		repository.save(atendente);
	}

	@Test
	void entraERecebeOToken() throws Exception {
		mockMvc.perform(login("ana@oficina.com", "s3nha-da-ana"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEm").isNotEmpty());
	}

	@Test
	void respondeNaoAutorizadoComASenhaErrada() throws Exception {
		mockMvc.perform(login("ana@oficina.com", "senha-errada"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.detail").value("E-mail ou senha inválidos"));
	}

	@Test
	void exigeOEmailEASenha() throws Exception {
		mockMvc.perform(login("", ""))
				.andExpect(status().isBadRequest());
	}

	@Test
	void aceitaOTokenGeradoNasRequisicoes() throws Exception {
		var resposta = mockMvc.perform(login("ana@oficina.com", "s3nha-da-ana")).andReturn().getResponse();
		String token = JsonPath.read(resposta.getContentAsString(), "$.token");

		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
				.andExpect(status().isOk());
	}

	@Test
	void recusaUmTokenAdulterado() throws Exception {
		var resposta = mockMvc.perform(login("ana@oficina.com", "s3nha-da-ana")).andReturn().getResponse();
		String token = JsonPath.read(resposta.getContentAsString(), "$.token");

		mockMvc.perform(get("/clientes").header(HttpHeaders.AUTHORIZATION, "Bearer " + token + "x"))
				.andExpect(status().isUnauthorized());
	}

	private static RequestBuilder login(String email, String senha) {
		return post("/auth/login").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{ "email": "%s", "senha": "%s" }
						""".formatted(email, senha));
	}

}
