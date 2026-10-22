create table ordem_servico (
    id                    uuid not null,
    cliente_id            uuid not null,
    veiculo_id            uuid not null,
    status                varchar(30) not null,
    aberta_em             timestamp with time zone not null,
    execucao_iniciada_em  timestamp with time zone,
    execucao_concluida_em timestamp with time zone,
    entregue_em           timestamp with time zone,

    constraint pk_ordem_servico primary key (id)
);

