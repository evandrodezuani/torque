package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import br.com.autosoft.torque.domain.cadastro.model.TipoVeiculo;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "veiculo")
@Getter
@Setter
class VeiculoEntity {

	@Id
	private UUID id;

	private UUID clienteId;

	private String placa;

	@Enumerated(EnumType.STRING)
	private TipoVeiculo tipo;

	private String marca;

	private String modelo;

	private int ano;

}
