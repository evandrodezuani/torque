package br.com.autosoft.torque.infrastructure.atendimento.web;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.autosoft.torque.application.atendimento.AbrirOrdemServico;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
class OrdemServicoController {

	private final AbrirOrdemServico abrirOrdemServico;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrdemServicoResponse abrir(@RequestBody @Valid AbrirOrdemServicoRequest request) {
		var ordemServico = abrirOrdemServico.executar(request.clienteId(), request.veiculoId());
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

}
