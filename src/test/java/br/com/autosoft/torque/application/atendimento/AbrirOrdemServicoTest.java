package br.com.autosoft.torque.application.atendimento;

import br.com.autosoft.torque.domain.atendimento.model.OrdemServico;
import br.com.autosoft.torque.domain.atendimento.model.StatusOS;
import br.com.autosoft.torque.domain.atendimento.repository.OrdemServicoRepository;
import br.com.autosoft.torque.domain.shared.RegraDeNegocioException;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class AbrirOrdemServicoTest {

    private final OrdemServicoRepository repository = mock(OrdemServicoRepository.class);
    private final AbrirOrdemServico abrirOrdemServico = new AbrirOrdemServico(repository);

    @Test
    void abreAOrdemDeServicoESalva() {
        var ordemServico = abrirOrdemServico.executar(UUID.randomUUID(), UUID.randomUUID());

        assertThat(ordemServico.getStatus()).isEqualTo(StatusOS.RECEBIDA);
        verify(repository).salvar(ordemServico);
    }

    @Test
    void naoSalvaQuandoFaltaOCliente() {
        assertThatThrownBy(() -> abrirOrdemServico.executar(null, UUID.randomUUID()))
                .isInstanceOf(RegraDeNegocioException.class);

        verify(repository, never()).salvar(any());
    }

}