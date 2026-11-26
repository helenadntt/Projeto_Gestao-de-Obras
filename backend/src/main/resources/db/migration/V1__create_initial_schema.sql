-- =============================================================
-- Flyway Migration: V1__create_initial_schema.sql
-- Projeto  : Gestão de Obras API
-- Descrição: Criação das tabelas iniciais (obras, centros_custo e
--            lancamentos_financeiros), índices e seed de centros de custo.
-- =============================================================

-- 1. TABELA: obras
CREATE TABLE obras (
    id                   BIGSERIAL                PRIMARY KEY,
    nome                 VARCHAR(150)             NOT NULL,
    descricao            TEXT,
    cliente              VARCHAR(150),
    endereco             VARCHAR(255)             NOT NULL,
    responsavel          VARCHAR(100)             NOT NULL,
    orcamento_previsto   NUMERIC(15, 2)           NOT NULL DEFAULT 0.00,
    data_inicio          DATE                     NOT NULL,
    previsao_fim         DATE                     NOT NULL,
    data_conclusao       DATE,
    status               VARCHAR(30)              NOT NULL DEFAULT 'PLANEJAMENTO',
    progresso_percentual INTEGER                  NOT NULL DEFAULT 0,
    created_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_obras_status CHECK (
        status IN ('PLANEJAMENTO', 'EM_ANDAMENTO', 'PARALISADA', 'CONCLUIDA', 'CANCELADA')
    ),
    CONSTRAINT chk_obras_datas CHECK (previsao_fim >= data_inicio),
    CONSTRAINT chk_obras_data_conclusao CHECK (data_conclusao IS NULL OR data_conclusao >= data_inicio),
    CONSTRAINT chk_obras_orcamento CHECK (orcamento_previsto >= 0),
    CONSTRAINT chk_obras_progresso CHECK (progresso_percentual BETWEEN 0 AND 100)
);

-- 2. TABELA: centros_custo
CREATE TABLE centros_custo (
    id         BIGSERIAL                PRIMARY KEY,
    nome       VARCHAR(100)             NOT NULL UNIQUE,
    descricao  VARCHAR(255),
    ativo      BOOLEAN                  NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. TABELA: lancamentos_financeiros
CREATE TABLE lancamentos_financeiros (
    id               BIGSERIAL                PRIMARY KEY,
    obra_id          BIGINT                   NOT NULL,
    centro_custo_id  BIGINT,
    descricao        VARCHAR(200)             NOT NULL,
    tipo             VARCHAR(10)              NOT NULL,
    valor            NUMERIC(15, 2)           NOT NULL,
    data_competencia DATE                     NOT NULL,
    data_pagamento   DATE,
    status_pagamento VARCHAR(20)              NOT NULL DEFAULT 'PENDENTE',
    comprovante_url  VARCHAR(500),
    observacoes      TEXT,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_lancamentos_obra
        FOREIGN KEY (obra_id)
        REFERENCES obras (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_lancamentos_centro_custo
        FOREIGN KEY (centro_custo_id)
        REFERENCES centros_custo (id)
        ON DELETE SET NULL,

    CONSTRAINT chk_lancamento_tipo CHECK (tipo IN ('RECEITA', 'DESPESA')),
    CONSTRAINT chk_lancamento_status CHECK (status_pagamento IN ('PENDENTE', 'PAGO', 'CANCELADO')),
    CONSTRAINT chk_lancamento_valor CHECK (valor > 0)
);

-- 4. ÍNDICES
CREATE INDEX idx_obras_status ON obras (status);
CREATE INDEX idx_obras_cliente ON obras (cliente);
CREATE INDEX idx_obras_data_inicio ON obras (data_inicio);

CREATE INDEX idx_lancamentos_obra_id ON lancamentos_financeiros (obra_id);
CREATE INDEX idx_lancamentos_centro_custo_id ON lancamentos_financeiros (centro_custo_id);
CREATE INDEX idx_lancamentos_tipo ON lancamentos_financeiros (tipo);
CREATE INDEX idx_lancamentos_status_pagamento ON lancamentos_financeiros (status_pagamento);
CREATE INDEX idx_lancamentos_obra_competencia ON lancamentos_financeiros (obra_id, data_competencia DESC);

-- 5. SEED INICIAL: Centros de Custo padrão
INSERT INTO centros_custo (nome, descricao, ativo) VALUES
    ('Materiais de Construção', 'Insumos brutos, acabamentos, ferragens e concreto', TRUE),
    ('Mão de Obra', 'Salários, diárias, empreiteiros e encargos', TRUE),
    ('Equipamentos & Maquinário', 'Locação, frete e manutenção de máquinas e ferramentas', TRUE),
    ('Projetos & Licenças', 'Taxas cartorárias, alvarás, projetos de arquitetura e engenharia', TRUE),
    ('Serviços Terceirizados', 'Segurança, caçambas de entulho, limpeza de obra e instalações', TRUE),
    ('Aporte / Receita de Cliente', 'Medições aprovadas, parcelas e pagamentos de clientes', TRUE);
