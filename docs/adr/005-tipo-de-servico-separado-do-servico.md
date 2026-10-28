# ADR 005 - Tipo de serviço separado do serviço

Data: 2026-10-28
Situação: aceita

## Contexto

Um mesmo trabalho tem preços diferentes conforme o tipo de veículo: a troca de óleo de uma moto não custa o mesmo que a de um carro. O catálogo não pode ter o
mesmo trabalho repetido para o mesmo tipo de veículo.

Alternativas consideradas:

- Uma tabela só, com descrição livre, tipo de veículo e preço.

## Decisão

O catálogo tem duas partes, cada uma com o seu CRUD:

- **Tipo de serviço** : o nome do trabalho, como "Troca de óleo". É cadastrado uma vez.
- **Serviço** : um tipo de serviço oferecido para um tipo de veículo (`CARRO`, `MOTO` ou `CAMINHAO`),
  com preço e a indicação de ativo ou inativo.

O serviço referencia o tipo de serviço pelo id, com chave estrangeira. A combinação de tipo de serviço
e tipo de veículo é única

## Consequências

- Não há erro de digitação: o serviço escolhe um tipo de serviço já cadastrado.
- São dois CRUDs no lugar de um.
