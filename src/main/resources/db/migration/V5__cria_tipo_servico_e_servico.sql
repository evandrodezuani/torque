create table tipo_servico (
    id        uuid not null,
    descricao varchar(100) not null,

    constraint pk_tipo_servico primary key (id),
    constraint uk_tipo_servico_descricao unique (descricao)
);

create table servico (
    id              uuid not null,
    tipo_servico_id uuid not null,
    tipo_veiculo    varchar(20) not null,
    preco           numeric(10, 2) not null,
    ativo           boolean not null,

    constraint pk_servico primary key (id),
    constraint fk_servico_tipo_servico foreign key (tipo_servico_id) references tipo_servico (id),
    constraint uk_servico_tipo_servico_tipo_veiculo unique (tipo_servico_id, tipo_veiculo)
);
