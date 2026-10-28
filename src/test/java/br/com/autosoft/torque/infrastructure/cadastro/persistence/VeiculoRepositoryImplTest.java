package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;

@SpringBootTest
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into cliente (id, documento, nome, email) values ('9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d', '11222333000181', 'Oficina Amiga', 'contato@amiga.com')"
})
class VeiculoRepositoryImplTest {

    private static final UUID MARIA = UUID.fromString("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b");
    private static final UUID OUTRO_CLIENTE = UUID.fromString("9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d");

    @Autowired
    private VeiculoRepository repository;

    @Test
    void salvaEBuscaOVeiculoDeVolta() {
        var moto = Veiculo.cadastrar(MARIA, new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022);

        repository.salvar(moto);
        var lido = repository.buscarPorId(moto.getId()).orElseThrow();

        assertThat(lido.getClienteId()).isEqualTo(MARIA);
        assertThat(lido.getPlaca()).isEqualTo(new Placa("BRA2E19"));
        assertThat(lido.getModelo()).isEqualTo("CG 160");
        assertThat(lido.getAno()).isEqualTo(2022);
    }

    @Test
    void listaSoOsVeiculosDoCliente() {
        repository.salvar(Veiculo.cadastrar(MARIA, new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022));
        repository.salvar(Veiculo.cadastrar(MARIA, new Placa("ABC-1234"), TipoVeiculo.CARRO, "Fiat", "Uno", 2010));
        repository.salvar(Veiculo.cadastrar(OUTRO_CLIENTE, new Placa("XYZ9K87"), TipoVeiculo.CARRO, "Ford", "Ranger", 2024));

        assertThat(repository.listarPorCliente(MARIA)).hasSize(2);
    }

    @Test
    void sabeSeAPlacaJaFoiCadastrada() {
        repository.salvar(Veiculo.cadastrar(MARIA, new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022));

        assertThat(repository.existePorPlaca(new Placa("bra2e19"))).isTrue();
        assertThat(repository.existePorPlaca(new Placa("ABC1234"))).isFalse();
    }

    @Test
    void removeTodosOsVeiculosDoCliente() {
        repository.salvar(Veiculo.cadastrar(MARIA, new Placa("BRA2E19"), TipoVeiculo.MOTO, "Honda", "CG 160", 2022));
        repository.salvar(Veiculo.cadastrar(MARIA, new Placa("ABC-1234"), TipoVeiculo.CARRO, "Fiat", "Uno", 2010));
        repository.salvar(Veiculo.cadastrar(OUTRO_CLIENTE, new Placa("XYZ9K87"), TipoVeiculo.CARRO, "Ford", "Ranger", 2024));

        repository.removerPorCliente(MARIA);

        assertThat(repository.listarPorCliente(MARIA)).isEmpty();
        assertThat(repository.listarPorCliente(OUTRO_CLIENTE)).hasSize(1);
    }

}
