package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.cadastro.model.Cliente;
import br.com.autosoft.torque.domain.cadastro.model.Documento;
import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class ClienteRepositoryImpl implements ClienteRepository {

	private final ClienteJpaRepository jpaRepository;

	@Override
	public void salvar(Cliente cliente) {
		jpaRepository.save(paraEntity(cliente));
	}

	@Override
	public Optional<Cliente> buscarPorId(UUID id) {
		return jpaRepository.findById(id).map(this::paraDominio);
	}

	@Override
	public List<Cliente> listar() {
		return jpaRepository.findAll().stream().map(this::paraDominio).toList();
	}

	@Override
	public boolean existePorDocumento(Documento documento) {
		return jpaRepository.existsByDocumento(documento.numero());
	}

	@Override
	public void remover(UUID id) {
		jpaRepository.deleteById(id);
	}

	private ClienteEntity paraEntity(Cliente cliente) {
		var entity = new ClienteEntity();
		entity.setId(cliente.getId());
		entity.setDocumento(cliente.getDocumento().numero());
		entity.setNome(cliente.getNome());
		entity.setEmail(cliente.getEmail());
		entity.setTelefone(cliente.getTelefone());
		return entity;
	}

	private Cliente paraDominio(ClienteEntity entity) {
		return new Cliente(entity.getId(), new Documento(entity.getDocumento()), entity.getNome(), entity.getEmail(),
				entity.getTelefone());
	}

}
