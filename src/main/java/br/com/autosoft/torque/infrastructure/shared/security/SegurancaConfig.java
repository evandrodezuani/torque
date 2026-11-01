package br.com.autosoft.torque.infrastructure.shared.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
class SegurancaConfig {

	private static final String ADMIN = Papel.ADMIN.name();
	private static final String GERENTE = Papel.GERENTE.name();
	private static final String ATENDENTE = Papel.ATENDENTE.name();
	private static final String MECANICO = Papel.MECANICO.name();
	private static final String ALMOXARIFE = Papel.ALMOXARIFE.name();
	private static final String CLIENTE = Papel.CLIENTE.name();

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, RespostaDeAcessoNegado acessoNegado) throws Exception {
		http.csrf(csrf -> csrf.disable())
				.sessionManagement(sessao -> sessao.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(rotas -> rotas
						// sem login
						.requestMatchers(HttpMethod.POST, "/auth/login", "/auth/clientes/definir-senha", "/auth/clientes/novo-link")
						.permitAll()
						.requestMatchers("/acompanhamento/**", "/portal/**", "/css/**").permitAll()
						.requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/error").permitAll()

						// clientes e veículos: Atendente altera, Mecânico consulta
						.requestMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole(ADMIN, GERENTE, ATENDENTE, MECANICO)
						.requestMatchers("/clientes/**").hasAnyRole(ADMIN, GERENTE, ATENDENTE)

						// tipos de serviço e serviços: Gerente altera, Atendente e Mecânico consultam
						.requestMatchers(HttpMethod.GET, "/tipos-servico/**", "/servicos/**")
						.hasAnyRole(ADMIN, GERENTE, ATENDENTE, MECANICO)
						.requestMatchers("/tipos-servico/**", "/servicos/**").hasAnyRole(ADMIN, GERENTE)

						// estoque: Almoxarife altera, Atendente e Mecânico consultam
						.requestMatchers(HttpMethod.GET, "/itens-estoque/**")
						.hasAnyRole(ADMIN, GERENTE, ALMOXARIFE, ATENDENTE, MECANICO)
						.requestMatchers("/itens-estoque/**").hasAnyRole(ADMIN, GERENTE, ALMOXARIFE)

						// ordens de serviço; o cliente só vê e responde as dele (checado no OrdemServicoService)
						.requestMatchers(HttpMethod.GET, "/ordens-servico/tempo-medio-execucao").hasAnyRole(ADMIN, GERENTE)
						.requestMatchers(HttpMethod.GET, "/ordens-servico", "/ordens-servico/*")
						.hasAnyRole(ADMIN, GERENTE, ATENDENTE, MECANICO, CLIENTE)
						.requestMatchers(HttpMethod.POST, "/ordens-servico", "/ordens-servico/*/entregar-veiculo")
						.hasAnyRole(ADMIN, GERENTE, ATENDENTE)
						.requestMatchers(HttpMethod.POST, "/ordens-servico/*/aprovar-orcamento", "/ordens-servico/*/rejeitar-orcamento")
						.hasAnyRole(ADMIN, GERENTE, ATENDENTE, CLIENTE)
						.requestMatchers(HttpMethod.POST, "/ordens-servico/*/iniciar-diagnostico",
								"/ordens-servico/*/servicos", "/ordens-servico/*/pecas",
								"/ordens-servico/*/concluir-diagnostico", "/ordens-servico/*/revisar-orcamento",
								"/ordens-servico/*/concluir-servico")
						.hasAnyRole(ADMIN, GERENTE, MECANICO)
						.requestMatchers(HttpMethod.DELETE, "/ordens-servico/*/servicos/*", "/ordens-servico/*/pecas/*")
						.hasAnyRole(ADMIN, GERENTE, MECANICO)

						// usuários da oficina: só o ADMIN (o GERENTE não)
						.requestMatchers("/usuarios/**").hasRole(ADMIN)

						.anyRequest().denyAll())
				.oauth2ResourceServer(servidor -> servidor
						.jwt(Customizer.withDefaults())
						.authenticationEntryPoint(acessoNegado))
				.exceptionHandling(erros -> erros
						.authenticationEntryPoint(acessoNegado)
						.accessDeniedHandler(acessoNegado));
		return http.build();
	}

	@Bean
	SecretKey chaveDoToken(@Value("${torque.jwt.segredo}") String segredo) {
		var bytes = segredo.getBytes(StandardCharsets.UTF_8);
		if (bytes.length < 32) {
			throw new IllegalStateException("JWT_SECRET precisa ter pelo menos 32 caracteres");
		}
		return new SecretKeySpec(bytes, "HmacSHA256");
	}

	@Bean
	JwtEncoder jwtEncoder(SecretKey chaveDoToken) {
		return NimbusJwtEncoder.withSecretKey(chaveDoToken).algorithm(MacAlgorithm.HS256).build();
	}

	@Bean
	JwtDecoder jwtDecoder(SecretKey chaveDoToken) {
		return NimbusJwtDecoder.withSecretKey(chaveDoToken).macAlgorithm(MacAlgorithm.HS256).build();
	}

	// Lê o claim "papeis" do token e transforma cada papel em ROLE_<PAPEL>, o formato do hasRole do Spring.
	@Bean
	JwtAuthenticationConverter jwtAuthenticationConverter() {
		var papeis = new JwtGrantedAuthoritiesConverter();
		papeis.setAuthoritiesClaimName(TokenService.CLAIM_PAPEIS);
		papeis.setAuthorityPrefix("ROLE_");
		var conversor = new JwtAuthenticationConverter();
		conversor.setJwtGrantedAuthoritiesConverter(papeis);
		return conversor;
	}

}
