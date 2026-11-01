package br.com.autosoft.torque.infrastructure.atendimento.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.almoxarife;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.atendente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.gerente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.mecanico;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into cliente (id, documento, nome, email) values ('9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d', '11222333000181', 'Oficina Amiga', 'contato@amiga.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')",
        "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c5e', 'Alinhamento')",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'MOTO', 50.00, true)",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d6f', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'CARRO', 80.00, true)",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d7a', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c5e', 'MOTO', 40.00, false)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 10, 0)"
})
class OrdemServicoControllerTest {

    private static final String TROCA_DE_OLEO_MOTO = "6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e";
    private static final String TROCA_DE_OLEO_CARRO = "6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d6f";
    private static final String ALINHAMENTO_MOTO_INATIVO = "6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d7a";
    private static final String OLEO_10W30 = "8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f";

    private static final String OS_DA_MOTO_DA_MARIA = """
            {
              "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
              "veiculoId": "7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void abreAOrdemDeServicoComOOrcamentoEmElaboracao() throws Exception {
        mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON).content(OS_DA_MOTO_DA_MARIA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.status").value("RECEBIDA"))
                .andExpect(jsonPath("$.orcamento.numero").value(1))
                .andExpect(jsonPath("$.orcamento.situacao").value("EM_ELABORACAO"))
                .andExpect(jsonPath("$.orcamento.total").value(0));
    }

    @Test
    void recusaPedidoSemVeiculo() throws Exception {
        mockMvc.perform(post("/ordens-servico").with(atendente())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
								{ "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b" }
								"""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void naoAbreComVeiculoDeOutroCliente() throws Exception {
        mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
                        .content(OS_DA_MOTO_DA_MARIA.replace("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
                                "9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O veículo não pertence ao cliente"));
    }

    @Test
    void naoAbreComVeiculoQueNaoExiste() throws Exception {
        mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
                        .content(OS_DA_MOTO_DA_MARIA.replace("7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e",
                                "00000000-0000-0000-0000-000000000000")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Veículo não encontrado"));
    }

    @Test
    void iniciaODiagnostico() throws Exception {
        var id = abrir();

        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void naoIniciaODiagnosticoDuasVezes() throws Exception {
        var id = abrir();
        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico())).andExpect(status().isOk());

        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível iniciar o diagnóstico de uma OS recebida"));
    }

    @Test
    void naoIniciaODiagnosticoDeOsQueNaoExiste() throws Exception {
        mockMvc.perform(post("/ordens-servico/00000000-0000-0000-0000-000000000000/iniciar-diagnostico").with(mecanico()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ordem de serviço não encontrada"));
    }

    @Test
    void incluiServicoEPecaEMostraOOrcamento() throws Exception {
        var id = abrirEmDiagnostico();

        incluirServico(id, TROCA_DE_OLEO_MOTO, 1).andExpect(status().isOk());
        incluirPeca(id, OLEO_10W30, 2)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.servicos[0].descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.orcamento.servicos[0].precoUnitario").value(50.00))
                .andExpect(jsonPath("$.orcamento.pecas[0].descricao").value("Óleo 10W30 1 L"))
                .andExpect(jsonPath("$.orcamento.pecas[0].subtotal").value(76.00))
                .andExpect(jsonPath("$.orcamento.total").value(126.00));
    }

    @Test
    void mesmaPecaIncluidaDeNovoSomaNaMesmaLinha() throws Exception {
        var id = abrirEmDiagnostico();
        incluirPeca(id, OLEO_10W30, 2);

        incluirPeca(id, OLEO_10W30, 1)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.pecas.length()").value(1))
                .andExpect(jsonPath("$.orcamento.pecas[0].quantidade").value(3));
    }

    @Test
    void naoIncluiServicoDeCarroNaMoto() throws Exception {
        var id = abrirEmDiagnostico();

        incluirServico(id, TROCA_DE_OLEO_CARRO, 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O serviço não é para este tipo de veículo"));
    }

    @Test
    void naoIncluiServicoInativo() throws Exception {
        var id = abrirEmDiagnostico();

        incluirServico(id, ALINHAMENTO_MOTO_INATIVO, 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O serviço está inativo"));
    }

    @Test
    void naoIncluiAntesDeIniciarODiagnostico() throws Exception {
        var id = abrir();

        incluirPeca(id, OLEO_10W30, 1)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível incluir peças e insumos numa OS em diagnóstico"));
    }

    @Test
    void recusaQuantidadeZero() throws Exception {
        var id = abrirEmDiagnostico();

        incluirServico(id, TROCA_DE_OLEO_MOTO, 0)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A quantidade deve ser maior que zero"));
    }

    @Test
    void consultaAOrdemDeServicoComOOrcamento() throws Exception {
        var id = abrirEmDiagnostico();
        incluirPeca(id, OLEO_10W30, 2);

        mockMvc.perform(get("/ordens-servico/" + id).with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.orcamento.pecas[0].quantidade").value(2))
                .andExpect(jsonPath("$.orcamento.total").value(76.00));
    }

    @Test
    void naoEncontraOrdemDeServicoQueNaoExiste() throws Exception {
        mockMvc.perform(get("/ordens-servico/00000000-0000-0000-0000-000000000000").with(atendente()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ordem de serviço não encontrada"));
    }

    @Test
    void listaAsOrdensDeServico() throws Exception {
        var recebida = abrir();
        var emDiagnostico = abrirEmDiagnostico();

        mockMvc.perform(get("/ordens-servico").with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.id == '" + recebida + "')].status").value("RECEBIDA"))
                .andExpect(jsonPath("$[?(@.id == '" + emDiagnostico + "')].status").value("EM_DIAGNOSTICO"));
    }

    @Test
    void removeAPecaEConcluiSoComOServico() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        incluirPeca(id, OLEO_10W30, 11);

        mockMvc.perform(delete("/ordens-servico/" + id + "/pecas/" + OLEO_10W30).with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.pecas.length()").value(0))
                .andExpect(jsonPath("$.orcamento.total").value(50.00));

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"));
    }

    @Test
    void removeOServico() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);

        mockMvc.perform(delete("/ordens-servico/" + id + "/servicos/" + TROCA_DE_OLEO_MOTO).with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.servicos.length()").value(0));
    }

    @Test
    void naoRemoveItemQueNaoEstaNoOrcamento() throws Exception {
        var id = abrirEmDiagnostico();

        mockMvc.perform(delete("/ordens-servico/" + id + "/pecas/" + OLEO_10W30).with(mecanico()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Peça ou insumo não encontrado no orçamento"));
    }

    @Test
    void concluiODiagnosticoEnviandoOOrcamentoEReservandoOEstoque() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        incluirPeca(id, OLEO_10W30, 2);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.orcamento.situacao").value("PENDENTE"));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(10))
                .andExpect(jsonPath("$.reservado").value(2));
    }

    @Test
    void naoConcluiODiagnosticoComOrcamentoVazio() throws Exception {
        var id = abrirEmDiagnostico();

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O orçamento precisa de pelo menos um serviço ou peça"));
    }

    @Test
    void naoConcluiODiagnosticoSemEstoque() throws Exception {
        var id = abrirEmDiagnostico();
        incluirPeca(id, OLEO_10W30, 11);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente para Óleo 10W30 1 L"));
    }

    @Test
    void enviaOOrcamentoComOPrecoAtualizadoDoServico() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        alterarTrocaDeOleoMoto("45.00", true);

        concluirDiagnostico(id)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orcamento.servicos[0].precoUnitario").value(45.00))
                .andExpect(jsonPath("$.orcamento.servicos[0].descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.orcamento.total").value(45.00));
    }

    @Test
    void naoEnviaOOrcamentoComServicoInativado() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        alterarTrocaDeOleoMoto("50.00", false);

        concluirDiagnostico(id)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O serviço Troca de óleo está inativo"));
    }

    @Test
    void aprovarIniciaAExecucaoEBaixaOEstoque() throws Exception {
        var id = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.orcamento.situacao").value("APROVADO"));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(8))
                .andExpect(jsonPath("$.reservado").value(0));
    }

    @Test
    void rejeitarLiberaAReserva() throws Exception {
        var id = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/ordens-servico/" + id + "/rejeitar-orcamento").with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.orcamento.situacao").value("REJEITADO"));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(10))
                .andExpect(jsonPath("$.reservado").value(0));
    }

    @Test
    void revisarCriaOOrcamentoSeguinteComAsLinhasDoAnterior() throws Exception {
        var id = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/ordens-servico/" + id + "/rejeitar-orcamento").with(atendente())).andExpect(status().isOk());

        mockMvc.perform(post("/ordens-servico/" + id + "/revisar-orcamento").with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_DIAGNOSTICO"))
                .andExpect(jsonPath("$.orcamento.numero").value(2))
                .andExpect(jsonPath("$.orcamento.situacao").value("EM_ELABORACAO"))
                .andExpect(jsonPath("$.orcamento.pecas[0].quantidade").value(2));
    }

    @Test
    void naoAprovaOrcamentoQueAindaNaoFoiEnviado() throws Exception {
        var id = abrirEmDiagnostico();

        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível aprovar o orçamento de uma OS aguardando aprovação"));
    }

    @Test
    void concluiOServicoEEntregaOVeiculo() throws Exception {
        var id = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente())).andExpect(status().isOk());

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-servico").with(mecanico()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FINALIZADA"));

        mockMvc.perform(post("/ordens-servico/" + id + "/entregar-veiculo").with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void devolveOVeiculoAntesDoOrcamento() throws Exception {
        var id = abrirEmDiagnostico();
        incluirPeca(id, OLEO_10W30, 2);

        mockMvc.perform(post("/ordens-servico/" + id + "/entregar-veiculo").with(atendente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENTREGUE"));
    }

    @Test
    void naoEntregaOVeiculoComOServicoEmExecucao() throws Exception {
        var id = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente())).andExpect(status().isOk());

        mockMvc.perform(post("/ordens-servico/" + id + "/entregar-veiculo").with(atendente()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail")
                        .value("Só é possível entregar o veículo de uma OS recebida, em diagnóstico, rejeitada ou finalizada"));
    }

    @Test
    void naoConcluiOServicoAntesDaAprovacao() throws Exception {
        var id = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-servico").with(mecanico()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível concluir o serviço de uma OS em execução"));
    }

    @Test
    void filtraAListaPeloStatus() throws Exception {
        var recebida = abrir();
        abrirEmDiagnostico();

        mockMvc.perform(get("/ordens-servico").with(atendente()).param("status", "RECEBIDA").param("ordem", "ASC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(recebida));
    }

    @Test
    void recusaStatusOuOrdemDesconhecidos() throws Exception {
        mockMvc.perform(get("/ordens-servico").with(atendente()).param("status", "PARADA")).andExpect(status().isBadRequest());
        mockMvc.perform(get("/ordens-servico").with(atendente()).param("ordem", "aleatoria")).andExpect(status().isBadRequest());
    }

    @Test
    void naoRemoveServicoNemItemJaUsadosEmOrcamento() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        incluirPeca(id, OLEO_10W30, 1);

        mockMvc.perform(delete("/servicos/" + TROCA_DE_OLEO_MOTO).with(gerente()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O serviço já foi usado em orçamentos e não pode ser removido"));
        mockMvc.perform(delete("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O item já foi usado em orçamentos e não pode ser removido"));
    }

    @Test
    void tempoMedioSemOsConcluidaDevolveZeroENulos() throws Exception {
        abrirComOrcamentoEnviado();

        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordensConsideradas").value(0))
                .andExpect(jsonPath("$.tempoMedioEmMinutos").isEmpty())
                .andExpect(jsonPath("$.tempoMedioEmDias").isEmpty())
                .andExpect(jsonPath("$.tempoMedio").isEmpty());
    }

    @Test
    void tempoMedioConsideraAOsComOServicoConcluido() throws Exception {
        var id = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente())).andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-servico").with(mecanico())).andExpect(status().isOk());

        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ordensConsideradas").value(1))
                .andExpect(jsonPath("$.tempoMedioEmMinutos").value(0))
                .andExpect(jsonPath("$.tempoMedioEmDias").value(0))
                .andExpect(jsonPath("$.tempoMedio").value("0min"));
    }

    @Test
    void tempoMedioFiltraPeloTipoDoVeiculo() throws Exception {
        var id = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/ordens-servico/" + id + "/aprovar-orcamento").with(atendente())).andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-servico").with(mecanico())).andExpect(status().isOk());

        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()).param("tipoVeiculo", "MOTO"))
                .andExpect(jsonPath("$.ordensConsideradas").value(1));
        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()).param("tipoVeiculo", "CARRO"))
                .andExpect(jsonPath("$.ordensConsideradas").value(0));
        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()).param("tipoVeiculo", "BICICLETA"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void tempoMedioRecusaPeriodoInvertidoEDataInvalida() throws Exception {
        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()).param("de", "2026-10-31").param("ate", "2026-10-01"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A data inicial não pode ser depois da data final"));
        mockMvc.perform(get("/ordens-servico/tempo-medio-execucao").with(gerente()).param("de", "31/10/2026"))
                .andExpect(status().isBadRequest());
    }

    private String abrirComOrcamentoEnviado() throws Exception {
        var id = abrirEmDiagnostico();
        incluirServico(id, TROCA_DE_OLEO_MOTO, 1);
        incluirPeca(id, OLEO_10W30, 2);
        concluirDiagnostico(id).andExpect(status().isOk());
        return id;
    }

    private ResultActions concluirDiagnostico(String id) throws Exception {
        return mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()));
    }

    private void alterarTrocaDeOleoMoto(String preco, boolean ativo) throws Exception {
        mockMvc.perform(put("/servicos/" + TROCA_DE_OLEO_MOTO).with(gerente()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoServicoId": "5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d",
                                  "tipoVeiculo": "MOTO",
                                  "preco": %s,
                                  "ativo": %s
                                }
                                """.formatted(preco, ativo)))
                .andExpect(status().isOk());
    }

    private ResultActions incluirServico(String id, String servicoId, int quantidade) throws Exception {
        return mockMvc.perform(post("/ordens-servico/" + id + "/servicos").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
                .content("{ \"servicoId\": \"" + servicoId + "\", \"quantidade\": " + quantidade + " }"));
    }

    private ResultActions incluirPeca(String id, String itemEstoqueId, int quantidade) throws Exception {
        return mockMvc.perform(post("/ordens-servico/" + id + "/pecas").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
                .content("{ \"itemEstoqueId\": \"" + itemEstoqueId + "\", \"quantidade\": " + quantidade + " }"));
    }

    private String abrirEmDiagnostico() throws Exception {
        var id = abrir();
        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico())).andExpect(status().isOk());
        return id;
    }

    private String abrir() throws Exception {
        var resposta = mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
                        .content(OS_DA_MOTO_DA_MARIA))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

}
