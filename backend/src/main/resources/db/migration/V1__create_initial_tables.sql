-- V1: Criação das tabelas iniciais do sistema de Gestão de Obras

CREATE TABLE obras (
    id          BIGSERIAL PRIMARY KEY,
    nome        VARCHAR(150)   NOT NULL,
    descricao   TEXT,
    endereco    VARCHAR(255),
    status      VARCHAR(50)    NOT NULL DEFAULT 'EM_ANDAMENTO',
    data_inicio DATE           NOT NULL,
    data_fim    DATE,
    criado_em   TIMESTAMP      NOT NULL DEFAULT NOW(),
    atualizado_em TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE financeiro (
    id          BIGSERIAL PRIMARY KEY,
    obra_id     BIGINT         NOT NULL REFERENCES obras(id) ON DELETE CASCADE,
    tipo        VARCHAR(20)    NOT NULL CHECK (tipo IN ('RECEITA', 'DESPESA')),
    descricao   VARCHAR(255)   NOT NULL,
    valor       NUMERIC(15, 2) NOT NULL,
    data        DATE           NOT NULL,
    criado_em   TIMESTAMP      NOT NULL DEFAULT NOW()
);
