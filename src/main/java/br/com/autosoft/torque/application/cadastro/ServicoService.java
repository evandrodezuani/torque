package br.com.autosoft.torque.application.cadastro;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Servico;
import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ServicoService {

	private final ServicoRepository repository;
	private final TipoServicoRepository tipoServicoRepository;

	@Transactional
	public Servico cadastrar(UUID tipoServicoId, TipoVeiculo tipoVeiculo, BigDecimal preco) {
		if (tipoServicoRepository.buscarPorId(tipoServicoId).isEmpty()) {
			throw new NaoEncontradoException("Tipo de serviço não encontrado");
		}
		if (repository.existePorTipoServicoETipoVeiculo(tipoServicoId, tipoVeiculo)) {
			throw new RegraDeNegocioException("Já existe serviço deste tipo para este tipo de veículo");
		}
		var servico = Servico.cadastrar(tipoServicoId, tipoVeiculo, new Dinheiro(preco));
		repository.salvar(servico);
		return servico;
	}

	public Servico consultar(UUID id) {
		return repository.buscarPorId(id)
				.orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));
	}

	public List<Servico> listar() {
		return repository.listar();
	}

	@Transactional
	public Servico alterar(UUID id, UUID tipoServicoId, TipoVeiculo tipoVeiculo, BigDecimal preco, boolean ativo) {
		var servico = consultar(id);
		servico.alterar(tipoServicoId, tipoVeiculo, new Dinheiro(preco), ativo);
		repository.salvar(servico);
		return servico;
	}

	@Transactional
	public void remover(UUID id) {
		consultar(id);
		repository.remover(id);
	}

}
