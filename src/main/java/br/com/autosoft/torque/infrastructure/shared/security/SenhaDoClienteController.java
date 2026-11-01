package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

/**
 * Páginas do link do e-mail de acesso: o cliente cria a senha pelo navegador, sem usar a API.
 */
@Controller
@RequestMapping("/portal/senha")
@RequiredArgsConstructor
class SenhaDoClienteController {

	private static final int TAMANHO_MINIMO = 8;
	private static final int TAMANHO_MAXIMO = 72;

	private final AcessoDoClienteService acessoDoClienteService;

	@GetMapping
	public String formulario(@RequestParam UUID token, Model model) {
		model.addAttribute("token", token);
		return "portal/senha";
	}

	@PostMapping
	public String definir(@RequestParam UUID token, @RequestParam String senha, Model model) {
		if (senha.length() < TAMANHO_MINIMO || senha.length() > TAMANHO_MAXIMO) {
			model.addAttribute("token", token);
			model.addAttribute("erro", "A senha precisa ter de 8 a 72 caracteres");
			return "portal/senha";
		}
		acessoDoClienteService.definirSenha(token, senha);
		model.addAttribute("titulo", "Senha criada");
		model.addAttribute("texto", "Pronto! Agora você já pode entrar com o seu e-mail e esta senha.");
		return "portal/aviso";
	}

	@PostMapping("/novo-link")
	public String novoLink(@RequestParam String emailOuDocumento, Model model) {
		acessoDoClienteService.enviarNovoLink(emailOuDocumento);
		model.addAttribute("titulo", "Pedido recebido");
		model.addAttribute("texto", AuthController.LINK_ENVIADO + ".");
		return "portal/aviso";
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
	public String linkInvalido(RegraDeNegocioException e, Model model) {
		model.addAttribute("titulo", "Não foi possível criar a senha");
		model.addAttribute("texto", e.getMessage());
		model.addAttribute("pedirNovoLink", true);
		return "portal/aviso";
	}

}
