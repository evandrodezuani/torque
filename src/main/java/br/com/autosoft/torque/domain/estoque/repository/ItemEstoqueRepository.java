package br.com.autosoft.torque.domain.estoque.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;

public interface ItemEstoqueRepository {

	void salvar(ItemEstoque item);

	Optional<ItemEstoque> buscarPorId(UUID id);

	List<ItemEstoque> listar();

	boolean existePorDescricao(String descricao);

	void remover(UUID id);

}
