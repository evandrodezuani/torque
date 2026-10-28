# ADR 004 - Remover cliente ou veículo apaga o histórico

Data: 2026-10-27
Situação: aceita

## Contexto

Um cliente pode ter veículos e ordens de serviço. As tabelas estão ligadas por chaves estrangeiras,
então remover um cliente exige decidir o que acontece com o que depende dele.

## Decisão

- Se o cliente tem alguma OS que não está Entregue, a remoção é recusada: o veículo ainda está na oficina.
- Se todas as OS estão Entregues, ou não há nenhuma, a remoção apaga, nesta ordem, as OS (com os orçamentos),
  os veículos e o cliente.

## Consequências

- Não há como desfazer uma remoção.
