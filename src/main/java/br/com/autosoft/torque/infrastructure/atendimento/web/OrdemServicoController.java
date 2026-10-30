package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.autosoft.torque.application.atendimento.OrdemServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ordens-servico")
@RequiredArgsConstructor
class OrdemServicoController {

	private final OrdemServicoService ordemServicoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public OrdemServicoResponse abrir(@RequestBody @Valid AbrirOrdemServicoRequest request) {
		var ordemServico = ordemServicoService.abrir(request.clienteId(), request.veiculoId());
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

	@GetMapping
	public List<OrdemServicoResponse> listar() {
		return ordemServicoService.listar().stream().map(OrdemServicoResponse::paraResposta).toList();
	}

	@GetMapping("/{id}")
	public OrdemServicoResponse consultar(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.consultar(id));
	}

	@PostMapping("/{id}/iniciar-diagnostico")
	public OrdemServicoResponse iniciarDiagnostico(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.iniciarDiagnostico(id));
	}

	@PostMapping("/{id}/servicos")
	public OrdemServicoResponse incluirServico(@PathVariable UUID id, @RequestBody @Valid IncluirServicoRequest request) {
		var ordemServico = ordemServicoService.incluirServico(id, request.servicoId(), request.quantidade());
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

	@PostMapping("/{id}/pecas")
	public OrdemServicoResponse incluirPeca(@PathVariable UUID id, @RequestBody @Valid IncluirPecaRequest request) {
		var ordemServico = ordemServicoService.incluirPeca(id, request.itemEstoqueId(), request.quantidade());
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

	@DeleteMapping("/{id}/servicos/{servicoId}")
	public OrdemServicoResponse removerServico(@PathVariable UUID id, @PathVariable UUID servicoId) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.removerServico(id, servicoId));
	}

	@DeleteMapping("/{id}/pecas/{itemEstoqueId}")
	public OrdemServicoResponse removerPeca(@PathVariable UUID id, @PathVariable UUID itemEstoqueId) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.removerPeca(id, itemEstoqueId));
	}

	@PostMapping("/{id}/concluir-diagnostico")
	public OrdemServicoResponse concluirDiagnostico(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.concluirDiagnostico(id));
	}

	@PostMapping("/{id}/aprovar-orcamento")
	public OrdemServicoResponse aprovarOrcamento(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.aprovarOrcamento(id));
	}

	@PostMapping("/{id}/rejeitar-orcamento")
	public OrdemServicoResponse rejeitarOrcamento(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.rejeitarOrcamento(id));
	}

	@PostMapping("/{id}/revisar-orcamento")
	public OrdemServicoResponse revisarOrcamento(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.revisarOrcamento(id));
	}

}
