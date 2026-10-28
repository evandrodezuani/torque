package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface VeiculoJpaRepository extends JpaRepository<VeiculoEntity, UUID> {

    List<VeiculoEntity> findByClienteId(UUID clienteId);

    boolean existsByPlaca(String placa);

    void deleteByClienteId(UUID clienteId);

}
