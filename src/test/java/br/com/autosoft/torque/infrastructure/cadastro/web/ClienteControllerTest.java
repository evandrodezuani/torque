package br.com.autosoft.torque.infrastructure.cadastro.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClienteControllerTest {

    private static final String MARIA = """
			{
			  "documento": "529.982.247-25",
			  "nome": "Maria Souza",
			  "email": "maria@email.com",
			  "telefone": "11999990000"
			}
			""";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastraEConsultaOCliente() throws Exception {
        var id = cadastrar(MARIA);

        mockMvc.perform(get("/clientes/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.documento").value("52998224725"))
                .andExpect(jsonPath("$.nome").value("Maria Souza"));
    }

    @Test
    void recusaDocumentoInvalido() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(MARIA.replace("529.982.247-25", "529.982.247-00")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("CPF ou CNPJ inválido"));
    }

    @Test
    void recusaPedidoSemNome() throws Exception {
        mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON)
                        .content(MARIA.replace("\"Maria Souza\"", "\"\"")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void naoDeixaAlterarODocumento() throws Exception {
        var id = cadastrar(MARIA);

        mockMvc.perform(put("/clientes/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(MARIA.replace("529.982.247-25", "11.222.333/0001-81")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("O documento não pode ser alterado"));
    }

    @Test
    void removeOCliente() throws Exception {
        var id = cadastrar(MARIA);

        mockMvc.perform(delete("/clientes/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/clientes/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Cliente não encontrado"));
    }

    private String cadastrar(String json) throws Exception {
        var resposta = mockMvc.perform(post("/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

}
