package br.com.autosoft.torque.application.atendimento;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.model.ItemPeca;
import br.com.autosoft.torque.domain.atendimento.model.ItemServico;
import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import br.com.autosoft.torque.domain.cadastro.repository.ServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.TipoServicoRepository;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrdemServicoService {

	private final OrdemServicoRepository repository;
	private final ClienteRepository clienteRepository;
	private final VeiculoRepository veiculoRepository;
	private final ServicoRepository servicoRepository;
	private final TipoServicoRepository tipoServicoRepository;
	private final ItemEstoqueRepository itemEstoqueRepository;

	@Transactional
	public OrdemServico abrir(UUID clienteId, UUID veiculoId) {
		if (clienteRepository.buscarPorId(clienteId).isEmpty()) {
			throw new NaoEncontradoException("Cliente não encontrado");
		}
		var veiculo = veiculoRepository.buscarPorId(veiculoId)
				.orElseThrow(() -> new NaoEncontradoException("Veículo não encontrado"));
		if (!veiculo.getClienteId().equals(clienteId)) {
			throw new RegraDeNegocioException("O veículo não pertence ao cliente");
		}

		var ordemServico = OrdemServico.abrir(clienteId, veiculoId, Instant.now());
		repository.salvar(ordemServico);
		return ordemServico;
	}

	public OrdemServico consultar(UUID id) {
		return repository.buscarPorId(id)
				.orElseThrow(() -> new NaoEncontradoException("Ordem de serviço não encontrada"));
	}

	public List<OrdemServico> listar(StatusOS status, boolean maisAntigasPrimeiro) {
		return repository.listar(status, maisAntigasPrimeiro);
	}

	@Transactional
	public OrdemServico iniciarDiagnostico(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.iniciarDiagnostico();
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico incluirServico(UUID id, UUID servicoId, int quantidade) {
		var ordemServico = consultar(id);
		var servico = servicoRepository.buscarPorId(servicoId)
				.orElseThrow(() -> new NaoEncontradoException("Serviço não encontrado"));
		if (!servico.isAtivo()) {
			throw new RegraDeNegocioException("O serviço está inativo");
		}
		var veiculo = veiculoRepository.buscarPorId(ordemServico.getVeiculoId()).orElseThrow();
		if (servico.getTipoVeiculo() != veiculo.getTipo()) {
			throw new RegraDeNegocioException("O serviço não é para este tipo de veículo");
		}
		var tipoServico = tipoServicoRepository.buscarPorId(servico.getTipoServicoId()).orElseThrow();

		ordemServico.incluirServico(
				new ItemServico(servico.getId(), tipoServico.getDescricao(), servico.getPreco(), quantidade));
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico incluirPeca(UUID id, UUID itemEstoqueId, int quantidade) {
		var ordemServico = consultar(id);
		var item = itemEstoqueRepository.buscarPorId(itemEstoqueId)
				.orElseThrow(() -> new NaoEncontradoException("Item de estoque não encontrado"));

		ordemServico.incluirPeca(new ItemPeca(item.getId(), item.getDescricao(), item.getPreco(), quantidade));
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico removerServico(UUID id, UUID servicoId) {
		var ordemServico = consultar(id);
		ordemServico.removerServico(servicoId);
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico removerPeca(UUID id, UUID itemEstoqueId) {
		var ordemServico = consultar(id);
		ordemServico.removerPeca(itemEstoqueId);
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico concluirDiagnostico(UUID id) {
		var ordemServico = consultar(id);

		var precosAtuais = new HashMap<UUID, Dinheiro>();
		for (var linha : ordemServico.orcamentoAtual().getItensServico()) {
			var servico = servicoRepository.buscarPorId(linha.servicoId()).orElseThrow();
			if (!servico.isAtivo()) {
				throw new RegraDeNegocioException("O serviço " + linha.descricao() + " está inativo");
			}
			precosAtuais.put(servico.getId(), servico.getPreco());
		}
		for (var linha : ordemServico.orcamentoAtual().getItensPeca()) {
			var item = itemEstoqueRepository.buscarPorId(linha.itemEstoqueId()).orElseThrow();
			precosAtuais.put(item.getId(), item.getPreco());
		}

		ordemServico.concluirDiagnostico(Instant.now(), precosAtuais);
		movimentarEstoque(ordemServico, ItemEstoque::reservar);
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico aprovarOrcamento(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.aprovarOrcamento(Instant.now());
		movimentarEstoque(ordemServico, ItemEstoque::baixar);
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico rejeitarOrcamento(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.rejeitarOrcamento(Instant.now());
		movimentarEstoque(ordemServico, ItemEstoque::liberarReserva);
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico revisarOrcamento(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.revisarOrcamento();
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico concluirServico(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.concluirServico(Instant.now());
		repository.salvar(ordemServico);
		return ordemServico;
	}

	@Transactional
	public OrdemServico entregarVeiculo(UUID id) {
		var ordemServico = consultar(id);
		ordemServico.entregarVeiculo(Instant.now());
		repository.salvar(ordemServico);
		return ordemServico;
	}

	private void movimentarEstoque(OrdemServico ordemServico, BiConsumer<ItemEstoque, Integer> movimento) {
		for (var peca : ordemServico.orcamentoAtual().getItensPeca()) {
			var item = itemEstoqueRepository.buscarPorId(peca.itemEstoqueId()).orElseThrow();
			movimento.accept(item, peca.quantidade());
			itemEstoqueRepository.salvar(item);
		}
	}

}
