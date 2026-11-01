package br.com.autosoft.torque.domain.cadastro.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

class ClienteTest {

    private final Documento cpf = new Documento("529.982.247-25");

    @Test
    void cadastraClienteComOsDadosInformados() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);

        assertThat(cliente.getId()).isNotNull();
        assertThat(cliente.getDocumento()).isEqualTo(cpf);
        assertThat(cliente.getNome()).isEqualTo("Maria Souza");
    }

    @Test
    void naoCadastraSemNomeOuEmail() {
        assertThatThrownBy(() -> Cliente.cadastrar(cpf, " ", "maria@email.com", null))
                .isInstanceOf(RegraDeNegocioException.class);
        assertThatThrownBy(() -> Cliente.cadastrar(cpf, "Maria Souza", null, null))
                .isInstanceOf(RegraDeNegocioException.class);
    }

    @Test
    void alteraOsDadosDeContatoSemTrocarODocumento() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);

        cliente.alterar(cpf, "Maria S. Lima", "maria.lima@email.com", "11999990000");

        assertThat(cliente.getNome()).isEqualTo("Maria S. Lima");
        assertThat(cliente.getTelefone()).isEqualTo("11999990000");
        assertThat(cliente.getDocumento()).isEqualTo(cpf);
    }

    @Test
    void naoAlteraODocumento() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);

        assertThatThrownBy(() -> cliente.alterar(new Documento("11.222.333/0001-81"), "Maria Souza", "maria@email.com", null))
                .isInstanceOf(RegraDeNegocioException.class)
                .hasMessage("O documento não pode ser alterado");
    }

    @Test
    void aceitaOMesmoDocumentoComOuSemMascara() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);

        cliente.alterar(new Documento("52998224725"), "Maria S. Lima", "maria@email.com", null);

        assertThat(cliente.getNome()).isEqualTo("Maria S. Lima");
    }

    @Test
    void guardaOEmailSemEspacosEEmMinusculas() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "  Maria.Souza@Email.COM ", null);

        assertThat(cliente.getEmail()).isEqualTo("maria.souza@email.com");
    }

}