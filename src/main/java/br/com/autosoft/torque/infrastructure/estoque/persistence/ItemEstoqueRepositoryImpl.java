package br.com.autosoft.torque.infrastructure.estoque.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.estoque.model.ItemEstoque;
import br.com.autosoft.torque.domain.estoque.repository.ItemEstoqueRepository;
import br.com.autosoft.torque.domain.shared.Dinheiro;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ItemEstoqueRepositoryImpl implements ItemEstoqueRepository {

	private final ItemEstoqueJpaRepository jpaRepository;

	@Override
	public void salvar(ItemEstoque item) {
		jpaRepository.save(paraEntity(item));
	}

	@Override
	public Optional<ItemEstoque> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	public List<ItemEstoque> listar() {
		return jpaRepository.findAll().stream().map(this::paraDominio).toList();
	}

	@Override
	public boolean existePorDescricao(String descricao) {
		return jpaRepository.existsByDescricao(descricao);
	}

	@Override
	public void remover(UUID id) {
		jpaRepository.deleteById(id);
	}

	private ItemEstoqueEntity paraEntity(ItemEstoque item) {
		var entity = new ItemEstoqueEntity();
		entity.setId(item.getId());
		entity.setTipo(item.getTipo());
		entity.setDescricao(item.getDescricao());
		entity.setPreco(item.getPreco().valor());
		entity.setSaldo(item.getSaldo());
		entity.setReservado(item.getReservado());
		return entity;
	}

	private ItemEstoque paraDominio(ItemEstoqueEntity entity) {
		return new ItemEstoque(entity.getId(), entity.getTipo(), entity.getDescricao(),
				new Dinheiro(entity.getPreco()), entity.getSaldo(), entity.getReservado());
	}

}
