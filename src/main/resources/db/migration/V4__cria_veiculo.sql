create table veiculo (
    id         uuid not null,
    cliente_id uuid not null,
    placa      varchar(7) not null,
    marca      varchar(60) not null,
    modelo     varchar(60) not null,
    ano        integer not null,

    constraint pk_veiculo primary key (id),
    constraint uk_veiculo_placa unique (placa),
    constraint fk_veiculo_cliente foreign key (cliente_id) references cliente (id)
);

create index ix_veiculo_cliente on veiculo (cliente_id);

alter table ordem_servico
    add constraint fk_ordem_servico_veiculo foreign key (veiculo_id) references veiculo (id);
