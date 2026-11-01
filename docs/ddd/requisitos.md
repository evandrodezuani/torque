# Requisitos

Requisitos funcionais e não funcionais do Torque, levantados a partir do Event Storming 

## Requisitos funcionais

### Cadastro

| Código | Requisito | Regras |
|---|---|---|
| RF01 | Cadastrar, consultar, listar, alterar e remover clientes | O cliente é identificado pelo CPF ou CNPJ, inclusive o CNPJ alfanumérico, validado no cadastro. Nome, documento e e-mail são obrigatórios; o telefone é opcional. O documento e o e-mail não se repetem, e o documento não pode ser alterado. |
| RF02 | Remover o cliente com o histórico | Remover o cliente apaga as OS entregues, os orçamentos e os veículos dele. Um cliente com OS em andamento não pode ser removido. |
| RF03 | Cadastrar, consultar, alterar e remover veículos de um cliente | O veículo tem placa, tipo (Carro, Moto ou Caminhão), marca, modelo e ano. A placa aceita o formato antigo e o Mercosul, não se repete e não pode ser alterada, assim como o tipo e o dono. O ano vai de 1900 até o ano seguinte ao atual. Um cliente pode ter mais de um veículo. Um veículo com OS em andamento não pode ser removido. |
| RF04 | Manter os tipos de serviço | O tipo de serviço é o nome de um trabalho, como troca de óleo, e não se repete. Um tipo de serviço com serviços cadastrados não pode ser removido. |
| RF05 | Manter o catálogo de serviços | O serviço é um tipo de serviço para um tipo de veículo, com preço maior que zero, ativo ou inativo. Só existe um serviço por tipo de serviço e tipo de veículo, e esses dois não podem ser alterados. Um serviço já usado em orçamento não pode ser removido, só inativado. |

### Estoque

| Código | Requisito | Regras |
|---|---|---|
| RF06 | Cadastrar, consultar, listar, alterar e remover peças e insumos | O item de estoque tem tipo (peça ou insumo), descrição única e preço. O tipo não pode ser alterado. Um item já usado em orçamento ou com quantidade reservada não pode ser removido. |
| RF07 | Registrar a entrada de estoque | A entrada aumenta o saldo do item. A quantidade deve ser maior que zero. |
| RF08 | Controlar o saldo e a reserva | O item guarda o saldo e a quantidade reservada. A disponibilidade é o saldo menos a reserva, e nenhum dos dois fica negativo. |

### Atendimento

| Código | Requisito | Regras |
|---|---|---|
| RF09 | Abrir a OS | A OS é aberta para um cliente e um veículo dele, no status Recebida, com o orçamento 1 em elaboração e um código de acompanhamento aleatório. |
| RF10 | Iniciar o diagnóstico | Só a partir de Recebida. A OS passa a Em diagnóstico. |
| RF11 | Incluir e remover serviços, peças e insumos no orçamento | Só com a OS em diagnóstico. O serviço precisa estar ativo e ser do tipo do veículo. Incluir de novo o mesmo item soma a quantidade na mesma linha. A remoção tira a linha inteira e não mexe no estoque. |
| RF12 | Concluir o diagnóstico e enviar o orçamento | O orçamento precisa de pelo menos um serviço ou peça. Os preços são atualizados com os do catálogo e do estoque, e depois não mudam mais. As peças e os insumos são reservados; se faltar disponibilidade ou um serviço estiver inativo, nada é gravado. A OS passa a Aguardando aprovação e o orçamento fica Pendente. |
| RF13 | Gerar o total do orçamento automaticamente | O total é a soma dos subtotais dos itens. |
| RF14 | Notificar o cliente | Ao concluir o diagnóstico, o cliente recebe um e-mail com o orçamento, os botões Aprovar e Rejeitar e um link para o portal do cliente. |
| RF15 | Registrar a aprovação do orçamento | Só com a OS aguardando aprovação. A reserva vira baixa de estoque e a OS passa a Em execução, que começa a contar o tempo de execução. |
| RF16 | Registrar a rejeição do orçamento | Só com a OS aguardando aprovação. A reserva é liberada e a OS passa a Rejeitada. |
| RF17 | Revisar o orçamento rejeitado | Cria o orçamento seguinte com os mesmos itens do anterior, que fica no histórico. A OS volta a Em diagnóstico. |
| RF18 | Concluir o serviço | Só com a OS em execução. A OS passa a Finalizada. |
| RF19 | Entregar o veículo | A partir de Finalizada, de Rejeitada, ou de Recebida e Em diagnóstico quando o cliente desiste antes do orçamento. A OS passa a Entregue. |
| RF20 | Mudar o status só pelas ações de negócio | Não existe endpoint para escolher o status. Uma ação fora do status certo é recusada. |
| RF21 | Listar e detalhar as OS | A listagem pode ser filtrada pelo status e é ordenada pela data de abertura, as mais recentes primeiro por padrão. |
| RF22 | Calcular o tempo médio de execução | Média, por OS, do tempo entre a aprovação do orçamento e a conclusão do serviço. Filtros opcionais por tipo de veículo e pelo período da conclusão. |

### Cliente

| Código | Requisito | Regras |
|---|---|---|
| RF23 | Acompanhar a OS sem login | Pelo código de acompanhamento, o cliente vê o status, o veículo e o orçamento atual, sem os ids internos. |
| RF24 | Responder ao orçamento sem login | Pelo código de acompanhamento, na API ou nas páginas do portal abertas pelo e-mail. |
| RF25 | Criar a própria senha | No cadastro, o cliente recebe por e-mail um link de uso único, válido por 24 horas. Se o link vencer ou ele esquecer a senha, pede um novo pelo documento ou pelo e-mail. |
| RF26 | Ver as próprias OS logado | O cliente logado lista e consulta só as OS dele e aprova ou rejeita o orçamento delas. A OS de outro cliente responde 404. |

### Acesso

| Código | Requisito | Regras |
|---|---|---|
| RF27 | Entrar no sistema | Login pelo e-mail e senha, para funcionários e clientes. Devolve um token de acesso. |
| RF28 | Gerenciar os funcionários | Só o Administrador cadastra, consulta e altera funcionários, com um ou mais papéis. O funcionário que sai é desativado, sem ser apagado. Sempre fica pelo menos um Administrador ativo. |
| RF29 | Controlar o acesso por papel | Cada rota e método HTTP tem os papéis que podem usá-la: Administrador, Gerente, Atendente, Mecânico, Almoxarife e Cliente. |
| RF30 | Criar o primeiro Administrador | Na subida, se não existe Administrador ativo, ele é criado a partir das variáveis de ambiente. |

## Requisitos não funcionais

| Código | Categoria | Requisito |
|---|---|---|
| RNF01 | Arquitetura | Monólito em camadas no estilo Onion, com DDD: `domain`, `application` e `infrastructure`, e dentro de cada camada um pacote por contexto delimitado (Cadastro, Atendimento e Estoque). O domínio não depende de Spring nem de JPA. |
| RNF02 | Interface | APIs RESTful em JSON, documentadas com OpenAPI e acessíveis pelo Swagger UI. |
| RNF03 | Erros | Respostas de erro no formato `ProblemDetail`: 400 para dados inválidos, 401 sem token válido, 403 sem permissão, 404 para recurso inexistente e 422 para regra de negócio quebrada. |
| RNF04 | Segurança | Autenticação JWT (HS256) nas APIs administrativas, com validade de 2 horas e sem refresh token. A chave vem da variável `JWT_SECRET`, com no mínimo 32 caracteres. |
| RNF05 | Segurança | Senhas guardadas com hash, com 8 a 72 caracteres. Nenhuma senha fica no repositório. Ninguém da oficina conhece a senha do cliente. |
| RNF06 | Segurança | Rotas públicas só para o login, o acompanhamento, o portal do cliente e o Swagger. Uma rota fora da matriz de papéis é negada para todos. |
| RNF07 | Consistência | Os fluxos principais são transacionais: concluir o diagnóstico muda a OS e reserva o estoque, e aprovar ou rejeitar muda a OS e o estoque, na mesma transação. |
| RNF08 | Consistência | O e-mail é enviado só depois do commit; uma falha no envio fica no log e não desfaz a operação. |
| RNF09 | Persistência | PostgreSQL 17, com o esquema criado e versionado pelo Flyway. O Hibernate só valida o esquema. A integridade também é garantida pelo banco, com chaves estrangeiras e restrições de unicidade. |
| RNF10 | Testes | Testes unitários e de integração com cobertura mínima de 80% de linhas, verificada pelo JaCoCo no build. Testes de API com REST Assured, num projeto separado e num banco próprio. |
| RNF11 | Qualidade | Pipeline no Jenkins com build, testes, análise no SonarQube com Quality Gate e testes de API. |
| RNF12 | Implantação | Dockerfile em duas etapas, com usuário sem root, e docker-compose com a aplicação, o PostgreSQL e o Mailpit. SonarQube e Jenkins no perfil `ci`. |
| RNF13 | Configuração | Configuração por variáveis de ambiente, com um `.env.example` no repositório e o `.env` fora do Git. |
| RNF14 | Versionamento | Branch `main` protegida, alterações por Pull Request, versão e changelog fechados pela action `Fechar versão`. |
