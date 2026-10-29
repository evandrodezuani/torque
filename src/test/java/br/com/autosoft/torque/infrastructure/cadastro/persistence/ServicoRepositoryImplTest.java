package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;

@SpringBootTest
@Transactional
@Sql(statements = "insert into tipo_servico (id, descricao) values ('5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d', 'Troca de óleo')")
class ServicoRepositoryImplTest {

	private static final UUID TROCA_DE_OLEO = UUID.fromString("5a1b2c3d-4e5f-4a6b-8c7d-9e0f1a2b3c4d");

	@Autowired
	private ServicoRepository repository;

	@Test
	void salvaEBuscaOServicoDeVolta() {
		var servico = Servico.cadastrar(TROCA_DE_OLEO, TipoVeiculo.MOTO, Dinheiro.de("50.00"));

		repository.salvar(servico);
		var lido = repository.buscarPorId(servico.getId()).orElseThrow();

		assertThat(lido.getTipoServicoId()).isEqualTo(TROCA_DE_OLEO);
		assertThat(lido.getTipoVeiculo()).isEqualTo(TipoVeiculo.MOTO);
		assertThat(lido.getPreco()).isEqualTo(Dinheiro.de("50.00"));
		assertThat(lido.isAtivo()).isTrue();
	}

	@Test
	void sabeSeOTipoDeServicoJaTemServicoParaOTipoDeVeiculo() {
		repository.salvar(Servico.cadastrar(TROCA_DE_OLEO, TipoVeiculo.CARRO, Dinheiro.de("80.00")));

		assertThat(repository.existePorTipoServicoETipoVeiculo(TROCA_DE_OLEO, TipoVeiculo.CARRO)).isTrue();
		assertThat(repository.existePorTipoServicoETipoVeiculo(TROCA_DE_OLEO, TipoVeiculo.MOTO)).isFalse();
		assertThat(repository.existePorTipoServico(TROCA_DE_OLEO)).isTrue();
	}

}
