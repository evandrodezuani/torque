alter table veiculo add column tipo varchar(20) default 'CARRO' not null;

alter table veiculo alter column tipo drop default;
