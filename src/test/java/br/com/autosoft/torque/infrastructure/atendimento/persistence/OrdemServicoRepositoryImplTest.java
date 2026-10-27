package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.UUID;

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
@Sql(statements = "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')")
class OrdemServicoRepositoryImplTest {

    private static final UUID CLIENTE_ID = UUID.fromString("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b");

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private OrdemServicoJpaRepository jpaRepository;

    @Test
    void salvaAOrdemDeServicoComOOrcamentoInicial() {
        var os = OrdemServico.abrir(CLIENTE_ID, UUID.randomUUID(), Instant.parse("2026-10-12T10:00:00Z"));

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
    void sabeSeOClienteTemOrdemDeServico() {
        repository.salvar(OrdemServico.abrir(CLIENTE_ID, UUID.randomUUID(), Instant.parse("2026-10-12T10:00:00Z")));

        assertThat(repository.existePorCliente(CLIENTE_ID)).isTrue();
        assertThat(repository.existePorCliente(UUID.randomUUID())).isFalse();
    }

}