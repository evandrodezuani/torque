*[Glossário do domínio \- Oficina Mecânica](https://miro.com/app/board/uXjVEb8S0tE=/?moveToWidget=3458764689092289807&cot=14)*

| Termo | Definição |
| --- | --- |
| Ordem de Serviço \(OS\) | Registro do atendimento de um veículo na oficina, da chegada à entrega\. Reúne o diagnóstico, os orçamentos e o status do atendimento\. |
| Cliente | Pessoa ou empresa responsável pelo veículo, identificada pelo CPF ou CNPJ\. Aprova ou rejeita o orçamento, acompanha o andamento da OS e recebe o veículo ao final\. Pode fazer isso sem login, pelo código de acompanhamento, ou entrando com o próprio e\-mail e senha, quando vê só as suas OS\. |
| Veículo | Carro, moto ou caminhão do cliente que recebe o atendimento, com placa, tipo, marca, modelo e ano\. Um cliente pode ter mais de um veículo\. Toda OS se refere a um veículo\. |
| Diagnóstico | Avaliação do veículo feita pelo mecânico para identificar os serviços, as peças e os insumos necessários\. Acontece antes do envio do orçamento\. |
| Orçamento | Proposta de valor com os serviços, as peças e os insumos necessários, enviada ao cliente para aprovação\. Cada orçamento tem um número sequencial dentro da OS e os seus próprios itens\. Uma OS pode ter mais de um orçamento\. |
| Aprovação do orçamento | Decisão do cliente de aceitar o orçamento\. Inicia a execução e confirma a baixa do estoque reservado\. |
| Serviço | Tipo de serviço oferecido para um tipo de veículo, com preço\. Pode estar ativo ou inativo, e só um serviço ativo e do mesmo tipo do veículo pode entrar em uma OS\. Quando incluído em um orçamento, vira um item de serviço\. |
| Execução | Realização, pelo mecânico, dos serviços do orçamento aprovado\. Começa na aprovação do orçamento e termina quando o serviço é concluído\. |
| Rejeição do orçamento | Decisão do cliente de não aceitar o orçamento\. A OS passa ao status Rejeitada e a reserva de estoque é liberada\. |
| Serviço concluído | O trabalho no veículo terminou\. A OS passa ao status Finalizada\. |
| Revisão do orçamento | Criação de um novo orçamento a partir de um orçamento rejeitado\. O novo orçamento começa com os mesmos itens do anterior, e o orçamento rejeitado é mantido no histórico\. |
| Entrega | Devolução do veículo ao cliente\. Encerra o atendimento e leva a OS ao status Entregue\. Também acontece sem execução: quando o orçamento é rejeitado, e antes do orçamento, com a OS Recebida ou Em diagnóstico, quando o cliente desiste do atendimento\. |
| Status da OS | Etapa em que a OS se encontra\. O status nunca é alterado diretamente: ele muda como consequência de uma ação, como concluir o diagnóstico ou aprovar o orçamento\. Valores: Recebida, Em diagnóstico, Aguardando aprovação, Em execução, Rejeitada, Finalizada, Entregue\. |
| Notificação | Aviso por e\-mail ao cliente de que o orçamento está disponível, com os botões Aprovar e Rejeitar e um link para o portal do cliente\. É enviada quando o diagnóstico é concluído\. |
| Peça | Componente instalado no veículo em substituição a outro, como pastilha de freio ou filtro\. |
| Insumo | Material consumido durante o serviço que não é um componente do veículo, como óleo, graxa ou estopa\. |
| Estoque | Conjunto de peças e insumos que a oficina mantém disponíveis para uso nas ordens de serviço\. |
| Saldo | Quantidade de uma peça ou insumo existente no estoque em um dado momento\. |
| Disponibilidade | Saldo menos a quantidade reservada\. É o que pode ser usado em um novo orçamento\. |
| Entrada de estoque | Aumento do saldo de uma peça ou insumo pelo recebimento de mercadoria\. |
| Baixa de estoque | Redução definitiva do saldo de uma peça ou insumo pelo uso em uma OS\. Acontece quando o orçamento é aprovado\. |
| Estoque insuficiente | Situação em que a disponibilidade é menor que a quantidade necessária\. Impede o envio do orçamento\. |
| Atendente | Funcionário que recebe o cliente, cadastra o cliente e o veículo, abre a OS, registra a resposta do cliente ao orçamento quando ela chega por telefone ou no balcão e entrega o veículo\. |
| Mecânico | Funcionário que faz o diagnóstico, inclui e remove serviços, peças e insumos do orçamento, conclui o diagnóstico, revisa o orçamento rejeitado e executa os serviços\. |
| Almoxarife | Funcionário responsável pelo estoque\. Cadastra peças e insumos e registra as entradas\. |
| Sistema | O próprio software quando age automaticamente, por exemplo ao reservar o estoque ou ao mudar o status da OS\. |
| Item do orçamento | Linha do orçamento\. Pode ser um item de serviço ou um item de peça \(peça ou insumo\)\. Guarda a descrição, o preço unitário e a quantidade\. Incluir de novo o mesmo serviço ou a mesma peça soma a quantidade na mesma linha\. Pode ser removido enquanto o orçamento está em elaboração\. |
| Total do orçamento | Soma dos subtotais de todos os itens do orçamento\. É calculado automaticamente\. |
| Situação do orçamento | Etapa em que o orçamento se encontra\. Valores: Em elaboração, Pendente, Aprovado, Rejeitado\. Só o orçamento em elaboração aceita a inclusão de itens\. |
| Tempo de execução | Tempo entre a aprovação do orçamento e a conclusão do serviço de uma OS\. |
| Tempo médio de execução | Média do tempo de execução das ordens de serviço concluídas, que pode ser filtrada pelo tipo de veículo e pelo período da conclusão\. A média é por OS, e não por serviço\. |
| Item de estoque | Peça ou insumo controlado no estoque, com saldo e quantidade reservada\. |
| Reserva | Quantidade do saldo separada para um orçamento enviado ao cliente\. É feita quando o diagnóstico é concluído\. |
| Liberação de reserva | Devolução da quantidade reservada à disponibilidade\. Acontece quando o orçamento é rejeitado\. |
| CPF/CNPJ | Documento que identifica o cliente\. É validado no cadastro\. |
| Placa | Identificação do veículo, no formato antigo \(ABC\-1234\) ou no Mercosul \(ABC1D23\)\. É validada no cadastro e não pode ser alterada\. |
| Tipo de veículo | Categoria do veículo: Carro, Moto ou Caminhão\. É informado no cadastro do veículo e não pode ser alterado\. Define quais serviços podem ser usados na OS e o preço deles\. |
| Tipo de serviço | Nome de um trabalho de mão de obra que a oficina faz, como troca de óleo ou alinhamento\. É cadastrado uma vez e vale para todos os tipos de veículo\. |
| Gerente | Funcionário responsável pelos serviços, pelos preços e pela ativação de serviço\. Faz toda a operação da oficina e é quem acompanha o tempo médio de execução\. Não gerencia os usuários\. |
| Administrador | Responsável pelos usuários da oficina: cadastra os funcionários, define os papéis deles e os desativa\. Também pode fazer toda a operação\. |
| Preço do orçamento | O preço de cada item é o vigente no envio do orçamento: ao concluir o diagnóstico, os preços são atualizados com os do catálogo e do estoque\. Depois do envio, o preço não muda mais\. |
| Código de acompanhamento | Código aleatório da OS, diferente do identificador interno, com o qual o cliente acompanha a OS e responde ao orçamento sem login\. |
| Acompanhamento | Consulta do andamento da OS pelo cliente: o status, o veículo e o orçamento atual\. |
| Portal do cliente | Páginas abertas pelo e\-mail, em que o cliente vê o orçamento e o aprova ou rejeita\. |
| Usuário | Quem entra no sistema com e\-mail e senha: um funcionário da oficina ou um cliente\. O funcionário desativado não entra mais\. |
| Papel | O que o usuário pode fazer: Administrador, Gerente, Atendente, Mecânico, Almoxarife ou Cliente\. Um funcionário pode ter mais de um papel\. |
| Login | Entrada no sistema com o e\-mail e a senha\. Devolve o token de acesso\. |
| Token de acesso | Credencial que o usuário recebe no login e apresenta em cada requisição\. Vale por duas horas\. |
| Link de criação de senha | Link enviado por e\-mail ao cliente quando ele é cadastrado, para que ele mesmo crie a senha\. Vale por 24 horas e uma única vez; o cliente pode pedir um novo\. Ninguém da oficina conhece a senha do cliente\. |