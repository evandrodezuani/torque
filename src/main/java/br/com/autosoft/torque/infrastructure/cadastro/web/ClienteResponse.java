package br.com.autosoft.torque.infrastructure.cadastro.web;

import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;

record ClienteResponse(UUID id, String documento, String nome, String email, String telefone) {

    public static ClienteResponse paraResposta(Cliente cliente) {
        return new ClienteResponse(cliente.getId(), cliente.getDocumento().numero(), cliente.getNome(),
                cliente.getEmail(), cliente.getTelefone());
    }

}
