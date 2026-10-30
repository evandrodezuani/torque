package br.com.autosoft.torque.domain.atendimento.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

/**
 * Entidade interna do agregado OrdemServico. Só a OrdemServico cria e altera orçamentos,
 * por isso os métodos que mudam o estado não são públicos. O construtor público serve
 * para o repositório remontar o orçamento lido do banco.
 */
@Getter
public class Orcamento {

	private final UUID id;
	private final int numero;
	private final List<ItemServico> itensServico = new ArrayList<>();
	private final List<ItemPeca> itensPeca = new ArrayList<>();
	private SituacaoOrcamento situacao;
	private Instant enviadoEm;
	private Instant respondidoEm;

	Orcamento(int numero) {
		this(UUID.randomUUID(), numero, SituacaoOrcamento.EM_ELABORACAO, null, null, List.of(), List.of());
	}

	public Orcamento(UUID id, int numero, SituacaoOrcamento situacao, Instant enviadoEm, Instant respondidoEm,
			List<ItemServico> itensServico, List<ItemPeca> itensPeca) {
		this.id = id;
		this.numero = numero;
		this.situacao = situacao;
		this.enviadoEm = enviadoEm;
		this.respondidoEm = respondidoEm;
		this.itensServico.addAll(itensServico);
		this.itensPeca.addAll(itensPeca);
	}

	Orcamento revisar() {
		Orcamento revisado = new Orcamento(numero + 1);
		revisado.itensServico.addAll(itensServico);
		revisado.itensPeca.addAll(itensPeca);
		return revisado;
	}

	void adicionar(ItemServico item) {
		incluirOuSomar(itensServico, item);
	}

	void adicionar(ItemPeca item) {
		incluirOuSomar(itensPeca, item);
	}

	void removerServico(UUID servicoId) {
		remover(itensServico, servicoId, "Serviço não encontrado no orçamento");
	}

	void removerPeca(UUID itemEstoqueId) {
		remover(itensPeca, itemEstoqueId, "Peça ou insumo não encontrado no orçamento");
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
		return Stream.concat(itensServico.stream(), itensPeca.stream())
				.map(ItemOrcamento::subtotal)
				.reduce(Dinheiro.ZERO, Dinheiro::somar);
	}

	public List<ItemServico> getItensServico() {
		return List.copyOf(itensServico);
	}

	public List<ItemPeca> getItensPeca() {
		return List.copyOf(itensPeca);
	}

	private <T extends ItemOrcamento<T>> void incluirOuSomar(List<T> itens, T item) {
		exigirSituacao(SituacaoOrcamento.EM_ELABORACAO);
		if (itens.stream().anyMatch(existente -> existente.referencia().equals(item.referencia()))) {
			itens.replaceAll(existente -> existente.referencia().equals(item.referencia())
					? existente.somar(item.quantidade())
					: existente);
		} else {
			itens.add(item);
		}
	}

	private <T extends ItemOrcamento<T>> void remover(List<T> itens, UUID referencia, String mensagem) {
		exigirSituacao(SituacaoOrcamento.EM_ELABORACAO);
		if (!itens.removeIf(item -> item.referencia().equals(referencia))) {
			throw new NaoEncontradoException(mensagem);
		}
	}

	private void exigirSituacao(SituacaoOrcamento esperada) {
		if (situacao != esperada) {
			throw new RegraDeNegocioException(
					"O orçamento " + numero + " está " + situacao + ", mas a operação exige " + esperada);
		}
	}

}
