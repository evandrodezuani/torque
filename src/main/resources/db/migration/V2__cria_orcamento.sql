create table orcamento (
    id               uuid not null,
    ordem_servico_id uuid not null,
    numero           integer not null,
    situacao         varchar(20) not null,
    enviado_em       timestamp with time zone,
    respondido_em    timestamp with time zone,

    constraint pk_orcamento primary key (id),
    constraint fk_orcamento_ordem_servico foreign key (ordem_servico_id) references ordem_servico (id),
    constraint uk_orcamento_numero unique (ordem_servico_id, numero)
);