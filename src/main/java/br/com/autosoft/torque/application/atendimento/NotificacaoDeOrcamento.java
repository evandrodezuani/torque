package br.com.autosoft.torque.application.atendimento;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import br.com.autosoft.torque.domain.cadastro.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Avisa o cliente depois do commit: se a conclusão do diagnóstico for desfeita, nada e avisado
 */
@Slf4j
@Component
@RequiredArgsConstructor
class NotificacaoDeOrcamento {

	private final OrdemServicoService ordemServicoService;
	private final ClienteRepository clienteRepository;
	private final Notificador notificador;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void orcamentoEnviado(OrcamentoEnviado evento) {
		try {
			var acompanhamento = ordemServicoService.acompanhar(evento.codigoAcompanhamento());
			var cliente = clienteRepository.buscarPorId(acompanhamento.ordemServico().getClienteId()).orElseThrow();
			notificador.orcamentoDisponivel(cliente, acompanhamento);
		} catch (RuntimeException e) {
			log.warn("Não foi possível avisar o cliente do orçamento {}", evento.codigoAcompanhamento(), e);
		}
	}

}
