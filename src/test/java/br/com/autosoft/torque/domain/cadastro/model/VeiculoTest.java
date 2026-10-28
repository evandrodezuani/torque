package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class VeiculoTest {

    private final UUID clienteId = UUID.randomUUID();
    private final Placa placa = new Placa("BRA2E19");

    @Test
    void cadastraVeiculoDoCliente() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

        assertThat(veiculo.getId()).isNotNull();
        assertThat(veiculo.getClienteId()).isEqualTo(clienteId);
        assertThat(veiculo.getPlaca()).isEqualTo(placa);
        assertThat(veiculo.getModelo()).isEqualTo("CG 160");
    }

    @Test
    void naoCadastraSemClienteTipoMarcaOuModelo() {
        assertThatThrownBy(() -> Veiculo.cadastrar(null, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, null, "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O tipo de veículo é obrigatório");
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, " ", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", null, 2022))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void recusaAnoForaDoIntervalo() {
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 1899))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Ano inválido");
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 3000))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Ano inválido");
    }

    @Test
    void alteraMarcaModeloEAno() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

        veiculo.alterar(new Placa("bra-2e19"), TipoVeiculo.MOTO, "Honda", "CG 160 Titan", 2023);

        assertThat(veiculo.getModelo()).isEqualTo("CG 160 Titan");
        assertThat(veiculo.getAno()).isEqualTo(2023);
    }

    @Test
    void naoAlteraAPlaca() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

        assertThatThrownBy(() -> veiculo.alterar(new Placa("ABC1234"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("A placa não pode ser alterada");
    }

    @Test
    void naoAlteraOTipo() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

        assertThatThrownBy(() -> veiculo.alterar(placa, TipoVeiculo.CARRO, "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O tipo de veículo não pode ser alterado");
    }

}
