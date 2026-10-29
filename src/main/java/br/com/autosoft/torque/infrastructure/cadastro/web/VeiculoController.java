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

import br.com.autosoft.torque.application.cadastro.VeiculoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/clientes/{clienteId}/veiculos")
@RequiredArgsConstructor
class VeiculoController {

    private final VeiculoService veiculoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VeiculoResponse cadastrar(@PathVariable UUID clienteId, @RequestBody @Valid VeiculoRequest request) {
        var veiculo = veiculoService.cadastrar(clienteId, request.placa(), request.tipo(), request.marca(),
                request.modelo(), request.ano());
        return VeiculoResponse.paraResposta(veiculo);
    }

    @GetMapping
    public List<VeiculoResponse> listar(@PathVariable UUID clienteId) {
        return veiculoService.listar(clienteId).stream().map(VeiculoResponse::paraResposta).toList();
    }

    @GetMapping("/{veiculoId}")
    public VeiculoResponse consultar(@PathVariable UUID clienteId, @PathVariable UUID veiculoId) {
        return VeiculoResponse.paraResposta(veiculoService.consultar(clienteId, veiculoId));
    }

    @PutMapping("/{veiculoId}")
    public VeiculoResponse alterar(@PathVariable UUID clienteId, @PathVariable UUID veiculoId,
                            @RequestBody @Valid VeiculoRequest request) {
        var veiculo = veiculoService.alterar(clienteId, veiculoId, request.placa(), request.tipo(), request.marca(),
                request.modelo(), request.ano());
        return VeiculoResponse.paraResposta(veiculo);
    }

    @DeleteMapping("/{veiculoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID clienteId, @PathVariable UUID veiculoId) {
        veiculoService.remover(clienteId, veiculoId);
    }

}
