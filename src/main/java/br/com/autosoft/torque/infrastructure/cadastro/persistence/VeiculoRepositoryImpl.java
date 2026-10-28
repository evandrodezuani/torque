package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import br.com.autosoft.torque.domain.cadastro.model.Placa;
import br.com.autosoft.torque.domain.cadastro.model.Veiculo;
import br.com.autosoft.torque.domain.cadastro.repository.VeiculoRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
class VeiculoRepositoryImpl implements VeiculoRepository {

    private final VeiculoJpaRepository jpaRepository;

    @Override
    public void salvar(Veiculo veiculo) {
        jpaRepository.save(paraEntity(veiculo));
    }

    @Override
    public Optional<Veiculo> buscarPorId(UUID id) {
        return jpaRepository.findById(id).map(this::paraDominio);
    }

    @Override
    public List<Veiculo> listarPorCliente(UUID clienteId) {
        return jpaRepository.findByClienteId(clienteId).stream().map(this::paraDominio).toList();
    }

    @Override
    public boolean existePorPlaca(Placa placa) {
        return jpaRepository.existsByPlaca(placa.numero());
    }

    @Override
    public void remover(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void removerPorCliente(UUID clienteId) {
        jpaRepository.deleteByClienteId(clienteId);
    }

    private VeiculoEntity paraEntity(Veiculo veiculo) {
        var entity = new VeiculoEntity();
        entity.setId(veiculo.getId());
        entity.setClienteId(veiculo.getClienteId());
        entity.setPlaca(veiculo.getPlaca().numero());
        entity.setMarca(veiculo.getMarca());
        entity.setModelo(veiculo.getModelo());
        entity.setAno(veiculo.getAno());
        return entity;
    }

    private Veiculo paraDominio(VeiculoEntity entity) {
        return new Veiculo(entity.getId(), entity.getClienteId(), new Placa(entity.getPlaca()), entity.getMarca(),
                entity.getModelo(), entity.getAno());
    }

}
