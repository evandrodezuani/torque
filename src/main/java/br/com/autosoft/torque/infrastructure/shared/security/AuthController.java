package br.com.autosoft.torque.infrastructure.shared.security;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
class AuthController {

	static final String LINK_ENVIADO = "Se houver um cadastro com estes dados, enviaremos um link para o e-mail cadastrado";

	private final AutenticacaoService autenticacaoService;
	private final AcessoDoClienteService acessoDoClienteService;

	@PostMapping("/login")
	public TokenResponse entrar(@RequestBody @Valid LoginRequest request) {
		return TokenResponse.paraResposta(autenticacaoService.entrar(request.email(), request.senha()));
	}

	@PostMapping("/clientes/definir-senha")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void definirSenha(@RequestBody @Valid DefinirSenhaRequest request) {
		acessoDoClienteService.definirSenha(request.token(), request.senha());
	}

	@PostMapping("/clientes/novo-link")
	@ResponseStatus(HttpStatus.ACCEPTED)
	public MensagemResponse novoLink(@RequestBody @Valid NovoLinkRequest request) {
		acessoDoClienteService.enviarNovoLink(request.emailOuDocumento());
		return new MensagemResponse(LINK_ENVIADO);
	}

}
