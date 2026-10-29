package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.SituacaoOrcamento;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;

@SpringBootTest
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)"
})
class OrdemServicoRepositoryImplTest {

    private static final UUID CLIENTE_ID = UUID.fromString("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b");
    private static final UUID VEICULO_ID = UUID.fromString("7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e");

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private OrdemServicoJpaRepository jpaRepository;

    @Test
    void salvaAOrdemDeServicoComOOrcamentoInicial() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));

        repository.salvar(os);
        jpaRepository.flush();

        var salva = jpaRepository.findById(os.getId()).orElseThrow();
        assertThat(salva.getStatus()).isEqualTo(StatusOS.RECEBIDA);
        assertThat(salva.getClienteId()).isEqualTo(os.getClienteId());
        assertThat(salva.getAbertaEm()).isEqualTo(os.getAbertaEm());
        assertThat(salva.getOrcamentos()).hasSize(1);
        assertThat(salva.getOrcamentos().getFirst().getNumero()).isEqualTo(1);
        assertThat(salva.getOrcamentos().getFirst().getSituacao()).isEqualTo(SituacaoOrcamento.EM_ELABORACAO);
    }

    @Test
    void osRecebidaContaComoEmAndamento() {
        repository.salvar(OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z")));

        assertThat(repository.existeEmAndamentoPorCliente(CLIENTE_ID)).isTrue();
        assertThat(repository.existeEmAndamentoPorVeiculo(VEICULO_ID)).isTrue();
        assertThat(repository.existeEmAndamentoPorCliente(UUID.randomUUID())).isFalse();
    }

    @Test
    void removeAsOrdensDeServicoDoClienteComOsOrcamentos() {
        repository.salvar(OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z")));
        jpaRepository.flush();

        repository.removerPorCliente(CLIENTE_ID);
        jpaRepository.flush();

        assertThat(jpaRepository.count()).isZero();
    }

    @Test
    void osEntregueNaoContaComoEmAndamento() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(new ItemServico(UUID.randomUUID(), "Troca de óleo", Dinheiro.de("80.00"), 1));
        os.concluirDiagnostico(Instant.parse("2026-10-12T11:00:00Z"));
        os.aprovarOrcamento(Instant.parse("2026-10-12T12:00:00Z"));
        os.concluirServico(Instant.parse("2026-10-12T15:00:00Z"));
        os.entregarVeiculo(Instant.parse("2026-10-12T17:00:00Z"));
        repository.salvar(os);

        assertThat(repository.existeEmAndamentoPorCliente(CLIENTE_ID)).isFalse();
    }

}