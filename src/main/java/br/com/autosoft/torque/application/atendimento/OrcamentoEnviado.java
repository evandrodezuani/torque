package br.com.autosoft.torque.application.atendimento;

import java.util.UUID;

/**
 * Publicado quando o diagnóstico é concluído e o orçamento fica disponível para o cliente.
 */
public record OrcamentoEnviado(UUID codigoAcompanhamento) {

}
