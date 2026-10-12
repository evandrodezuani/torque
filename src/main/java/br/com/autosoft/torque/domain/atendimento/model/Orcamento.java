package br.com.autosoft.torque.domain.atendimento.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

/**
 * Entidade interna do agregado OrdemServico. Só a OrdemServico cria e altera orçamentos,
 * por isso os métodos que mudam o estado não são públicos.
 */
@Getter
public class Orcamento {

	private final int numero;
	private final List<ItemServico> itensServico = new ArrayList<>();
	private final List<ItemPeca> itensPeca = new ArrayList<>();
	private SituacaoOrcamento situacao = SituacaoOrcamento.EM_ELABORACAO;
	private Instant enviadoEm;
	private Instant respondidoEm;

	Orcamento(int numero) {
		this.numero = numero;
	}

	Orcamento revisar() {
		Orcamento revisado = new Orcamento(numero + 1);
		revisado.itensServico.addAll(itensServico);
		revisado.itensPeca.addAll(itensPeca);
		return revisado;
	}

	void adicionar(ItemServico item) {
		exigirSituacao(SituacaoOrcamento.EM_ELABORACAO);
		itensServico.add(item);
	}

	void adicionar(ItemPeca item) {
		exigirSituacao(SituacaoOrcamento.EM_ELABORACAO);
		itensPeca.add(item);
	}

	void enviar(Instant agora) {
		exigirSituacao(SituacaoOrcamento.EM_ELABORACAO);
		if (itensServico.isEmpty() && itensPeca.isEmpty()) {
			throw new RegraDeNegocioException("O orçamento precisa de pelo menos um serviço ou peça");
		}
		this.situacao = SituacaoOrcamento.PENDENTE;
		this.enviadoEm = agora;
	}

	void aprovar(Instant agora) {
		exigirSituacao(SituacaoOrcamento.PENDENTE);
		this.situacao = SituacaoOrcamento.APROVADO;
		this.respondidoEm = agora;
	}

	void rejeitar(Instant agora) {
		exigirSituacao(SituacaoOrcamento.PENDENTE);
		this.situacao = SituacaoOrcamento.REJEITADO;
		this.respondidoEm = agora;
	}

	public Dinheiro total() {
		Dinheiro total = Dinheiro.ZERO;
		for (ItemServico item : itensServico) {
			total = total.somar(item.subtotal());
		}
		for (ItemPeca item : itensPeca) {
			total = total.somar(item.subtotal());
		}
		return total;
	}

	public List<ItemServico> getItensServico() {
		return List.copyOf(itensServico);
	}

	public List<ItemPeca> getItensPeca() {
		return List.copyOf(itensPeca);
	}

	private void exigirSituacao(SituacaoOrcamento esperada) {
		if (situacao != esperada) {
			throw new RegraDeNegocioException(
					"O orçamento " + numero + " está " + situacao + ", mas a operação exige " + esperada);
		}
	}

}
