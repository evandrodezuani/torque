# ADR 002 - Domínio separado das entidades JPA

Data: 2026-10-26
Situação: aceita

## Contexto

O projeto segue a arquitetura Onion: o domínio fica no centro e não depende de framework nem de banco.
As classes do domínio (`OrdemServico`, `Orcamento`, `Cliente`) guardam as regras do negócio, e só mudam de
estado por métodos com nome de negócio, como `iniciarDiagnostico()` ou `alterar(...)`.

## Decisão

Cada agregado tem duas classes:

- a classe de domínio, sem nenhuma anotação de banco;
- a entidade JPA, que espelha a tabela.

O domínio declara o repositório como interface. A implementação usa um repositório do Spring Data por dentro e converte nos dois
sentidos:

- `paraEntity`: do domínio para a entidade, ao salvar;
- `paraDominio`: da entidade para o domínio, ao ler.

Um objeto lido do banco é montado pelo construtor público da classe de domínio, com o id que já existe e
sem reaplicar as regras de criação. Criar um objeto novo continua sendo papel dos métodos de negócio, que geram o id e validam os dados.


## Consequências

- O domínio não depende de banco e é testado sem Spring, em milissegundos.
- O banco pode mudar de formato (nomes de coluna, tabelas auxiliares) sem mexer nas regras.