package br.com.autosoft.torque.domain.cadastro.model;

import java.time.Year;
import java.util.UUID;

import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import lombok.Getter;

@Getter
public class Veiculo {

	private final UUID id;
	private final UUID clienteId;
	private final Placa placa;
	private String marca;
	private String modelo;
	private int ano;

	public Veiculo(UUID id, UUID clienteId, Placa placa, String marca, String modelo, int ano) {
		this.id = id;
		this.clienteId = clienteId;
		this.placa = placa;
		this.marca = marca;
		this.modelo = modelo;
		this.ano = ano;
	}

	public static Veiculo cadastrar(UUID clienteId, Placa placa, String marca, String modelo, int ano) {
		if (clienteId == null) {
			throw new RegraDeNegocioException("O cliente é obrigatório");
		}
		if (placa == null) {
			throw new RegraDeNegocioException("A placa é obrigatória");
		}

		var veiculo = new Veiculo(UUID.randomUUID(), clienteId, placa, null, null, 0);
		veiculo.alterar(placa, marca, modelo, ano);
		return veiculo;
	}

	public void alterar(Placa placa, String marca, String modelo, int ano) {
		if (!this.placa.equals(placa)) {
			throw new RegraDeNegocioException("A placa não pode ser alterada");
		}

		if (marca == null || marca.isBlank()) {
			throw new RegraDeNegocioException("A marca é obrigatória");
		}

		if (modelo == null || modelo.isBlank()) {
			throw new RegraDeNegocioException("O modelo é obrigatório");
		}

		if (ano < 1900 || ano > Year.now().getValue() + 1) {
			throw new RegraDeNegocioException("Ano inválido");
		}

		this.marca = marca;
		this.modelo = modelo;
		this.ano = ano;
	}

}
