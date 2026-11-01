create table usuario (
    id                    uuid not null,
    login                 varchar(100) not null,
    senha                 varchar(100),
    ativo                 boolean not null,
    cliente_id            uuid,
    token_senha           uuid,
    token_senha_expira_em timestamp with time zone,

    constraint pk_usuario primary key (id),
    constraint uk_usuario_login unique (login),
    constraint uk_usuario_cliente unique (cliente_id),
    constraint uk_usuario_token_senha unique (token_senha),
    constraint fk_usuario_cliente foreign key (cliente_id) references cliente (id)
);

create table usuario_papel (
    usuario_id uuid not null,
    papel      varchar(20) not null,

    constraint pk_usuario_papel primary key (usuario_id, papel),
    constraint fk_usuario_papel_usuario foreign key (usuario_id) references usuario (id)
);
