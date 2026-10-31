package br.com.autosoft.torque.application.atendimento;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.cadastro.model.Cliente;


@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 1, 0)"
})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, statements = {
        "delete from orcamento_item_peca",
        "delete from orcamento_item_servico",
        "delete from orcamento",
        "delete from ordem_servico",
        "delete from item_estoque",
        "delete from veiculo",
        "delete from cliente"
})
class NotificacaoDeOrcamentoTest {

    private static final String OLEO = "8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f";

    @MockitoBean
    private Notificador notificador;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void avisaOClienteDepoisQueODiagnosticoEConcluido() throws Exception {
        var id = abrirEmDiagnosticoComOleo(1);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico")).andExpect(status().isOk());

        var cliente = ArgumentCaptor.forClass(Cliente.class);
        var acompanhamento = ArgumentCaptor.forClass(Acompanhamento.class);
        verify(notificador).orcamentoDisponivel(cliente.capture(), acompanhamento.capture());
        assertThat(cliente.getValue().getEmail()).isEqualTo("maria@email.com");
        assertThat(acompanhamento.getValue().ordemServico().getStatus()).isEqualTo(StatusOS.AGUARDANDO_APROVACAO);
        assertThat(acompanhamento.getValue().veiculo().getModelo()).isEqualTo("CG 160");
    }

    @Test
    void naoAvisaQuandoAConclusaoEDesfeitaPorFaltaDeEstoque() throws Exception {
        var id = abrirEmDiagnosticoComOleo(3);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico"))
                .andExpect(status().isUnprocessableContent());

        verify(notificador, never()).orcamentoDisponivel(any(), any());
    }

    @Test
    @Transactional
    void naoAvisaEnquantoATransacaoNaoForConfirmada() throws Exception {
        var id = abrirEmDiagnosticoComOleo(1);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico")).andExpect(status().isOk());

        verify(notificador, never()).orcamentoDisponivel(any(), any());
    }

    private String abrirEmDiagnosticoComOleo(int quantidade) throws Exception {
        var resposta = mockMvc.perform(post("/ordens-servico").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clienteId": "3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b",
                                  "veiculoId": "7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        String id = JsonPath.read(resposta, "$.id");
        mockMvc.perform(post("/ordens-servico/" + id + "/iniciar-diagnostico")).andExpect(status().isOk());
        mockMvc.perform(post("/ordens-servico/" + id + "/pecas").contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"itemEstoqueId\": \"" + OLEO + "\", \"quantidade\": " + quantidade + " }"))
                .andExpect(status().isOk());
        return id;
    }

}
