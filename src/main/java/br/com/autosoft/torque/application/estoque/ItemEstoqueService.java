package br.com.autosoft.torque.application.estoque;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.model.TipoItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import br.com.autosoft.torque.domain.shared.NaoEncontradoException;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ItemEstoqueService {

	private final ItemEstoqueRepository repository;
	private final OrdemServicoRepository ordemServicoRepository;

	@Transactional
	public ItemEstoque cadastrar(TipoItemEstoque tipo, String descricao, BigDecimal preco) {
		if (repository.existePorDescricao(descricao)) {
			throw new RegraDeNegocioException("Já existe item de estoque com esta descrição");
		}
		var item = ItemEstoque.cadastrar(tipo, descricao, new Dinheiro(preco));
		repository.salvar(item);
		return item;
	}

	public ItemEstoque consultar(UUID id) {
		return repository.buscarPorId(id)
				.orElseThrow(() -> new NaoEncontradoException("Item de estoque não encontrado"));
	}

	public List<ItemEstoque> listar() {
		return repository.listar();
	}

	@Transactional
	public ItemEstoque alterar(UUID id, TipoItemEstoque tipo, String descricao, BigDecimal preco) {
		var item = consultar(id);
		if (!item.getDescricao().equals(descricao) && repository.existePorDescricao(descricao)) {
			throw new RegraDeNegocioException("Já existe item de estoque com esta descrição");
		}
		item.alterar(tipo, descricao, new Dinheiro(preco));
		repository.salvar(item);
		return item;
	}

	@Transactional
	public ItemEstoque registrarEntrada(UUID id, int quantidade) {
		var item = consultar(id);
		item.registrarEntrada(quantidade);
		repository.salvar(item);
		return item;
	}

	@Transactional
	public void remover(UUID id) {
		var item = consultar(id);
		if (item.getReservado() > 0) {
			throw new RegraDeNegocioException("O item tem quantidade reservada e não pode ser removido");
		}
		if (ordemServicoRepository.existeComItemEstoque(id)) {
			throw new RegraDeNegocioException("O item já foi usado em orçamentos e não pode ser removido");
		}
		repository.remover(id);
	}

}
