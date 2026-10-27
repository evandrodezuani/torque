# ADR 003 - Contextos referenciados por id no modelo e por chave estrangeira no banco

Data: 2026-10-26
Situação: aceita

## Contexto

Os contextos Atendimento e Cadastro se relacionam: uma ordem de serviço pertence a um cliente e a um veículo.
um agregado referencia outro só pelo id


## Decisão

- Os agregados de contextos diferentes se referenciam só pelo id.
- No banco, toda coluna que aponta para outra tabela tem chave estrangeira, mesmo entre contextos.


## Consequências

- O banco garante que toda OS aponta para um cliente que existe, mesmo se algum caminho do código esquecer
  de conferir.
