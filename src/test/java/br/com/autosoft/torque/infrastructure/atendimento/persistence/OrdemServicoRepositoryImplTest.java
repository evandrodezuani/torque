package br.com.autosoft.torque.infrastructure.atendimento.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
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
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;

@SpringBootTest
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'ABC1234', 'CARRO', 'Fiat', 'Uno', 2015)",
        "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'MOTO', 50.00, true)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 10, 0)"
})
class OrdemServicoRepositoryImplTest {

    private static final UUID CLIENTE_ID = UUID.fromString("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b");
    private static final UUID VEICULO_ID = UUID.fromString("7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e");
    private static final UUID CARRO_ID = UUID.fromString("1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d");
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
    void calculaOTempoMedioSoDasOsConcluidas() {
        repository.salvar(osExecutada("2026-10-12T10:00:00Z", "2026-10-12T13:00:00Z"));
        repository.salvar(osExecutada("2026-10-13T08:00:00Z", "2026-10-13T09:00:00Z"));
        var emExecucao = osAprovada("2026-10-14T08:00:00Z");
        repository.salvar(emExecucao);
        repository.salvar(OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-14T09:00:00Z")));
        recarregarDoBanco();

        var tempoMedio = repository.tempoMedioDeExecucao(null, null, null);

        assertThat(tempoMedio.ordensConsideradas()).isEqualTo(2);
        assertThat(tempoMedio.tempoMedio()).isEqualTo(Duration.ofHours(2));
    }

    @Test
    void filtraPelaDataDeConclusao() {
        repository.salvar(osExecutada("2026-09-30T10:00:00Z", "2026-09-30T14:00:00Z"));
        repository.salvar(osExecutada("2026-10-12T10:00:00Z", "2026-10-12T11:00:00Z"));
        repository.salvar(osExecutada("2026-11-01T10:00:00Z", "2026-11-01T18:00:00Z"));
        recarregarDoBanco();

        var outubro = repository.tempoMedioDeExecucao(null,
                Instant.parse("2026-10-01T00:00:00Z"), Instant.parse("2026-11-01T00:00:00Z"));

        assertThat(outubro.ordensConsideradas()).isEqualTo(1);
        assertThat(outubro.tempoMedio()).isEqualTo(Duration.ofHours(1));
    }

    @Test
    void filtraPeloTipoDoVeiculo() {
        repository.salvar(osExecutada(VEICULO_ID, "2026-10-12T10:00:00Z", "2026-10-12T12:00:00Z"));
        repository.salvar(osExecutada(CARRO_ID, "2026-10-12T10:00:00Z", "2026-10-12T18:00:00Z"));
        repository.salvar(osExecutada(CARRO_ID, "2026-10-13T10:00:00Z", "2026-10-13T14:00:00Z"));
        recarregarDoBanco();

        var motos = repository.tempoMedioDeExecucao(TipoVeiculo.MOTO, null, null);
        var carros = repository.tempoMedioDeExecucao(TipoVeiculo.CARRO, null, null);
        var caminhoes = repository.tempoMedioDeExecucao(TipoVeiculo.CAMINHAO, null, null);
        var todos = repository.tempoMedioDeExecucao(null, null, null);

        assertThat(motos.ordensConsideradas()).isEqualTo(1);
        assertThat(motos.tempoMedio()).isEqualTo(Duration.ofHours(2));
        assertThat(carros.ordensConsideradas()).isEqualTo(2);
        assertThat(carros.tempoMedio()).isEqualTo(Duration.ofHours(6));
        assertThat(caminhoes.ordensConsideradas()).isZero();
        assertThat(todos.ordensConsideradas()).isEqualTo(3);
    }

    @Test
    void semOsConcluidaAMediaENula() {
        repository.salvar(OrdemServico.abrir(CLIENTE_ID, VEICULO_ID, Instant.parse("2026-10-12T10:00:00Z")));
        recarregarDoBanco();

        var tempoMedio = repository.tempoMedioDeExecucao(null, null, null);

        assertThat(tempoMedio.ordensConsideradas()).isZero();
        assertThat(tempoMedio.tempoMedio()).isNull();
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

    private OrdemServico osAprovada(String aprovadaEm) {
        return osAprovada(VEICULO_ID, aprovadaEm);
    }

    private OrdemServico osAprovada(UUID veiculoId, String aprovadaEm) {
        var os = OrdemServico.abrir(CLIENTE_ID, veiculoId, Instant.parse(aprovadaEm).minusSeconds(3600));
        os.iniciarDiagnostico();
        os.incluirServico(TROCA_DE_OLEO);
        os.concluirDiagnostico(Instant.parse(aprovadaEm).minusSeconds(1800), Map.of());
        os.aprovarOrcamento(Instant.parse(aprovadaEm));
        return os;
    }

    private OrdemServico osExecutada(String aprovadaEm, String concluidaEm) {
        return osExecutada(VEICULO_ID, aprovadaEm, concluidaEm);
    }

    private OrdemServico osExecutada(UUID veiculoId, String aprovadaEm, String concluidaEm) {
        var os = osAprovada(veiculoId, aprovadaEm);
        os.concluirServico(Instant.parse(concluidaEm));
        return os;
    }

    private void recarregarDoBanco() {
        jpaRepository.flush();
        entityManager.clear();
    }

}
