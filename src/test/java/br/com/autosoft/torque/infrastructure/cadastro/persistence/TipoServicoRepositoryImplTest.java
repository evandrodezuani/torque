package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;

@SpringBootTest
@Transactional
class TipoServicoRepositoryImplTest {

	@Autowired
	private TipoServicoRepository repository;

	@Test
	void salvaEBuscaOTipoDeServicoDeVolta() {
		var trocaDeOleo = TipoServico.cadastrar("Troca de óleo");

		repository.salvar(trocaDeOleo);

		assertThat(repository.buscarPorId(trocaDeOleo.getId()).orElseThrow().getDescricao()).isEqualTo("Troca de óleo");
	}

	@Test
	void sabeSeADescricaoJaFoiCadastrada() {
		repository.salvar(TipoServico.cadastrar("Troca de óleo"));

		assertThat(repository.existePorDescricao("Troca de óleo")).isTrue();
		assertThat(repository.existePorDescricao("Alinhamento")).isFalse();
	}

	@Test
	void removeOTipoDeServico() {
		var trocaDeOleo = TipoServico.cadastrar("Troca de óleo");
		repository.salvar(trocaDeOleo);

		repository.remover(trocaDeOleo.getId());

		assertThat(repository.buscarPorId(trocaDeOleo.getId())).isEmpty();
	}

}
