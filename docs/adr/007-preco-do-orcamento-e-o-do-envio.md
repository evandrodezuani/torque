# ADR 007 - Preço do orçamento é o do catálogo no envio

Data: 2026-10-30
Situação: aceita

## Contexto

O item do orçamento guarda o preço do serviço ou da peça, para que o valor que o cliente recebeu não
mude se o catálogo mudar depois.


Alternativas consideradas:

- Manter o preço da inclusão e o mecânico remover e incluir de novo a linha para pegar o preço novo.

## Decisão

Ao concluir o diagnóstico, antes de enviar, cada linha do orçamento recebe o preço vigente no catálogo,
para mais ou para menos. A descrição continua a da inclusão.

Depois do envio o preço fica congelado: é o que o cliente viu e o que vale na aprovação.

Se um serviço do orçamento foi inativado depois da inclusão, o envio é recusado, e o mecânico remove
a linha.

## Consequências

- O cliente sempre recebe o preço vigente no dia do envio.
- A revisão não precisa de regra própria: o orçamento seguinte é atualizado no próximo envio.
- O total que o mecânico vê durante o diagnóstico pode mudar no envio.
