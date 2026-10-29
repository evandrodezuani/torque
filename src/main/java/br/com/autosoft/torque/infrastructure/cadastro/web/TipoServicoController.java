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

import br.com.autosoft.torque.application.cadastro.TipoServicoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/tipos-servico")
@RequiredArgsConstructor
class TipoServicoController {

	private final TipoServicoService tipoServicoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TipoServicoResponse cadastrar(@RequestBody @Valid TipoServicoRequest request) {
		return TipoServicoResponse.paraResposta(tipoServicoService.cadastrar(request.descricao()));
	}

	@GetMapping
	public List<TipoServicoResponse> listar() {
		return tipoServicoService.listar().stream().map(TipoServicoResponse::paraResposta).toList();
	}

	@GetMapping("/{id}")
	public TipoServicoResponse consultar(@PathVariable UUID id) {
		return TipoServicoResponse.paraResposta(tipoServicoService.consultar(id));
	}

	@PutMapping("/{id}")
	public TipoServicoResponse alterar(@PathVariable UUID id, @RequestBody @Valid TipoServicoRequest request) {
		return TipoServicoResponse.paraResposta(tipoServicoService.alterar(id, request.descricao()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		tipoServicoService.remover(id);
	}

}
