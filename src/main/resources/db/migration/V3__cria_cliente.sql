create table cliente (
    id        uuid not null,
    documento varchar(14) not null,
    nome      varchar(150) not null,
    email     varchar(254) not null,
    telefone  varchar(20),

    constraint pk_cliente primary key (id),
    constraint uk_cliente_documento unique (documento)
);

alter table ordem_servico
    add constraint fk_ordem_servico_cliente foreign key (cliente_id) references cliente (id);
