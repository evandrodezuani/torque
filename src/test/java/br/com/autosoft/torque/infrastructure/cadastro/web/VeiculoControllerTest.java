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
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.jayway.jsonpath.JsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Sql(statements = {
        "insert into cliente (id, documento, nome, email) values ('3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b', '52998224725', 'Maria Souza', 'maria@email.com')",
        "insert into cliente (id, documento, nome, email) values ('9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d', '11222333000181', 'Oficina Amiga', 'contato@amiga.com')"
})
class VeiculoControllerTest {

    private static final String VEICULOS_DA_MARIA = "/clientes/3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b/veiculos";
    private static final String VEICULOS_DO_OUTRO_CLIENTE = "/clientes/9c8b7a6d-5e4f-4a3b-8c2d-1e0f9a8b7c6d/veiculos";

    private static final String MOTO = """
			{
			  "placa": "bra-2e19",
			  "tipo": "MOTO",
			  "marca": "Honda",
			  "modelo": "CG 160",
			  "ano": 2022
			}
			""";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void cadastraEListaOsVeiculosDoCliente() throws Exception {
        cadastrar(MOTO);
        cadastrar(MOTO.replace("bra-2e19", "ABC1234").replace("MOTO", "CARRO").replace("CG 160", "Uno")
                .replace("Honda", "Fiat"));

        mockMvc.perform(get(VEICULOS_DA_MARIA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.placa == 'BRA2E19')].tipo").value("MOTO"))
                .andExpect(jsonPath("$[?(@.placa == 'ABC1234')].tipo").value("CARRO"));
    }

    @Test
    void recusaVeiculoSemTipo() throws Exception {
        mockMvc.perform(post(VEICULOS_DA_MARIA).contentType(MediaType.APPLICATION_JSON)
                        .content(MOTO.replace("\"tipo\": \"MOTO\",", "")))
                .andExpect(status().isBadRequest());
    }

    @Test
    void recusaPlacaInvalida() throws Exception {
        mockMvc.perform(post(VEICULOS_DA_MARIA).contentType(MediaType.APPLICATION_JSON)
                        .content(MOTO.replace("bra-2e19", "BR-12345")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("Placa inválida"));
    }

    @Test
    void naoMostraVeiculoPeloEnderecoDeOutroCliente() throws Exception {
        var id = cadastrar(MOTO);

        mockMvc.perform(get(VEICULOS_DO_OUTRO_CLIENTE + "/" + id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("Veículo não encontrado"));
    }

    @Test
    void naoDeixaAlterarAPlaca() throws Exception {
        var id = cadastrar(MOTO);

        mockMvc.perform(put(VEICULOS_DA_MARIA + "/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(MOTO.replace("bra-2e19", "ABC1234")))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("A placa não pode ser alterada"));
    }

    @Test
    void removeOVeiculo() throws Exception {
        var id = cadastrar(MOTO);

        mockMvc.perform(delete(VEICULOS_DA_MARIA + "/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get(VEICULOS_DA_MARIA + "/" + id)).andExpect(status().isNotFound());
    }

    private String cadastrar(String json) throws Exception {
        var resposta = mockMvc.perform(post(VEICULOS_DA_MARIA).contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.clienteId").value("3f2a1c4e-8b7d-4e6f-9a1b-2c3d4e5f6a7b"))
                .andReturn().getResponse().getContentAsString();
        return JsonPath.read(resposta, "$.id");
    }

}
