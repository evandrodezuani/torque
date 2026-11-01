package br.com.autosoft.torque.infrastructure.shared.security;

import static br.com.autosoft.torque.infrastructure.shared.security.Papel.ADMIN;
import static br.com.autosoft.torque.infrastructure.shared.security.Papel.ALMOXARIFE;
import static br.com.autosoft.torque.infrastructure.shared.security.Papel.ATENDENTE;
import static br.com.autosoft.torque.infrastructure.shared.security.Papel.CLIENTE;
import static br.com.autosoft.torque.infrastructure.shared.security.Papel.GERENTE;
import static br.com.autosoft.torque.infrastructure.shared.security.Papel.MECANICO;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.admin;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.com;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PermissoesTest {

	private static final String ID = "/00000000-0000-0000-0000-000000000000";
	private static final String OS = "/ordens-servico" + ID;

	@Autowired
	private MockMvc mockMvc;

	static Stream<Arguments> matriz() {
		return Stream.of(
				// clientes e veículos
				linha("GET", "/clientes", ATENDENTE, MECANICO),
				linha("GET", "/clientes" + ID, ATENDENTE, MECANICO),
				linha("POST", "/clientes", ATENDENTE),
				linha("PUT", "/clientes" + ID, ATENDENTE),
				linha("DELETE", "/clientes" + ID, ATENDENTE),
				linha("GET", "/clientes" + ID + "/veiculos", ATENDENTE, MECANICO),
				linha("GET", "/clientes" + ID + "/veiculos" + ID, ATENDENTE, MECANICO),
				linha("POST", "/clientes" + ID + "/veiculos", ATENDENTE),
				linha("PUT", "/clientes" + ID + "/veiculos" + ID, ATENDENTE),
				linha("DELETE", "/clientes" + ID + "/veiculos" + ID, ATENDENTE),

				// tipos de serviço e serviços
				linha("GET", "/tipos-servico", ATENDENTE, MECANICO),
				linha("GET", "/tipos-servico" + ID, ATENDENTE, MECANICO),
				linha("POST", "/tipos-servico"),
				linha("PUT", "/tipos-servico" + ID),
				linha("DELETE", "/tipos-servico" + ID),
				linha("GET", "/servicos", ATENDENTE, MECANICO),
				linha("GET", "/servicos" + ID, ATENDENTE, MECANICO),
				linha("POST", "/servicos"),
				linha("PUT", "/servicos" + ID),
				linha("DELETE", "/servicos" + ID),

				// estoque
				linha("GET", "/itens-estoque", ALMOXARIFE, ATENDENTE, MECANICO),
				linha("GET", "/itens-estoque" + ID, ALMOXARIFE, ATENDENTE, MECANICO),
				linha("POST", "/itens-estoque", ALMOXARIFE),
				linha("PUT", "/itens-estoque" + ID, ALMOXARIFE),
				linha("POST", "/itens-estoque" + ID + "/entradas", ALMOXARIFE),
				linha("DELETE", "/itens-estoque" + ID, ALMOXARIFE),

				// ordens de serviço
				linha("GET", "/ordens-servico/tempo-medio-execucao"),
				linha("GET", "/ordens-servico", ATENDENTE, MECANICO, CLIENTE),
				linha("GET", OS, ATENDENTE, MECANICO, CLIENTE),
				linha("POST", "/ordens-servico", ATENDENTE),
				linha("POST", OS + "/entregar-veiculo", ATENDENTE),
				linha("POST", OS + "/aprovar-orcamento", ATENDENTE, CLIENTE),
				linha("POST", OS + "/rejeitar-orcamento", ATENDENTE, CLIENTE),
				linha("POST", OS + "/iniciar-diagnostico", MECANICO),
				linha("POST", OS + "/servicos", MECANICO),
				linha("POST", OS + "/pecas", MECANICO),
				linha("DELETE", OS + "/servicos" + ID, MECANICO),
				linha("DELETE", OS + "/pecas" + ID, MECANICO),
				linha("POST", OS + "/concluir-diagnostico", MECANICO),
				linha("POST", OS + "/revisar-orcamento", MECANICO),
				linha("POST", OS + "/concluir-servico", MECANICO),

				// usuários da oficina: só o ADMIN, nem o GERENTE
				soDoAdmin("GET", "/usuarios"),
				soDoAdmin("GET", "/usuarios" + ID),
				soDoAdmin("POST", "/usuarios"),
				soDoAdmin("PUT", "/usuarios" + ID));
	}

	private static Arguments soDoAdmin(String metodo, String rota) {
		return Arguments.of(metodo, rota, Set.of(ADMIN));
	}

	// ADMIN e GERENTE entram em todas as linhas; cada linha só lista os outros papéis que podem.
	private static Arguments linha(String metodo, String rota, Papel... outrosPapeis) {
		var permitidos = Stream.concat(Stream.of(ADMIN, GERENTE), Arrays.stream(outrosPapeis)).toList();
		return Arguments.of(metodo, rota, Set.copyOf(permitidos));
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("matriz")
	void quemNaoTemOPapelRecebe403(String metodo, String rota, Set<Papel> permitidos) throws Exception {
		for (var papel : Papel.values()) {
			if (!permitidos.contains(papel)) {
				mockMvc.perform(request(HttpMethod.valueOf(metodo), rota).with(com(papel)))
						.andExpect(status().isForbidden())
						.andExpect(jsonPath("$.detail").value("Você não tem permissão para esta ação"));
			}
		}
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("matriz")
	void quemTemOPapelNaoEBarrado(String metodo, String rota, Set<Papel> permitidos) throws Exception {
		for (var papel : permitidos) {
			var resposta = mockMvc.perform(request(HttpMethod.valueOf(metodo), rota).with(com(papel)))
					.andReturn().getResponse();
			assertThat(resposta.getStatus()).as("%s com %s", rota, papel).isNotIn(401, 403);
		}
	}

	@ParameterizedTest(name = "{0} {1}")
	@MethodSource("matriz")
	void semTokenRecebe401(String metodo, String rota, Set<Papel> permitidos) throws Exception {
		mockMvc.perform(request(HttpMethod.valueOf(metodo), rota))
				.andExpect(status().isUnauthorized())
				.andExpect(header().string("WWW-Authenticate", "Bearer"))
				.andExpect(jsonPath("$.detail").value("É preciso entrar com um token válido"));
	}

	@Test
	void tokenDeClienteSemOCadastroNaoViraAcessoDeFuncionario() throws Exception {
		mockMvc.perform(get("/ordens-servico").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_CLIENTE"))))
				.andExpect(status().isForbidden());
	}

	@Test
	void asRotasDoClienteEDoSwaggerNaoPedemToken() throws Exception {
		mockMvc.perform(get("/acompanhamento" + ID)).andExpect(status().isNotFound());
		mockMvc.perform(get("/portal" + ID)).andExpect(status().isNotFound());
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.components.securitySchemes.token.scheme").value("bearer"));
		mockMvc.perform(post("/auth/clientes/novo-link").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"emailOuDocumento\": \"ninguem@email.com\" }")).andExpect(status().isAccepted());
		mockMvc.perform(post("/auth/clientes/definir-senha").contentType(MediaType.APPLICATION_JSON)
				.content("{ \"token\": \"" + ID.substring(1) + "\", \"senha\": \"s3nha-forte\" }"))
				.andExpect(status().isUnprocessableContent());
	}

	@Test
	void rotaForaDaMatrizFicaFechadaAteParaOAdmin() throws Exception {
		mockMvc.perform(get("/rota-que-nao-existe").with(admin()))
				.andExpect(status().isForbidden());
	}

}
