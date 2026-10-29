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

import br.com.autosoft.torque.application.cadastro.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes")
@RequiredArgsConstructor
class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClienteResponse cadastrar(@RequestBody @Valid ClienteRequest request) {
        var cliente = clienteService.cadastrar(request.documento(), request.nome(), request.email(), request.telefone());
        return ClienteResponse.paraResposta(cliente);
    }

    @GetMapping("/{id}")
    public ClienteResponse consultar(@PathVariable UUID id) {
        return ClienteResponse.paraResposta(clienteService.consultar(id));
    }

    @GetMapping
    public List<ClienteResponse> listar() {
        return clienteService.listar().stream().map(ClienteResponse::paraResposta).toList();
    }

    @PutMapping("/{id}")
    public ClienteResponse alterar(@PathVariable UUID id, @RequestBody @Valid ClienteRequest request) {
        var cliente = clienteService.alterar(id, request.documento(), request.nome(), request.email(), request.telefone());
        return ClienteResponse.paraResposta(cliente);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID id) {
        clienteService.remover(id);
    }

}
