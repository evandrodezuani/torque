package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class DocumentoTest {

    @Test
    void aceitaCpfComOuSemMascaraEGuardaSoOsNumeros() {
        assertThat(new Documento("529.982.247-25").numero()).isEqualTo("52998224725");
        assertThat(new Documento("52998224725")).isEqualTo(new Documento("529.982.247-25"));
    }

    @Test
    void aceitaCnpjNumericoEAlfanumerico() {
        assertThat(new Documento("11.222.333/0001-81").numero()).isEqualTo("11222333000181");
        assertThat(new Documento("12.abc.345/01de-35").numero()).isEqualTo("12ABC34501DE35");
    }

    @Test
    void recusaDocumentoInvalido() {
        assertThatThrownBy(() -> new Documento("529.982.247-00")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Documento("111.111.111-11")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Documento("12345")).isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> new Documento(" ")).isInstanceOf(RegraDeNegocioException.class);
    }

}