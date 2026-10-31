package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.model.ItemPeca;
import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import jakarta.persistence.EntityManager;
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
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'MOTO', 50.00, true)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 10, 0)"
})
class OrdemServicoRepositoryImplTest {

    private static final UUID CLIENTE_ID = UUID.fromString("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b");
    private static final UUID VEICULO_ID = UUID.fromString("7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e");
    private static final UUID SERVICO_ID = UUID.fromString("6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e");
    private static final UUID ITEM_ESTOQUE_ID = UUID.fromString("8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f");

    private static final ItemServico TROCA_DE_OLEO = new ItemServico(SERVICO_ID, "Troca de óleo", Dinheiro.de("50.00"), 1);
    private static final ItemPeca OLEO = new ItemPeca(ITEM_ESTOQUE_ID, "Óleo 10W30 1 L", Dinheiro.de("38.00"), 2);

    @Autowired
    private OrdemServicoRepository repository;

    @Autowired
    private OrdemServicoJpaRepository jpaRepository;

    @Autowired
    private EntityManager entityManager;

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
    void buscaAOrdemDeServicoComOsItensDeVolta() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        os.incluirPeca(OLEO);

        repository.salvar(os);
        recarregarDoBanco();
        var lida = repository.buscarPorId(os.getId()).orElseThrow();

        assertThat(lida.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
        assertThat(lida.orcamentoAtual().getId()).isEqualTo(os.orcamentoAtual().getId());
        assertThat(lida.orcamentoAtual().getItensServico()).containsExactly(TROCA_DE_OLEO);
        assertThat(lida.orcamentoAtual().getItensPeca()).containsExactly(OLEO);
        assertThat(lida.orcamentoAtual().total()).isEqualTo(Dinheiro.de("126.00"));
    }

    @Test
    void salvaDeNovoDepoisDeRevisarOOrcamento() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        repository.salvar(os);
        recarregarDoBanco();

        var lida = repository.buscarPorId(os.getId()).orElseThrow();
        lida.concluirDiagnostico(Instant.parse("2026-10-12T11:00:00Z"), Map.of());
        lida.rejeitarOrcamento(Instant.parse("2026-10-12T12:00:00Z"));
        lida.revisarOrcamento();
        repository.salvar(lida);
        recarregarDoBanco();

        var revisada = repository.buscarPorId(os.getId()).orElseThrow();
        assertThat(revisada.getOrcamentos()).hasSize(2);
        assertThat(revisada.getOrcamentos().getFirst().getSituacao()).isEqualTo(SituacaoOrcamento.REJEITADO);
        assertThat(revisada.orcamentoAtual().getNumero()).isEqualTo(2);
        assertThat(revisada.orcamentoAtual().getItensServico()).containsExactly(TROCA_DE_OLEO);
    }

    @Test
    void buscaPeloCodigoDeAcompanhamento() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        repository.salvar(os);
        recarregarDoBanco();

        var lida = repository.buscarPorCodigoAcompanhamento(os.getCodigoAcompanhamento()).orElseThrow();

        assertThat(lida.getId()).isEqualTo(os.getId());
        assertThat(lida.getCodigoAcompanhamento()).isEqualTo(os.getCodigoAcompanhamento());
        assertThat(repository.buscarPorCodigoAcompanhamento(os.getId())).isEmpty();
    }

    @Test
    void listaFiltrandoPeloStatusEOrdenandoPelaAbertura() {
        var primeira = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        var segunda = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T11:00:00Z"));
        segunda.iniciarDiagnostico();
        var terceira = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T12:00:00Z"));
        repository.salvar(segunda);
        repository.salvar(terceira);
        repository.salvar(primeira);
        recarregarDoBanco();

        assertThat(repository.listar(null, true)).extracting(OrdemServico::getId)
                .containsExactly(primeira.getId(), segunda.getId(), terceira.getId());
        assertThat(repository.listar(null, false)).extracting(OrdemServico::getId)
                .containsExactly(terceira.getId(), segunda.getId(), primeira.getId());
        assertThat(repository.listar(StatusOS.RECEBIDA, true)).extracting(OrdemServico::getId)
                .containsExactly(primeira.getId(), terceira.getId());
    }

    @Test
    void sabeSeOServicoEOItemJaForamUsadosEmOrcamento() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        os.incluirPeca(OLEO);
        repository.salvar(os);
        recarregarDoBanco();

        assertThat(repository.existeComServico(SERVICO_ID)).isTrue();
        assertThat(repository.existeComItemEstoque(ITEM_ESTOQUE_ID)).isTrue();
        assertThat(repository.existeComServico(UUID.randomUUID())).isFalse();
        assertThat(repository.existeComItemEstoque(UUID.randomUUID())).isFalse();
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
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        repository.salvar(os);
        jpaRepository.flush();

        repository.removerPorCliente(CLIENTE_ID);
        jpaRepository.flush();

        assertThat(jpaRepository.count()).isZero();
    }

    @Test
    void osEntregueNaoContaComoEmAndamento() {
        var os = OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z"));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        os.concluirDiagnostico(Instant.parse("2026-10-12T11:00:00Z"), Map.of());
        os.aprovarOrcamento(Instant.parse("2026-10-12T12:00:00Z"));
        os.concluirServico(Instant.parse("2026-10-12T15:00:00Z"));
        os.entregarVeiculo(Instant.parse("2026-10-12T17:00:00Z"));
        repository.salvar(os);

        assertThat(repository.existeEmAndamentoPorCliente(CLIENTE_ID)).isFalse();
    }

    private void recarregarDoBanco() {
        jpaRepository.flush();
        entityManager.clear();
    }

}
