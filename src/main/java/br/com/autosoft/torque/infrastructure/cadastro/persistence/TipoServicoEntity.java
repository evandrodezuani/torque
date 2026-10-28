package br.com.autosoft.torque.infrastructure.cadastro.persistence;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tipo_servico")
@Getter
@Setter
class TipoServicoEntity {

	@Id
	private UUID id;

	private String descricao;

}
