create table orcamento_item_servico (
    orcamento_id    uuid not null,
    servico_id      uuid not null,
    descricao       varchar(100) not null,
    preco_unitario  numeric(10, 2) not null,
    quantidade      integer not null,

    constraint pk_orcamento_item_servico primary key (orcamento_id, servico_id),
    constraint fk_orcamento_item_servico_orcamento foreign key (orcamento_id) references orcamento (id),
    constraint fk_orcamento_item_servico_servico foreign key (servico_id) references servico (id)
);

create table orcamento_item_peca (
    orcamento_id    uuid not null,
    item_estoque_id uuid not null,
    descricao       varchar(100) not null,
    preco_unitario  numeric(10, 2) not null,
    quantidade      integer not null,

    constraint pk_orcamento_item_peca primary key (orcamento_id, item_estoque_id),
    constraint fk_orcamento_item_peca_orcamento foreign key (orcamento_id) references orcamento (id),
    constraint fk_orcamento_item_peca_item_estoque foreign key (item_estoque_id) references item_estoque (id)
);
