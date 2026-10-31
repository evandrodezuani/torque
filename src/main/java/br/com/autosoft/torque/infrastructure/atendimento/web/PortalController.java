package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import br.com.autosoft.torque.application.atendimento.OrdemServicoService;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

/**
 * Páginas do cliente, abertas pelo e-mail do orçamento
 */
@Controller
@RequestMapping("/portal/{codigo}")
@RequiredArgsConstructor
class PortalController {

	private final OrdemServicoService ordemServicoService;

	@GetMapping
	public String orcamento(@PathVariable UUID codigo, Model model) {
		model.addAttribute("acompanhamento", ordemServicoService.acompanhar(codigo));
		return "portal/orcamento";
	}

	@PostMapping("/aprovar")
	public String aprovar(@PathVariable UUID codigo, Model model) {
		model.addAttribute("acompanhamento", ordemServicoService.aprovarOrcamentoPeloCliente(codigo));
		model.addAttribute("titulo", "Orçamento aprovado");
		model.addAttribute("texto", "Obrigado! Já vamos começar o serviço do seu veículo.");
		return "portal/resultado";
	}

	@PostMapping("/rejeitar")
	public String rejeitar(@PathVariable UUID codigo, Model model) {
		model.addAttribute("acompanhamento", ordemServicoService.rejeitarOrcamentoPeloCliente(codigo));
		model.addAttribute("titulo", "Orçamento rejeitado");
		model.addAttribute("texto", "Recebemos a sua resposta. Vamos entrar em contato com você.");
		return "portal/resultado";
	}

	@ExceptionHandler(RegraDeNegocioException.class)
	@ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
	public String regraDeNegocio(RegraDeNegocioException e, Model model) {
		model.addAttribute("mensagem", e.getMessage());
		return "portal/erro";
	}

	@ExceptionHandler(NaoEncontradoException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String naoEncontrado(Model model) {
		model.addAttribute("mensagem", "Orçamento não encontrado. Confira o link recebido por e-mail.");
		return "portal/erro";
	}

}
