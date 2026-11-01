package br.com.autosoft.torque.infrastructure.shared.security;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
class UsuarioController {

	private final UsuarioService usuarioService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UsuarioResponse cadastrar(@RequestBody @Valid CadastrarUsuarioRequest request) {
		return UsuarioResponse.paraResposta(usuarioService.cadastrar(request.email(), request.senha(), request.papeis()));
	}

	@GetMapping
	public List<UsuarioResponse> listar() {
		return usuarioService.listar().stream().map(UsuarioResponse::paraResposta).toList();
	}

	@GetMapping("/{id}")
	public UsuarioResponse consultar(@PathVariable UUID id) {
		return UsuarioResponse.paraResposta(usuarioService.consultar(id));
	}

	@PutMapping("/{id}")
	public UsuarioResponse alterar(@PathVariable UUID id, @RequestBody @Valid AlterarUsuarioRequest request) {
		var usuario = usuarioService.alterar(id, request.papeis(), request.ativo(), request.senha());
		return UsuarioResponse.paraResposta(usuario);
	}

}
