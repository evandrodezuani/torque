package br.com.autosoft.torque.infrastructure.shared.security;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "usuario")
@Getter
@Setter
class UsuarioEntity {

	@Id
	private UUID id;
	private String login;
	private String senha;
	private boolean ativo;
	private UUID clienteId;
	private UUID tokenSenha;
	private Instant tokenSenhaExpiraEm;
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "usuario_papel", joinColumns = @JoinColumn(name = "usuario_id"))
	@Column(name = "papel")
	@Enumerated(EnumType.STRING)
	private Set<Papel> papeis = new HashSet<>();

}
