package br.com.autosoft.torque.infrastructure.atendimento.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.almoxarife;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.atendente;
import static br.com.autosoft.torque.infrastructure.shared.security.UsuariosDeTeste.mecanico;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import com.jayway.jsonpath.JsonPath;

import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values ('7b6a5f4e-3d2c-4b1a-9f8e-7d6c5b4a3f2e', '3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', 'BRA2E19', 'MOTO', 'Honda', 'CG 160', 2022)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f', 'PECA', 'Filtro de óleo', 35.00, 10, 0)",
        "insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values ('9d4e5f6a-7b8c-4d9e-8f0a-2b3c4d5e6f7a', 'INSUMO', 'Óleo 10W30 1 L', 38.00, 1, 0)"
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
class ConcluirDiagnosticoSemEstoqueTest {

    private static final String FILTRO = "8c3d4e5f-6a7b-4c8d-9e0f-1a2b3c4d5e6f";
    private static final String OLEO = "9d4e5f6a-7b8c-4d9e-8f0a-2b3c4d5e6f7a";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OrdemServicoRepository ordemServicoRepository;

    @Test
    void faltaDeEstoqueDesfazAReservaEMantemAOsEmDiagnostico() throws Exception {
        var id = abrirEmDiagnostico();
        incluirPeca(id, FILTRO, 2);
        incluirPeca(id, OLEO, 3);

        mockMvc.perform(post("/ordens-servico/" + id + "/concluir-diagnostico").with(mecanico()))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Estoque insuficiente para Óleo 10W30 1 L"));

        mockMvc.perform(get("/itens-estoque/" + FILTRO).with(almoxarife())).andExpect(jsonPath("$.reservado").value(0));
        mockMvc.perform(get("/itens-estoque/" + OLEO).with(almoxarife())).andExpect(jsonPath("$.reservado").value(0));
        var ordemServico = ordemServicoRepository.buscarPorId(UUID.fromString(id)).orElseThrow();
        assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.EM_DIAGNOSTICO);
    }

    private String abrirEmDiagnostico() throws Exception {
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
        return id;
    }

    private void incluirPeca(String id, String itemEstoqueId, int quantidade) throws Exception {
        mockMvc.perform(post("/ordens-servico/" + id + "/pecas").with(mecanico()).contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"itemEstoqueId\": \"" + itemEstoqueId + "\", \"quantidade\": " + quantidade + " }"))
                .andExpect(status().isOk());
    }

}
