alter table ordem_servico add column codigo_acompanhamento uuid not null;
alter table ordem_servico add constraint uk_ordem_servico_codigo_acompanhamento unique (codigo_acompanhamento);
