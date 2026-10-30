package br.com.autosoft.torque.domain.atendimento.model;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;

import lombok.Getter;

/**
 * Raiz do agregado. O status muda como consequência dos comandos de negócio abaixo;
 * cada comando começa conferindo em que status a OS precisa estar.
 */
@Getter
public class OrdemServico {

	private final UUID id;
	private final UUID clienteId;
	private final UUID veiculoId;
	private final Instant abertaEm;
	private final List<Orcamento> orcamentos = new ArrayList<>();
	private StatusOS status;
	private Instant execucaoIniciadaEm;
	private Instant execucaoConcluidaEm;
	private Instant entregueEm;

	public OrdemServico(UUID id, UUID clienteId, UUID veiculoId, Instant abertaEm, StatusOS status,
			Instant execucaoIniciadaEm, Instant execucaoConcluidaEm, Instant entregueEm, List<Orcamento> orcamentos) {
		this.id = id;
		this.clienteId = clienteId;
		this.veiculoId = veiculoId;
		this.abertaEm = abertaEm;
		this.status = status;
		this.execucaoIniciadaEm = execucaoIniciadaEm;
		this.execucaoConcluidaEm = execucaoConcluidaEm;
		this.entregueEm = entregueEm;
		this.orcamentos.addAll(orcamentos);
	}

	public static OrdemServico abrir(UUID clienteId, UUID veiculoId, Instant horaAtual) {
		if (clienteId == null || veiculoId == null) {
			throw new RegraDeNegocioException("Cliente e veículo são obrigatórios para abrir a OS");
		}
		return new OrdemServico(UUID.randomUUID(), clienteId, veiculoId, horaAtual, StatusOS.RECEBIDA, null, null, null,
				List.of(new Orcamento(1)));
	}

	public void iniciarDiagnostico() {
		if (status != StatusOS.RECEBIDA) {
			throw new TransicaoInvalidaException("Só é possível iniciar o diagnóstico de uma OS recebida");
		}
		this.status = StatusOS.EM_DIAGNOSTICO;
	}

	public void incluirServico(ItemServico item) {
		if (status != StatusOS.EM_DIAGNOSTICO) {
			throw new RegraDeNegocioException("Só é possível incluir serviços numa OS em diagnóstico");
		}
		orcamentoAtual().adicionar(item);
	}

	public void incluirPeca(ItemPeca item) {
		if (status != StatusOS.EM_DIAGNOSTICO) {
			throw new RegraDeNegocioException("Só é possível incluir peças e insumos numa OS em diagnóstico");
		}
		orcamentoAtual().adicionar(item);
	}

	public void removerServico(UUID servicoId) {
		if (status != StatusOS.EM_DIAGNOSTICO) {
			throw new RegraDeNegocioException("Só é possível remover serviços numa OS em diagnóstico");
		}
		orcamentoAtual().removerServico(servicoId);
	}

	public void removerPeca(UUID itemEstoqueId) {
		if (status != StatusOS.EM_DIAGNOSTICO) {
			throw new RegraDeNegocioException("Só é possível remover peças e insumos numa OS em diagnóstico");
		}
		orcamentoAtual().removerPeca(itemEstoqueId);
	}

	/**
	 * Envia o orçamento com os preços vigentes no catálogo, pela referência de cada linha.
	 * Uma linha sem preço no mapa mantém o preço da inclusão.
	 */
	public void concluirDiagnostico(Instant agora, Map<UUID, Dinheiro> precosAtuais) {
		if (status != StatusOS.EM_DIAGNOSTICO) {
			throw new TransicaoInvalidaException("Só é possível concluir o diagnóstico de uma OS em diagnóstico");
		}
		orcamentoAtual().enviar(agora, precosAtuais);
		this.status = StatusOS.AGUARDANDO_APROVACAO;
	}

	public void aprovarOrcamento(Instant agora) {
		if (status != StatusOS.AGUARDANDO_APROVACAO) {
			throw new TransicaoInvalidaException("Só é possível aprovar o orçamento de uma OS aguardando aprovação");
		}
		orcamentoAtual().aprovar(agora);
		this.status = StatusOS.EM_EXECUCAO;
		this.execucaoIniciadaEm = agora;
	}

	public void rejeitarOrcamento(Instant agora) {
		if (status != StatusOS.AGUARDANDO_APROVACAO) {
			throw new TransicaoInvalidaException("Só é possível rejeitar o orçamento de uma OS aguardando aprovação");
		}
		orcamentoAtual().rejeitar(agora);
		this.status = StatusOS.REJEITADA;
	}

	public void revisarOrcamento() {
		if (status != StatusOS.REJEITADA) {
			throw new TransicaoInvalidaException("Só é possível revisar o orçamento de uma OS rejeitada");
		}
		orcamentos.add(orcamentoAtual().revisar());
		this.status = StatusOS.EM_DIAGNOSTICO;
	}

	public void concluirServico(Instant agora) {
		if (status != StatusOS.EM_EXECUCAO) {
			throw new TransicaoInvalidaException("Só é possível concluir o serviço de uma OS em execução");
		}
		this.status = StatusOS.FINALIZADA;
		this.execucaoConcluidaEm = agora;
	}

	/**
	 * Entrega depois do serviço (Finalizada) ou devolução sem execução (Recebida, Em diagnóstico ou
	 * Rejeitada). Com o orçamento aguardando aprovação há estoque reservado, e em execução o estoque
	 * já foi baixado, por isso esses dois status não entregam.
	 */
	public void entregarVeiculo(Instant agora) {
		var statusPodeEntregar = EnumSet.of(StatusOS.FINALIZADA, StatusOS.REJEITADA, StatusOS.RECEBIDA, StatusOS.EM_DIAGNOSTICO);
		if (!statusPodeEntregar .contains(status)) {
			throw new TransicaoInvalidaException(
					"Só é possível entregar o veículo de uma OS recebida, em diagnóstico, rejeitada ou finalizada");
		}
		this.status = StatusOS.ENTREGUE;
		this.entregueEm = agora;
	}

	public Orcamento orcamentoAtual() {
		return orcamentos.getLast();
	}

	public Optional<Duration> tempoDeExecucao() {
		if (execucaoIniciadaEm == null || execucaoConcluidaEm == null) {
			return Optional.empty();
		}
		return Optional.of(Duration.between(execucaoIniciadaEm, execucaoConcluidaEm));
	}

	public List<Orcamento> getOrcamentos() {
		return List.copyOf(orcamentos);
	}

}
