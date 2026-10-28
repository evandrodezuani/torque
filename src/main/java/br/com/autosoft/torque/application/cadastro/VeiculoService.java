package br.com.autosoft.torque.application.cadastro;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VeiculoService {

    private final VeiculoRepository repository;
    private final ClienteRepository clienteRepository;
    private final OrdemServicoRepository ordemServicoRepository;

    @Transactional
    public Veiculo cadastrar(UUID clienteId, String placa, TipoVeiculo tipo, String marca, String modelo, int ano) {
        exigirCliente(clienteId);
        var placaDoVeiculo = new Placa(placa);
        if (repository.existePorPlaca(placaDoVeiculo)) {
            throw new RegraDeNegocioException("Já existe veículo com esta placa");
        }
        var veiculo = Veiculo.cadastrar(clienteId, placaDoVeiculo, tipo, marca, modelo, ano);
        repository.salvar(veiculo);
        return veiculo;
    }

    public Veiculo consultar(UUID clienteId, UUID veiculoId) {
        return repository.buscarPorId(veiculoId)
                .filter(veiculo -> veiculo.getClienteId().equals(clienteId))
                .orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado"));
    }

    public List<Veiculo> listar(UUID clienteId) {
        exigirCliente(clienteId);
        return repository.listarPorCliente(clienteId);
    }

    @Transactional
    public Veiculo alterar(UUID clienteId, UUID veiculoId, String placa, TipoVeiculo tipo, String marca, String modelo,
                           int ano) {
        var veiculo = consultar(clienteId, veiculoId);
        veiculo.alterar(new Placa(placa), tipo, marca, modelo, ano);
        repository.salvar(veiculo);
        return veiculo;
    }

    @Transactional
    public void remover(UUID clienteId, UUID veiculoId) {
        consultar(clienteId, veiculoId);
        if (ordemServicoRepository.existeEmAndamentoPorVeiculo(veiculoId)) {
            throw new RegraDeNegocioException("O veículo tem ordens de serviço em andamento e não pode ser removido");
        }
        ordemServicoRepository.removerPorVeiculo(veiculoId);
        repository.remover(veiculoId);
    }

    private void exigirCliente(UUID clienteId) {
        if (clienteRepository.buscarPorId(clienteId).isEmpty()) {
            throw new NaoEncontradoException("Cliente não encontrado");
        }
    }

}
