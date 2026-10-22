package br.com.autosoft.torque.application.atendimento;

import java.time.Instant;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AbrirOrdemServico {

	private final OrdemServicoRepository repository;

	@Transactional
	public OrdemServico executar(UUID clienteId, UUID veiculoId) {
		var ordemServico = OrdemServico.abrir(clienteId, veiculoId, Instant.now());
		repository.salvar(ordemServico);
		return ordemServico;
	}

}
