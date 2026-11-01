package br.com.autosoft.torque.infrastructure.atendimento.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.almoxarife;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.atendente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.mecanico;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')",
        "insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values ('6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e', '5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'MOTO', 50.00, true)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 10, 0)"
})
class AcompanhamentoControllerTest {

    private static final String OLEO_10W30 = "8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void clienteVeOAndamentoSemOsIdsInternos() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(get("/acompanhamento/" + codigo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AGUARDANDO_APROVACAO"))
                .andExpect(jsonPath("$.veiculo.placa").value("BRA2E19"))
                .andExpect(jsonPath("$.veiculo.modelo").value("CG 160"))
                .andExpect(jsonPath("$.orcamento.situacao").value("PENDENTE"))
                .andExpect(jsonPath("$.orcamento.servicos[0].descricao").value("Troca de óleo"))
                .andExpect(jsonPath("$.orcamento.pecas[0].subtotal").value(76.00))
                .andExpect(jsonPath("$.orcamento.total").value(126.00))
                .andExpect(jsonPath("$.id").doesNotExist())
                .andExpect(jsonPath("$.clienteId").doesNotExist())
                .andExpect(jsonPath("$.orcamento.servicos[0].servicoId").doesNotExist())
                .andExpect(jsonPath("$.orcamento.pecas[0].itemEstoqueId").doesNotExist());
    }

    @Test
    void clienteAprovaEOEstoqueEBaixado() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/acompanhamento/" + codigo + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"))
                .andExpect(jsonPath("$.orcamento.situacao").value("APROVADO"));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(8))
                .andExpect(jsonPath("$.reservado").value(0));
    }

    @Test
    void clienteRejeitaEAReservaELiberada() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/acompanhamento/" + codigo + "/rejeitar"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJEITADA"))
                .andExpect(jsonPath("$.orcamento.situacao").value("REJEITADO"));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(10))
                .andExpect(jsonPath("$.reservado").value(0));
    }

    @Test
    void clienteNaoRespondeOrcamentoQueAindaNaoFoiEnviado() throws Exception {
        var resposta = abrir();
        String codigo = JsonPath.read(resposta, "$.codigoAcompanhamento");

        mockMvc.perform(post("/acompanhamento/" + codigo + "/aprovar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Só é possível aprovar o orçamento de uma OS aguardando aprovação"));
    }

    @Test
    void codigoDesconhecidoNaoEncontraNada() throws Exception {
        mockMvc.perform(get("/acompanhamento/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Ordem de serviço não encontrada"));
    }

    @Test
    void oIdDaOsNaoServeComoCodigo() throws Exception {
        var resposta = abrir();
        String id = JsonPath.read(resposta, "$.id");

        mockMvc.perform(get("/acompanhamento/" + id)).andExpect(status().isNotFound());
    }

    private String abrirComOrcamentoEnviado() throws Exception {
        var resposta = abrir();
        String id = JsonPath.read(resposta, "$.id");
        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico").with(mecanico())).andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/servicos").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"servicoId\": \"6b2c3d4e-5f6a-4b7c-9d8e-0f1a2b3c4d5e\", \"quantidade\": 1 }"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/pecas").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"itemEstoqueId\": \"" + OLEO_10W30 + "\", \"quantidade\": 2 }"))
                .andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico())).andExpect(status().isOk());
        return JsonPath.read(resposta, "$.codigoAcompanhamento");
    }

    private String abrir() throws Exception {
        return mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
                                  "veiculoId": "7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }

}
