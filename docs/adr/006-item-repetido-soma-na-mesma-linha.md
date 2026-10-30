# ADR 006 - Item repetido soma na mesma linha do orçamento

Data: 2026-10-29
Situação: aceita

## Contexto

Durante o diagnóstico o mecânico inclui serviços, peças e insumos no orçamento. O mesmo item pode ser
incluído mais de uma vez, pois o mecânico percebeu que precisa de mais unidades.

Alternativas consideradas:

- Recusar o serviço repetido e somar só a peça repetida.


## Decisão

A inclusão informa sempre a quantidade, para serviço e para peça. Se o item já está no orçamento, a
quantidade é somada na linha que já existe; o preço e a descrição continuam os da primeira inclusão.

## Consequências

- O orçamento tem uma linha por serviço e uma linha por peça ou insumo.
- A mesma regra vale para os dois tipos de item.
- Uma inclusão repetida por engano não dá erro: ela aumenta a quantidade, e o mecânico vê o orçamento
  atualizado na resposta.
