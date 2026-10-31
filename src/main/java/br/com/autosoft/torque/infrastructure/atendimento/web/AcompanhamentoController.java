package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.autosoft.torque.application.atendimento.OrdemServicoService;
import lombok.RequiredArgsConstructor;

/**
 * Acesso do cliente à própria OS pelo código de acompanhamento, sem login administrativo.
 */
@RestController
@RequestMapping("/acompanhamento/{codigo}")
@RequiredArgsConstructor
class AcompanhamentoController {

	private final OrdemServicoService ordemServicoService;

	@GetMapping
	public AcompanhamentoResponse consultar(@PathVariable UUID codigo) {
		return AcompanhamentoResponse.paraResposta(ordemServicoService.acompanhar(codigo));
	}

	@PostMapping("/aprovar")
	public AcompanhamentoResponse aprovar(@PathVariable UUID codigo) {
		return AcompanhamentoResponse.paraResposta(ordemServicoService.aprovarOrcamentoPeloCliente(codigo));
	}

	@PostMapping("/rejeitar")
	public AcompanhamentoResponse rejeitar(@PathVariable UUID codigo) {
		return AcompanhamentoResponse.paraResposta(ordemServicoService.rejeitarOrcamentoPeloCliente(codigo));
	}

}
