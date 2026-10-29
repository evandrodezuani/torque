create table item_estoque (
    id        uuid not null,
    tipo      varchar(20) not null,
    descricao varchar(100) not null,
    preco     numeric(10, 2) not null,
    saldo     integer not null,
    reservado integer not null,

    constraint pk_item_estoque primary key (id),
    constraint uk_item_estoque_descricao unique (descricao)
);
