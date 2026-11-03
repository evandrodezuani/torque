# Torque

Tech Challenge FIAP, Software Architecture (SOAT).

## Objetivo do projeto

O Torque é o back-end de um sistema de gestão para oficinas mecânicas. Sistema Integrado de
Atendimento e Execução de Serviços, que permitirá aos clientes acompanhar
em tempo real o andamento do serviço, autorizar reparos adicionais via aplicativo
e garantir uma gestão interna eficiente e segura.

## Descrição resumida da solução

- **Cadastro**: clientes (CPF ou CNPJ, inclusive o CNPJ alfanumérico), veículos (placa antiga e Mercosul), tipos de serviço e o catálogo de serviços, com preço por tipo de veículo.
- **Estoque**: peças e insumos, com entrada de estoque. O saldo e a quantidade reservada ficam separados.
- **Atendimento**: a OS nasce no status Recebida e muda de status só pelas ações de negócio. Não existe endpoint para escolher o status:

  | Ação | Status depois |
  |---|---|
  | Abrir a OS | Recebida |
  | Iniciar o diagnóstico | Em diagnóstico |
  | Concluir o diagnóstico (gera e envia o orçamento) | Aguardando aprovação |
  | Aprovar o orçamento | Em execução |
  | Rejeitar o orçamento | **Rejeitada** |
  | Revisar o orçamento rejeitado | Em diagnóstico, com um orçamento novo |
  | Concluir o serviço | Finalizada |
  | Entregar o veículo | Entregue |

  
- **Orçamento e estoque**: o orçamento é calculado a partir dos serviços e das peças incluídos no diagnóstico, com o preço vigente no envio. Ao concluir o diagnóstico, as peças são **reservadas**, e o orçamento só é enviado se houver estoque. A aprovação transforma a reserva em baixa; a rejeição libera a reserva. A mudança da OS e a do estoque acontecem na mesma transação.
- **Cliente**: recebe o orçamento por e-mail, com os botões Aprovar e Rejeitar, e acompanha a OS pela API `/acompanhamento/{codigo}` sem login, por um código aleatório diferente do id. O cliente também tem um login próprio: a senha é criada por ele, pelo link do e-mail, e ele vê só as próprias OS.
- **Tempo médio de execução**: média do tempo entre a aprovação do orçamento e a conclusão do serviço, com filtros por tipo de veículo e por período.
- **Segurança**: autenticação JWT nas APIs administrativas, com papéis por rota.

## Arquitetura utilizada

Monólito em camadas no estilo **Onion** com utilização de Domain-Driven Design (DDD). O primeiro nível de pacotes são as camadas; dentro de cada uma, um pacote por **contexto delimitado** : `cadastro`, `atendimento` e `estoque`, mais o `shared`.

```
infrastructure  →  application  →  domain
(web, JPA, e-mail,    (casos de uso,    (agregados, objetos de valor,
 segurança)            transações)       regras de negócio)
```

- **domain**: o modelo, sem Spring nem JPA. Agregados `OrdemServico` (com o `Orcamento`), `ItemEstoque`, `Cliente`, `Veiculo`, `Servico` e `TipoServico`; objetos de valor como `Documento`, `Placa` e `Dinheiro`; as interfaces dos repositórios.
- **application**: os casos de uso (`OrdemServicoService`, `ClienteService`...), o controle de transação e as portas para o que é técnico, como o `Notificador` (o aviso ao cliente) e o `AcessoDoCliente` (o login do cliente).
- **infrastructure**: os controllers REST e as páginas do cliente, as entidades JPA e as implementações dos repositórios, o envio de e-mail e a segurança.


## Tecnologias utilizadas

| | |
|---|---|
| Linguagem e framework | Java 21, Spring Boot 4.1.1 (Web MVC, Data JPA, Validation, Security com OAuth2 Resource Server, Mail, Thymeleaf) |
| Banco de dados | PostgreSQL 17, com as migrações no Flyway |
| Documentação da API | springdoc-openapi (Swagger UI) |
| Validação de CPF e CNPJ | Caelum Stella |
| Testes | JUnit, Mockito, Spring MockMvc, Spring Security Test, H2 em memória |
| Testes de API | REST Assured (projeto `apitest`) |
| Contêineres | Docker e Docker Compose |
| E-mail local | Mailpit (servidor SMTP de testes, com caixa de entrada na web) |
| Integração contínua e qualidade | Jenkins e SonarQube Community |

## Pré-requisitos

- **Para rodar com o Docker**: Docker Desktop com o Docker Compose. Não é preciso ter Java nem Maven: a imagem compila o projeto.
- **Para rodar ou testar sem o Docker**: JDK 21 e um PostgreSQL. O Maven não é preciso, porque o projeto tem o Maven Wrapper (`mvnw`).
- **Para o SonarQube e o Jenkins** (perfil `ci`): cerca de 4 GB de memória livres para o Docker.

## Instruções para execução

### Com o Docker

1. Crie o arquivo `.env` a partir do modelo e preencha os valores:

   ```bash
   cp .env.example .env        # no Windows: copy .env.example .env
   ```

   | Variável | Para que serve |
   |---|---|
   | `DB_USER`, `DB_PASSWORD` | usuário e senha do PostgreSQL. O banco é criado com eles, e a aplicação os usa para se conectar |
   | `JWT_SECRET` | segredo que assina os tokens. **Mínimo de 32 caracteres**, senão a aplicação não sobe |
   | `TORQUE_ADMIN_EMAIL`, `TORQUE_ADMIN_SENHA` | o primeiro ADMIN, criado na subida (veja [Usuários e autenticação](#usuários-credenciais-e-autenticação)) |
   | `TORQUE_DEMO_SENHA` | senha dos usuários de demonstração, um por papel da oficina (veja [Usuários e autenticação](#usuários-credenciais-e-autenticação)); vazia, eles não são criados |
   | `SONAR_DB_USER`, `SONAR_DB_PASSWORD`, `SONAR_TOKEN`, `JENKINS_ADMIN_USER`, `JENKINS_ADMIN_SENHA` | só para o perfil `ci` |

   Se faltar uma variável obrigatória, o Docker Compose para e diz qual é. O `.env` não vai para o Git.

2. Suba a aplicação, o banco e o servidor de e-mail:

   ```bash
   docker compose up --build
   ```

   O primeiro build demora alguns minutos, porque baixa as dependências. As migrações do banco rodam sozinhas na subida.

| Serviço | Endereço |
|---|---|
| API | http://localhost:8080 |
| Swagger | http://localhost:8080/swagger-ui.html |
| Caixa de entrada do Mailpit (os e-mails enviados aos clientes) | http://localhost:8025 |
| PostgreSQL | `localhost:5433` (a porta 5433, para não conflitar com um PostgreSQL local na 5432) |

Para parar: `docker compose down`. Para parar e **apagar os dados**: `docker compose down -v`.

### Com o SonarQube e o Jenkins (perfil `ci`)

No Windows com o WSL, o SonarQube exige um ajuste de memória do Elasticsearch, que precisa ser feito de novo a cada reinício da máquina:

```powershell
wsl -d docker-desktop sysctl -w vm.max_map_count=262144
```

```bash
docker compose --profile ci up --build
```

| Serviço | Endereço | Acesso |
|---|---|---|
| SonarQube | http://localhost:9000 | `admin` / `admin` no primeiro acesso; o SonarQube pede para trocar a senha |
| Jenkins | http://localhost:8090 | `JENKINS_ADMIN_USER` / `JENKINS_ADMIN_SENHA` do `.env` |

O Jenkins já sobe configurado (pasta [`ci/jenkins`](ci/jenkins)): os plugins, o usuário e o job `torque`, que roda o [`Jenkinsfile`](Jenkinsfile) da branch `main` do GitHub. O pipeline tem quatro etapas:

1. **Build e testes**: os testes unitários e de integração, com o relatório de cobertura do JaCoCo. O build falha abaixo de 80%.
2. **Testes de API**: sobe o jar que acabou de compilar, na porta 8081 e no banco `torque_api_test`, e roda o projeto `apitest` contra ele.
3. **Análise no SonarQube**.
4. **Quality Gate**: espera o resultado do SonarQube e falha o build se o Quality Gate falhar.

Para as etapas 3 e 4 funcionarem, uma vez só:

1. No SonarQube, gere um token do tipo **Global Analysis Token** em **My Account → Security**. O projeto `torque` é criado sozinho na primeira análise.
2. Coloque o token no `SONAR_TOKEN` do `.env` e recrie o Jenkins: `docker compose --profile ci up -d jenkins`.
3. No SonarQube, em **Administration → Configuration → Webhooks**, crie um webhook com a URL `http://jenkins:8080/sonarqube-webhook/`. É por ele que o Jenkins fica sabendo do resultado do Quality Gate. O SonarQube bloqueia webhooks para endereços de rede interna, como o do Jenkins no Docker, para se proteger de SSRF; por isso o `docker-compose.yml` sobe o SonarQube com `-Dsonar.validateWebhooks=false`, um risco aceito porque o SonarQube roda só localmente.

### Sem o Docker

Crie um banco no PostgreSQL e rode a aplicação com as variáveis de ambiente:

```bash
DB_URL=jdbc:postgresql://localhost:5432/torque DB_USER=... DB_PASSWORD=... \
JWT_SECRET=... TORQUE_ADMIN_EMAIL=... TORQUE_ADMIN_SENHA=... \
./mvnw spring-boot:run
```

Sem um servidor SMTP na porta 1025, a aplicação funciona normalmente: o envio de e-mail falha e só fica registrado no log. Para receber os e-mails, suba só o Mailpit: `docker compose up smtp`.

## Instruções para execução dos testes

```bash
./mvnw verify        # no Windows: mvnw.cmd verify
```

São testes unitários (domínio e casos de uso) e de integração (controllers com MockMvc, repositórios e transações). Eles usam o H2 em memória no modo PostgreSQL ([`src/test/resources/application.properties`](src/test/resources/application.properties)), então não precisam de banco nem de Docker. Os testes de integração fazem cada requisição com o papel real da ação, e o `PermissoesTest` confere cada linha da matriz de papéis: 403 para quem não tem o papel e 401 sem token.

**Cobertura.** O `verify` gera o relatório do JaCoCo em `target/site/jacoco/index.html` e **falha o build se a cobertura de linhas ficar abaixo de 80%**. Ficam fora da conta a classe com o `main`, as entidades JPA e os DTOs da API (`*Request`, `*Response`), que não têm regra de negócio. O SonarQube usa as mesmas exclusões. Só os testes, sem o relatório: `./mvnw test`.

**Testes de API.** Ficam no projeto [`apitest`](apitest) (REST Assured) e rodam contra a aplicação de pé, com o login do ADMIN. Cada recurso tem três classes: o `Populate` grava direto no banco o que o teste precisa que já exista, o `API` faz as chamadas aos endpoints e o `Test` tem os testes. Os testes leem **as mesmas variáveis de ambiente da aplicação** que estão testando: `SERVER_PORT` (o endereço é `http://localhost:SERVER_PORT`, 8081 se faltar), `TORQUE_ADMIN_EMAIL` e `TORQUE_ADMIN_SENHA` (o login) e `DB_URL`, `DB_USER` e `DB_PASSWORD` (o banco). Os valores vêm das variáveis de ambiente ou, se não houver, do `apitest/src/test/resources/application.properties`, que fica fora do Git: copie o [`application.properties.example`](apitest/src/test/resources/application.properties.example) e preencha com os mesmos valores da aplicação. O banco precisa ser separado e ter o nome terminado em `api_test`, como o `torque_api_test`: os testes gravam dados e, no começo e no fim de cada classe, esvaziam as tabelas do negócio.

```bash
SERVER_PORT=8081 DB_URL=jdbc:postgresql://localhost:5432/torque_api_test DB_USER=<usuário> DB_PASSWORD=<senha> \
TORQUE_ADMIN_EMAIL=<e-mail do ADMIN> TORQUE_ADMIN_SENHA=<senha do ADMIN> \
./mvnw -f apitest/pom.xml test
```

No Jenkins, essa é a etapa **Testes de API** do pipeline, que sobe a aplicação e roda os testes sozinha.

## Instruções para acesso ao Swagger ou documentação da API

Com a aplicação de pé:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI em JSON**: http://localhost:8080/v3/api-docs

O Swagger não pede login. Para chamar as rotas protegidas:

1. Chame o `POST /auth/login` com o e-mail e a senha.
2. Copie o `token` da resposta.
3. Clique em **Authorize** e cole o token, sem a palavra "Bearer".

Os erros de validação, de autenticação e de negócio seguem o formato **ProblemDetail** (RFC 9457), com a mensagem no campo `detail`: 400 para um campo inválido, 401 sem token ou com token inválido, 403 sem o papel, 404 para o que não existe e 422 para uma regra de negócio violada.

## Decisão e justificativa do banco de dados escolhido

**PostgreSQL**, um banco relacional, porque o problema é relacional e depende de consistência:

- **Transações ACID nos fluxos principais.** Concluir o diagnóstico muda a OS e reserva o estoque; aprovar muda a OS e baixa o estoque. Ou as duas coisas acontecem, ou nenhuma. Um banco relacional garante isso numa transação só.
- **Integridade garantida pelo banco**, e não só pelo código. São chaves estrangeiras entre a OS, o cliente, o veículo e os itens do orçamento, e restrições de unicidade (documento e e-mail do cliente, placa, o código de acompanhamento da OS, o login). Mesmo duas requisições ao mesmo tempo não gravam um cliente duplicado.
- **Consultas de agregação no próprio banco.** O tempo médio de execução é uma média sobre a diferença entre datas, calculada pelo PostgreSQL, com os filtros de tipo de veículo e de período.
- **Ferramentas e custo.** É livre e gratuito, tem imagem oficial no Docker e é suportado pelo Flyway e pelo Hibernate. O mesmo servidor também serve o SonarQube.

O esquema é criado e versionado pelas migrações do Flyway ([`src/main/resources/db/migration`](src/main/resources/db/migration)), e o Hibernate só **valida** o esquema (`ddl-auto=validate`), sem alterá-lo.

## Usuários, credenciais e autenticação

O login é sempre pelo **e-mail**: `POST /auth/login` com `{ "email": "...", "senha": "..." }`. A resposta traz um token JWT válido por **2 horas**, que vai no cabeçalho `Authorization: Bearer <token>`.

**O primeiro acesso.** Na subida, se não existe nenhum ADMIN ativo, o Torque cria um com o `TORQUE_ADMIN_EMAIL` e a `TORQUE_ADMIN_SENHA` do `.env`. Nenhuma senha fica no repositório. Para recuperar o acesso do ADMIN, desative-o no banco e suba a aplicação de novo com uma senha nova no `.env`.

**Os funcionários.** O ADMIN cadastra os funcionários em `POST /usuarios`, com o e-mail, a senha e os papéis. O funcionário que sai da oficina é desativado, sem ser apagado.

| Papel | O que faz |
|---|---|
| ADMIN | tudo, inclusive gerenciar os usuários (`/usuarios`) |
| GERENTE | toda a operação da oficina, menos os usuários; é quem mantém os serviços e os preços e vê o tempo médio |
| ATENDENTE | cadastra clientes e veículos, abre a OS, registra a resposta do cliente ao orçamento e entrega o veículo |
| MECANICO | faz o diagnóstico, inclui serviços e peças, conclui o diagnóstico e o serviço e revisa o orçamento rejeitado |
| ALMOXARIFE | cadastra os itens de estoque e registra as entradas |
| CLIENTE | vê as próprias OS e aprova ou rejeita os orçamentos delas |

Um usuário pode ter mais de um papel. A matriz completa, por rota e método HTTP, está em [`SegurancaConfig`](src/main/java/br/com/autosoft/torque/infrastructure/shared/security/SegurancaConfig.java).

**Os clientes.** No cadastro do cliente, ele recebe um e-mail com um link, válido por 24 horas e de uso único, para **criar a própria senha**: ninguém da oficina conhece a senha do cliente. Se o link vencer, ou se ele esquecer a senha, ele pede um link novo pelo `POST /auth/clientes/novo-link` ou pela página do link. Logado, ele vê só as próprias OS; a OS de outro cliente responde 404. Sem login, o cliente também acompanha a OS e responde ao orçamento pelo código de acompanhamento (`/acompanhamento/{codigo}`) e pelos botões do e-mail.

**Os usuários de demonstração.** A cada subida, o Torque deixa pronto um usuário de cada papel da oficina, todos com a senha do `TORQUE_DEMO_SENHA` do `.env`:

| E-mail | Papel |
|---|---|
| `gerente@torque.local` | GERENTE |
| `atendente@torque.local` | ATENDENTE |
| `mecanico@torque.local` | MECANICO |
| `almoxarife@torque.local` | ALMOXARIFE |

Se um deles for alterado ou desativado pelo `/usuarios`, ele volta ao padrão na próxima subida: ativo, com essa senha e só com o próprio papel. Com o `TORQUE_DEMO_SENHA` vazio, eles não são criados.

**Para uma demonstração**: suba o projeto e entre com cada usuário para ver o que cada papel pode fazer; uma ação fora do papel responde 403. Cadastre um cliente e abra o e-mail dele no Mailpit (http://localhost:8025) para criar a senha do cliente.

## Estrutura dos principais diretórios do projeto

```
torque/
├── src/main/java/br/com/autosoft/torque/
│   ├── domain/            o modelo: agregados, objetos de valor e interfaces dos repositórios
│   │   ├── atendimento/   OrdemServico, Orcamento, itens do orçamento, status
│   │   ├── cadastro/      Cliente, Veiculo, Servico, TipoServico, Documento, Placa
│   │   ├── estoque/       ItemEstoque
│   │   └── shared/        Dinheiro e as exceções de negócio
│   ├── application/       os casos de uso e as transações, um pacote por contexto
│   └── infrastructure/
│       ├── atendimento/   API e páginas do cliente, persistência, envio de e-mail
│       ├── cadastro/      API e persistência
│       ├── estoque/       API e persistência
│       └── shared/        segurança (JWT, usuários, papéis), tratamento de erros, OpenAPI
├── src/main/resources/
│   ├── db/migration/      migrações do Flyway (V1 a V11)
│   ├── templates/         e-mails e páginas do cliente (Thymeleaf)
│   └── application.properties
├── src/test/              testes unitários e de integração
├── apitest/               testes de API com REST Assured (projeto Maven separado)
├── ci/jenkins/            imagem e configuração do Jenkins
├── docker/postgres/       script que cria os bancos extras na primeira subida
├── docs/
│   ├── adr/               registros das decisões de arquitetura
│   └── ddd/               documentação DDD
├── Dockerfile             imagem da aplicação, em duas etapas
├── docker-compose.yml     aplicação, PostgreSQL, Mailpit e, no perfil ci, SonarQube e Jenkins
├── Jenkinsfile            pipeline de integração contínua
└── .env.example           modelo das variáveis de ambiente
```

## Link para a documentação DDD

- Board com os Event Stormings, os contextos delimitados e os diagramas C4: _link público a publicar_
- No repositório, em [`docs/ddd`](docs/ddd): os Event Stormings, os diagramas e a [linguagem ubíqua](docs/ddd/linguagem-ubiqua.md), que inclui a relação entre a documentação e o código

## Link para o vídeo da entrega

[Link Fase 1 ](https://drive.google.com/file/d/1HlrhOkTa6Uwf9O8wPdXMDn4oo07_-Gz_/view?usp=sharing)

## Limitações conhecidas

- **Sem refresh token**: depois de 2 horas é preciso entrar de novo. Como o token é conferido só pela assinatura, um funcionário desativado ou um cliente removido continua com acesso até o token que já tem vencer, por até 2 horas.
- **A reserva de estoque não expira**: ela só é liberada quando a rejeição do orçamento é registrada.
- **O tempo médio é por OS, e não por serviço**, porque o Torque registra o início e o fim da OS inteira. Ele conta da aprovação do orçamento, que inicia a execução, até a conclusão do serviço.
- **Usuários de demonstração com senha conhecida**: os quatro usuários de demonstração são recriados a cada subida, com a mesma senha do `.env`. 