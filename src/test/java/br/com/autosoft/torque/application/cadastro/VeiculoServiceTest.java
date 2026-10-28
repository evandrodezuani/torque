package br.com.autosoft.torque.application.cadastro;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class VeiculoServiceTest {

    private final VeiculoRepository repository = mock(VeiculoRepository.class);
    private final ClienteRepository clienteRepository = mock(ClienteRepository.class);
    private final OrdemServicoRepository ordemServicoRepository = mock(OrdemServicoRepository.class);
    private final VeiculoService veiculoService = new VeiculoService(repository, clienteRepository, ordemServicoRepository);

    private final Cliente maria = Cliente.cadastrar(new Documento("529.982.247-25"), "Maria Souza", "maria@email.com", null);
    private final Veiculo moto = Veiculo.cadastrar(maria.getId(), new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

    @Test
    void cadastraOVeiculoDoCliente() {
        when(clienteRepository.buscarPorId(maria.getId())).thenReturn(Optional.of(maria));

        var veiculo = veiculoService.cadastrar(maria.getId(), "abc-1234", TipoVeiculo.CARRO, "Fiat", "Uno", 2010);

        assertThat(veiculo.getPlaca()).isEqualTo(new Placa("ABC1234"));
        verify(repository).salvar(veiculo);
    }

    @Test
    void naoCadastraParaClienteQueNaoExiste() {
        var id = UUID.randomUUID();
        when(clienteRepository.buscarPorId(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> veiculoService.cadastrar(id, "ABC1234", TipoVeiculo.CARRO, "Fiat", "Uno", 2010))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessage("Cliente não encontrado");
        verify(repository, never()).salvar(any());
    }

    @Test
    void recusaPlacaJaCadastrada() {
        when(clienteRepository.buscarPorId(maria.getId())).thenReturn(Optional.of(maria));
        when(repository.existePorPlaca(new Placa("ABC1234"))).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.cadastrar(maria.getId(), "ABC-1234", TipoVeiculo.CARRO, "Fiat", "Uno", 2010))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Já existe veículo com esta placa");
    }

    @Test
    void naoMostraVeiculoDeOutroCliente() {
        when(repository.buscarPorId(moto.getId())).thenReturn(Optional.of(moto));

        assertThatThrownBy(() -> veiculoService.consultar(UUID.randomUUID(), moto.getId()))
                .isInstanceOf(NaoEncontradoException.class)
                .hasMessage("Veículo não encontrado");
    }

    @Test
    void naoRemoveVeiculoComOrdemDeServicoEmAndamento() {
        when(repository.buscarPorId(moto.getId())).thenReturn(Optional.of(moto));
        when(ordemServicoRepository.existeEmAndamentoPorVeiculo(moto.getId())).thenReturn(true);

        assertThatThrownBy(() -> veiculoService.remover(maria.getId(), moto.getId()))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O veículo tem ordens de serviço em andamento e não pode ser removido");
        verify(repository, never()).remover(any());
    }

    @Test
    void removeOVeiculoComOHistoricoDeOrdensDeServico() {
        when(repository.buscarPorId(moto.getId())).thenReturn(Optional.of(moto));

        veiculoService.remover(maria.getId(), moto.getId());

        var ordem = inOrder(ordemServicoRepository, repository);
        ordem.verify(ordemServicoRepository).removerPorVeiculo(moto.getId());
        ordem.verify(repository).remover(moto.getId());
    }

}
