package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;

@SpringBootTest
@Transactional
class ClienteRepositoryImplTest {

    @Autowired
    private ClienteRepository repository;

    private final Documento cpf = new Documento("529.982.247-25");

    @Test
    void salvaEBuscaOClienteDeVolta() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", "11999990000");

        repository.salvar(cliente);
        var lido = repository.buscarPorId(cliente.getId()).orElseThrow();

        assertThat(lido.getId()).isEqualTo(cliente.getId());
        assertThat(lido.getDocumento()).isEqualTo(cpf);
        assertThat(lido.getNome()).isEqualTo("Maria Souza");
        assertThat(lido.getTelefone()).isEqualTo("11999990000");
    }

    @Test
    void salvarDeNovoAlteraOClienteExistente() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);
        repository.salvar(cliente);

        cliente.alterar(cpf, "Maria S. Lima", "maria@email.com", null);
        repository.salvar(cliente);

        assertThat(repository.listar()).hasSize(1);
        assertThat(repository.buscarPorId(cliente.getId()).orElseThrow().getNome()).isEqualTo("Maria S. Lima");
    }

    @Test
    void sabeSeODocumentoJaFoiCadastrado() {
        repository.salvar(Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null));

        assertThat(repository.existePorDocumento(new Documento("52998224725"))).isTrue();
        assertThat(repository.existePorDocumento(new Documento("11.222.333/0001-81"))).isFalse();
    }

    @Test
    void removeOCliente() {
        var cliente = Cliente.cadastrar(cpf, "Maria Souza", "maria@email.com", null);
        repository.salvar(cliente);

        repository.remover(cliente.getId());

        assertThat(repository.buscarPorId(cliente.getId())).isEmpty();
    }

}
