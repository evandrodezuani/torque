package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class PlacaTest {

    @Test
    void aceitaPlacaAntigaComOuSemHifen() {
        assertThat(new Placa("abc-1234").numero()).isEqualTo("ABC1234");
        assertThat(new Placa("ABC1234")).isEqualTo(new Placa("abc-1234"));
    }

    @Test
    void aceitaPlacaMercosul() {
        assertThat(new Placa("bra2e19").numero()).isEqualTo("BRA2E19");
    }

    @Test
    void recusaPlacaInvalida() {
        assertThatThrownBy(() -> new Placa("AB12345")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Placa("ABCD123")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Placa("ABC123")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Placa(" ")).isInstanceOf(RegraDeNegocioException.class);
    }

}
