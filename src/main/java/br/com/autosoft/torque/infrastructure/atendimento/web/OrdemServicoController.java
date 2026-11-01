package br.com.autosoft.torque.infrastructure.atendimento.web;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.autosoft.torque.application.atendimento.OrdemServicoService;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.infrastructure.shared.security.ClienteLogado;
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
	public List<OrdemServicoResponse> listar(@RequestParam(required = false) StatusOS status,
			@RequestParam(defaultValue = "DESC") Sort.Direction ordem, Authentication autenticacao) {
		var ordens = ClienteLogado.id(autenticacao)
				.map(clienteId -> ordemServicoService.listarDoCliente(clienteId, status, ordem.isAscending()))
				.orElseGet(() -> ordemServicoService.listar(status, ordem.isAscending()));
		return ordens.stream()
				.map(OrdemServicoResponse::paraResposta)
				.toList();
	}

	@GetMapping("/tempo-medio-execucao")
	public TempoMedioExecucaoResponse tempoMedioExecucao(@RequestParam(required = false) TipoVeiculo tipoVeiculo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
		return TempoMedioExecucaoResponse.paraResposta(ordemServicoService.tempoMedioDeExecucao(tipoVeiculo, de, ate));
	}

	@GetMapping("/{id}")
	public OrdemServicoResponse consultar(@PathVariable UUID id, Authentication autenticacao) {
		var ordemServico = ClienteLogado.id(autenticacao)
				.map(clienteId -> ordemServicoService.consultarDoCliente(clienteId, id))
				.orElseGet(() -> ordemServicoService.consultar(id));
		return OrdemServicoResponse.paraResposta(ordemServico);
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

	@PostMapping("/{id}/concluir-servico")
	public OrdemServicoResponse concluirServico(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.concluirServico(id));
	}

	@PostMapping("/{id}/entregar-veiculo")
	public OrdemServicoResponse entregarVeiculo(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.entregarVeiculo(id));
	}

	@PostMapping("/{id}/aprovar-orcamento")
	public OrdemServicoResponse aprovarOrcamento(@PathVariable UUID id, Authentication autenticacao) {
		var ordemServico = ClienteLogado.id(autenticacao)
				.map(clienteId -> ordemServicoService.aprovarOrcamentoDoCliente(clienteId, id))
				.orElseGet(() -> ordemServicoService.aprovarOrcamento(id));
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

	@PostMapping("/{id}/rejeitar-orcamento")
	public OrdemServicoResponse rejeitarOrcamento(@PathVariable UUID id, Authentication autenticacao) {
		var ordemServico = ClienteLogado.id(autenticacao)
				.map(clienteId -> ordemServicoService.rejeitarOrcamentoDoCliente(clienteId, id))
				.orElseGet(() -> ordemServicoService.rejeitarOrcamento(id));
		return OrdemServicoResponse.paraResposta(ordemServico);
	}

	@PostMapping("/{id}/revisar-orcamento")
	public OrdemServicoResponse revisarOrcamento(@PathVariable UUID id) {
		return OrdemServicoResponse.paraResposta(ordemServicoService.revisarOrcamento(id));
	}

}
