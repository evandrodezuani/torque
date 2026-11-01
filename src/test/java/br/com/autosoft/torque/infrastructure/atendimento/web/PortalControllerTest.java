package br.com.autosoft.torque.infrastructure.atendimento.web;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
class PortalControllerTest {

    private static final String OLEO_10W30 = "8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void mostraOOrcamentoComOsBotoes() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(get("/portal/" + codigo))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Honda CG 160")))
                .andExpect(content().string(containsString("Aguardando a sua resposta")))
                .andExpect(content().string(containsString("R$ 126,00")))
                .andExpect(content().string(containsString("action=\"/portal/" + codigo + "/aprovar\"")))
                .andExpect(content().string(containsString("action=\"/portal/" + codigo + "/rejeitar\"")));
    }

    @Test
    void aprovarMostraAPaginaDeResultadoEBaixaOEstoque() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/portal/" + codigo + "/aprovar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orçamento aprovado")));

        mockMvc.perform(get("/itens-estoque/" + OLEO_10W30).with(almoxarife()))
                .andExpect(jsonPath("$.saldo").value(8))
                .andExpect(jsonPath("$.reservado").value(0));
    }

    @Test
    void rejeitarMostraAPaginaDeResultado() throws Exception {
        var codigo = abrirComOrcamentoEnviado();

        mockMvc.perform(post("/portal/" + codigo + "/rejeitar"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Orçamento rejeitado")));
    }

    @Test
    void depoisDeRespondidoOOrcamentoNaoMostraMaisOsBotoes() throws Exception {
        var codigo = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/portal/" + codigo + "/aprovar")).andExpect(status().isOk());

        mockMvc.perform(get("/portal/" + codigo))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Aprovado")))
                .andExpect(content().string(not(containsString("/aprovar\""))));
    }

    @Test
    void responderDuasVezesMostraAPaginaDeErro() throws Exception {
        var codigo = abrirComOrcamentoEnviado();
        mockMvc.perform(post("/portal/" + codigo + "/aprovar")).andExpect(status().isOk());

        mockMvc.perform(post("/portal/" + codigo + "/aprovar"))
                .andExpect(status().isUnprocessableContent())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
                .andExpect(content().string(containsString("Só é possível aprovar o orçamento de uma OS aguardando aprovação")));
    }

    @Test
    void codigoDesconhecidoMostraAPaginaDeErro() throws Exception {
        mockMvc.perform(get("/portal/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Orçamento não encontrado")));
    }

    private String abrirComOrcamentoEnviado() throws Exception {
        var resposta = mockMvc.perform(post("/ordens-servico").with(atendente()).contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
                                  "veiculoId": "7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
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

}
