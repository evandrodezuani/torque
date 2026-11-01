package br.com.autosoft.torque.infrastructure.shared.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Esta classe entrega a exceção ao TratadorDeErros, para que saiam no mesmo ProblemDetail dos outros erros.
 */
@Component
class RespostaDeAcessoNegado implements AuthenticationEntryPoint, AccessDeniedHandler {

	private final HandlerExceptionResolver tratador;

	RespostaDeAcessoNegado(@Qualifier("handlerExceptionResolver") HandlerExceptionResolver tratador) {
		this.tratador = tratador;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) {
		response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
		tratador.resolveException(request, response, null, e);
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) {
		tratador.resolveException(request, response, null, e);
	}

}
