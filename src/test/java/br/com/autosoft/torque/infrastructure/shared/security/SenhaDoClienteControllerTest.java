package br.com.autosoft.torque.infrastructure.shared.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SenhaDoClienteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioJpaRepository repository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	private final UUID link = UUID.randomUUID();
	private UsuarioEntity usuario;

	@BeforeEach
	void criaOUsuarioComOLink() {
		usuario = new UsuarioEntity();
		usuario.setId(UUID.randomUUID());
		usuario.setLogin("52998224725");
		usuario.setAtivo(true);
		usuario.setPapeis(Set.of(Papel.CLIENTE));
		usuario.setTokenSenha(link);
		usuario.setTokenSenhaExpiraEm(Instant.now().plus(1, ChronoUnit.HOURS));
		repository.save(usuario);
	}

	@Test
	void mostraOFormularioDaSenha() throws Exception {
		mockMvc.perform(get("/portal/senha").param("token", link.toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"token\" value=\"" + link + "\"")));
	}

	@Test
	void criaASenhaPeloFormulario() throws Exception {
		mockMvc.perform(post("/portal/senha").param("token", link.toString()).param("senha", "s3nha-da-maria"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Senha criada")));

		assertThat(passwordEncoder.matches("s3nha-da-maria", repository.findById(usuario.getId()).orElseThrow().getSenha()))
				.isTrue();
	}

	@Test
	void avisaQuandoASenhaECurta() throws Exception {
		mockMvc.perform(post("/portal/senha").param("token", link.toString()).param("senha", "curta"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("A senha precisa ter de 8 a 72 caracteres")));
	}

	@Test
	void linkInvalidoOferecePedirUmNovo() throws Exception {
		mockMvc.perform(post("/portal/senha").param("token", UUID.randomUUID().toString()).param("senha", "s3nha-da-maria"))
				.andExpect(status().isUnprocessableContent())
				.andExpect(content().string(containsString("O link é inválido ou expirou")))
				.andExpect(content().string(containsString("action=\"/portal/senha/novo-link\"")));
	}

	@Test
	void pedeUmNovoLinkPelaPagina() throws Exception {
		mockMvc.perform(post("/portal/senha/novo-link").param("emailOuDocumento", "ninguem@email.com"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Se houver um cadastro com estes dados")));
	}

}
