# ADR 008 - Entrega sem execução antes do orçamento

Data: 2026-10-30
Situação: aceita

## Contexto

O veículo só podia ser entregue com a OS finalizada ou rejeitada. Se faltasse uma peça sem substituta,
ou se o cliente desistisse antes do orçamento, a OS ficava presa em Recebida ou Em diagnóstico, sem
como ser encerrada.

Alternativas consideradas:

- Rejeitar o orçamento automaticamente quando faltar estoque na conclusão do diagnóstico. A rejeição é
  uma decisão do cliente, e capturar a exceção impediria o rollback, deixando reservas presas.
- Não tratar e declarar a limitação no README.

## Decisão

O comando Entregar veículo passa a aceitar também a OS Recebida ou Em diagnóstico, como devolução sem
execução.

Continuam sem poder entregar a OS Aguardando aprovação, porque há estoque reservado para o orçamento (o
caminho é rejeitar, o que libera a reserva), e a OS Em execução, porque o estoque já foi baixado.

## Consequências

- Toda OS tem uma saída, e nenhuma reserva fica presa.
- A OS devolvida antes do orçamento fica com o orçamento em elaboração e sem tempo de execução, por isso
  não entra no tempo médio de execução.
