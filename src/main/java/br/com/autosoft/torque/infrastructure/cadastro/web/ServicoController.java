package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.com.autosoft.torque.application.cadastro.ServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/servicos")
@RequiredArgsConstructor
class ServicoController {

	private final ServicoService servicoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ServicoResponse cadastrar(@RequestBody @Valid CadastrarServicoRequest request) {
		var servico = servicoService.cadastrar(request.tipoServicoId(), request.tipoVeiculo(), request.preco());
		return ServicoResponse.paraResposta(servico);
	}

	@GetMapping
	public List<ServicoResponse> listar() {
		return servicoService.listar().stream().map(ServicoResponse::paraResposta).toList();
	}

	@GetMapping("/{id}")
	public ServicoResponse consultar(@PathVariable UUID id) {
		return ServicoResponse.paraResposta(servicoService.consultar(id));
	}

	@PutMapping("/{id}")
	public ServicoResponse alterar(@PathVariable UUID id, @RequestBody @Valid AlterarServicoRequest request) {
		var servico = servicoService.alterar(id, request.tipoServicoId(), request.tipoVeiculo(), request.preco(),
				request.ativo());
		return ServicoResponse.paraResposta(servico);
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		servicoService.remover(id);
	}

}
