package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
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

	private String marca;

	private String modelo;

	private int ano;

}
