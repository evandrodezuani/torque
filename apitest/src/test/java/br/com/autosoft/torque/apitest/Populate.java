package br.com.autosoft.torque.apitest;

import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.UUID;

import br.com.caelum.stella.validation.CPFValidator;

/**
 * Base das classes Populate: grava direto no banco dos testes o que um teste precisa que já exista.
 * Aqui ficam os inserts usados por mais de um recurso; os de um recurso só ficam no Populate dele.
 */
public abstract class Populate {

	private static final String USUARIOS_DOS_TESTES = "cliente_id is not null or login like '%@apitest.local'";

	public UUID inserirCliente() {
		return inserirCliente(new CPFValidator().generateRandomValid(), ApiTest.emailNovo());
	}

	public UUID inserirCliente(String documento, String email) {
		var id = UUID.randomUUID();
		executar("insert into cliente (id, documento, nome, email) values (?, ?, ?, ?)",
				id, documento, "Maria Souza", email);
		return id;
	}

	public UUID inserirVeiculo(UUID clienteId) {
		var id = UUID.randomUUID();
		executar("insert into veiculo (id, cliente_id, placa, tipo, marca, modelo, ano) values (?, ?, ?, ?, ?, ?, ?)",
				id, clienteId, ApiTest.placaNova(), "MOTO", "Honda", "CG 160", 2022);
		return id;
	}

	/**
	 * o ADMIN, com o qual os testes entram, e os usuários de demonstração ficam.
	 */
	public static void limpar() {
		exigirBancoDeTeste();
		executar("delete from orcamento_item_peca");
		executar("delete from orcamento_item_servico");
		executar("delete from orcamento");
		executar("delete from ordem_servico");
		executar("delete from veiculo");
		executar("delete from usuario_papel where usuario_id in (select id from usuario where " + USUARIOS_DOS_TESTES + ")");
		executar("delete from usuario where " + USUARIOS_DOS_TESTES);
		executar("delete from cliente");
		executar("delete from servico");
		executar("delete from tipo_servico");
		executar("delete from item_estoque");
	}

	// A limpeza apaga as tabelas inteiras: só roda num banco cujo nome termina em api_test, como o torque_api_test.
	private static void exigirBancoDeTeste() {
		var url = ApiTest.variavel("DB_URL");
		var banco = url.substring(url.lastIndexOf('/') + 1).split("\\?")[0];
		if (!banco.endsWith("api_test")) {
			throw new IllegalStateException("A limpeza só roda num banco de teste (nome terminado em api_test), e não em " + banco);
		}
	}

	protected static void executar(String sql, Object... valores) {
		try (var conexao = DriverManager.getConnection(ApiTest.variavel("DB_URL"),
				ApiTest.variavel("DB_USER"), ApiTest.variavel("DB_PASSWORD"));
				var comando = conexao.prepareStatement(sql)) {
			for (int i = 0; i < valores.length; i++) {
				comando.setObject(i + 1, valores[i]);
			}
			comando.executeUpdate();
		} catch (SQLException e) {
			throw new IllegalStateException("Não foi possível gravar no banco dos testes: " + sql, e);
		}
	}

}
