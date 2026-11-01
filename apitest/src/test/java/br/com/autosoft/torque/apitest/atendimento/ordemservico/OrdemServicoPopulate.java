package br.com.autosoft.torque.apitest.atendimento.ordemservico;

import java.math.BigDecimal;
import java.util.UUID;

import br.com.autosoft.torque.apitest.Populate;

/**
 * O que os testes da OS precisam que já exista no banco. O cliente e o veículo vêm da Populate;
 * aqui ficam o catálogo de serviços e o estoque, que só a OS usa.
 */
public class OrdemServicoPopulate extends Populate {

	public UUID inserirTipoServico(String descricao) {
		var id = UUID.randomUUID();
		executar("insert into tipo_servico (id, descricao) values (?, ?)", id, descricao);
		return id;
	}

	public UUID inserirServico(UUID tipoServicoId, String tipoVeiculo, BigDecimal preco) {
		var id = UUID.randomUUID();
		executar("insert into servico (id, tipo_servico_id, tipo_veiculo, preco, ativo) values (?, ?, ?, ?, ?)",
				id, tipoServicoId, tipoVeiculo, preco, true);
		return id;
	}

	public UUID inserirPeca(String descricao, BigDecimal preco, int saldo) {
		var id = UUID.randomUUID();
		executar("insert into item_estoque (id, tipo, descricao, preco, saldo, reservado) values (?, ?, ?, ?, ?, ?)",
				id, "PECA", descricao, preco, saldo, 0);
		return id;
	}

}
