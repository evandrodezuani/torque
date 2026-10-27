package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "cliente")
@Getter
@Setter
class ClienteEntity {

	@Id
	private UUID id;

	private String documento;

	private String nome;

	private String email;

	private String telefone;

}
