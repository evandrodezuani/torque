package br.com.autosoft.torque.infrastructure.estoque.web;

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

import br.com.autosoft.torque.application.estoque.ItemEstoqueService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/itens-estoque")
@RequiredArgsConstructor
class ItemEstoqueController {

	private final ItemEstoqueService itemEstoqueService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ItemEstoqueResponse cadastrar(@RequestBody @Valid ItemEstoqueRequest request) {
		var item = itemEstoqueService.cadastrar(request.tipo(), request.descricao(), request.preco());
		return ItemEstoqueResponse.paraResposta(item);
	}

	@GetMapping
	public List<ItemEstoqueResponse> listar() {
		return itemEstoqueService.listar().stream().map(ItemEstoqueResponse::paraResposta).toList();
	}

	@GetMapping("/{id}")
	public ItemEstoqueResponse consultar(@PathVariable UUID id) {
		return ItemEstoqueResponse.paraResposta(itemEstoqueService.consultar(id));
	}

	@PutMapping("/{id}")
	public ItemEstoqueResponse alterar(@PathVariable UUID id, @RequestBody @Valid ItemEstoqueRequest request) {
		var item = itemEstoqueService.alterar(id, request.tipo(), request.descricao(), request.preco());
		return ItemEstoqueResponse.paraResposta(item);
	}

	@PostMapping("/{id}/entradas")
	public ItemEstoqueResponse registrarEntrada(@PathVariable UUID id, @RequestBody @Valid EntradaEstoqueRequest request) {
		return ItemEstoqueResponse.paraResposta(itemEstoqueService.registrarEntrada(id, request.quantidade()));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void remover(@PathVariable UUID id) {
		itemEstoqueService.remover(id);
	}

}
