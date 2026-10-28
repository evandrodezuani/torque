package br.com.autosoft.torque.application.cadastro;

import java.util.List;
import java.util.UUID;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ClienteService {

	private final ClienteRepository repository;
	private final OrdemServicoRepository ordemServicoRepository;
	private final VeiculoRepository veiculoRepository;

	@Transactional
	public Cliente cadastrar(String documento, String nome, String email, String telefone) {
		var documentoDoCliente = new Documento(documento);
		if (repository.existePorDocumento(documentoDoCliente)) {
			throw new RegraDeNegocioException("Já existe cliente com este documento");
		}
		var cliente = Cliente.cadastrar(documentoDoCliente, nome, email, telefone);
		repository.salvar(cliente);
		return cliente;
	}

	public Cliente consultar(UUID id) {
		return repository.buscarPorId(id)
				.orElseThrow(() -> new NaoEncontradoException("Cliente não encontrado"));
	}

	public List<Cliente> listar() {
		return repository.listar();
	}

	@Transactional
	public Cliente alterar(UUID id, String documento, String nome, String email, String telefone) {
		var cliente = consultar(id);
		cliente.alterar(new Documento(documento), nome, email, telefone);
		repository.salvar(cliente);
		return cliente;
	}

	@Transactional
	public void remover(UUID id) {
		consultar(id);
		if (ordemServicoRepository.existeEmAndamentoPorCliente(id)) {
			throw new RegraDeNegocioException("O cliente tem ordens de serviço em andamento e não pode ser removido");
		}
		ordemServicoRepository.removerPorCliente(id);
		veiculoRepository.removerPorCliente(id);
		repository.remover(id);
	}

}
