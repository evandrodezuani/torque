package br.com.autosoft.torque.application.cadastro;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.TipoServico;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TipoServicoService {

	private final TipoServicoRepository repository;
	private final ServicoRepository servicoRepository;

	@Transactional
	public TipoServico cadastrar(String descricao) {
		if (repository.existePorDescricao(descricao)) {
			throw new RegraDeNegocioException("Já existe tipo de serviço com esta descrição");
		}
		var tipoServico = TipoServico.cadastrar(descricao);
		repository.salvar(tipoServico);
		return tipoServico;
	}

	public TipoServico consultar(UUID id) {
		return repository.buscarPorId(id)
				.orElseThrow(() -> new NaoEncontradoException("Tipo de serviço não encontrado"));
	}

	public List<TipoServico> listar() {
		return repository.listar();
	}

	@Transactional
	public TipoServico alterar(UUID id, String descricao) {
		var tipoServico = consultar(id);
		if (!tipoServico.getDescricao().equals(descricao) && repository.existePorDescricao(descricao)) {
			throw new RegraDeNegocioException("Já existe tipo de serviço com esta descrição");
		}
		tipoServico.alterar(descricao);
		repository.salvar(tipoServico);
		return tipoServico;
	}

	@Transactional
	public void remover(UUID id) {
		consultar(id);
		if (servicoRepository.existePorTipoServico(id)) {
			throw new RegraDeNegocioException("O tipo de serviço tem serviços cadastrados e não pode ser removido");
		}
		repository.remover(id);
	}

}
