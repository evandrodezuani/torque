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
        var veiculo = Veiculo.cadastrar(clienteId, placa, "Honda", "CG 160", 2022);

        assertThat(veiculo.getId()).isNotNull();
        assertThat(veiculo.getClienteId()).isEqualTo(clienteId);
        assertThat(veiculo.getPlaca()).isEqualTo(placa);
        assertThat(veiculo.getModelo()).isEqualTo("CG 160");
    }

    @Test
    void naoCadastraSemClienteMarcaOuModelo() {
        assertThatThrownBy(() -> Veiculo.cadastrar(null, placa, "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, " ", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, "Honda", null, 2022))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void recusaAnoForaDoIntervalo() {
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, "Honda", "CG 160", 1899))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Ano inválido");
        assertThatThrownBy(() -> Veiculo.cadastrar(clienteId, placa, "Honda", "CG 160", 3000))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("Ano inválido");
    }

    @Test
    void alteraMarcaModeloEAno() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, "Honda", "CG 160", 2022);

        veiculo.alterar(new Placa("bra-2e19"), "Honda", "CG 160 Titan", 2023);

        assertThat(veiculo.getModelo()).isEqualTo("CG 160 Titan");
        assertThat(veiculo.getAno()).isEqualTo(2023);
    }

    @Test
    void naoAlteraAPlaca() {
        var veiculo = Veiculo.cadastrar(clienteId, placa, "Honda", "CG 160", 2022);

        assertThatThrownBy(() -> veiculo.alterar(new Placa("ABC1234"), "Honda", "CG 160", 2022))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("A placa não pode ser alterada");
    }

}
