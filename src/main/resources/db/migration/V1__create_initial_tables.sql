-- =============================================================
-- Flyway Migration: V1__create_initial_tables.sql
-- Projeto  : Gestão de Obras API
-- Descrição: Criação das tabelas iniciais do sistema.
-- =============================================================

-- ─────────────────────────────────────────────────────────────
-- TABELA: obras
-- Representa cada projeto/obra de construção civil.
-- ─────────────────────────────────────────────────────────────
CREATE TABLE obras (
    id                BIGSERIAL       PRIMARY KEY,
    nome              VARCHAR(150)    NOT NULL,
    cliente           VARCHAR(150),
    endereco          TEXT,
    orcamento_previsto NUMERIC(15, 2) NOT NULL,
    status            VARCHAR(30)     NOT NULL
                        CONSTRAINT ck_obras_status
                        CHECK (status IN ('PLANEJAMENTO', 'EM_ANDAMENTO', 'CONCLUIDA', 'PAUSADA')),
    data_inicio       DATE,
    data_previsao_fim DATE,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Regra de negócio: data de previsão de fim não pode ser anterior à data de início
    CONSTRAINT ck_obras_datas
        CHECK (data_previsao_fim IS NULL
            OR data_inicio IS NULL
            OR data_previsao_fim >= data_inicio)
);

COMMENT ON TABLE  obras                     IS 'Projetos de construção civil gerenciados pelo sistema';
COMMENT ON COLUMN obras.id                  IS 'Identificador único da obra (auto-incremento)';
COMMENT ON COLUMN obras.nome                IS 'Nome ou título da obra';
COMMENT ON COLUMN obras.cliente             IS 'Nome do cliente/contratante';
COMMENT ON COLUMN obras.endereco            IS 'Endereço completo do local da obra';
COMMENT ON COLUMN obras.orcamento_previsto  IS 'Valor orçado para a execução total da obra (em reais)';
COMMENT ON COLUMN obras.status              IS 'Estado atual da obra: PLANEJAMENTO | EM_ANDAMENTO | CONCLUIDA | PAUSADA';
COMMENT ON COLUMN obras.data_inicio         IS 'Data de início efetivo dos trabalhos';
COMMENT ON COLUMN obras.data_previsao_fim   IS 'Data prevista para conclusão da obra';
COMMENT ON COLUMN obras.created_at          IS 'Timestamp de criação do registro';
COMMENT ON COLUMN obras.updated_at          IS 'Timestamp da última atualização do registro';

-- Índice: busca por status (filtros de listagem)
CREATE INDEX idx_obras_status ON obras (status);

-- Índice: busca por cliente (consultas por contratante)
CREATE INDEX idx_obras_cliente ON obras (cliente);


-- ─────────────────────────────────────────────────────────────
-- TABELA: transacoes_financeiras
-- Registra todas as movimentações financeiras de uma obra
-- (receitas de aporte e despesas operacionais).
-- ─────────────────────────────────────────────────────────────
CREATE TABLE transacoes_financeiras (
    id              BIGSERIAL       PRIMARY KEY,
    obra_id         BIGINT          NOT NULL,
    descricao       VARCHAR(255)    NOT NULL,
    tipo            VARCHAR(20)     NOT NULL
                      CONSTRAINT ck_transacoes_tipo
                      CHECK (tipo IN ('RECEITA', 'DESPESA')),
    categoria       VARCHAR(50)     NOT NULL
                      CONSTRAINT ck_transacoes_categoria
                      CHECK (categoria IN (
                          'MATERIAL',
                          'MAO_DE_OBRA',
                          'EQUIPAMENTO',
                          'TAXAS',
                          'APORTE',
                          'OUTROS'
                      )),
    valor           NUMERIC(15, 2)  NOT NULL
                      CONSTRAINT ck_transacoes_valor_positivo
                      CHECK (valor > 0),
    data_transacao  DATE            NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign Key: exclusão em cascata quando a obra pai for removida
    CONSTRAINT fk_transacoes_obra
        FOREIGN KEY (obra_id)
        REFERENCES obras (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

COMMENT ON TABLE  transacoes_financeiras                IS 'Movimentações financeiras (receitas e despesas) associadas às obras';
COMMENT ON COLUMN transacoes_financeiras.id             IS 'Identificador único da transação';
COMMENT ON COLUMN transacoes_financeiras.obra_id        IS 'Obra à qual esta transação pertence (FK → obras.id)';
COMMENT ON COLUMN transacoes_financeiras.descricao      IS 'Descrição livre da transação';
COMMENT ON COLUMN transacoes_financeiras.tipo           IS 'Tipo financeiro: RECEITA (entrada) ou DESPESA (saída)';
COMMENT ON COLUMN transacoes_financeiras.categoria      IS 'Categoria da transação: MATERIAL | MAO_DE_OBRA | EQUIPAMENTO | TAXAS | APORTE | OUTROS';
COMMENT ON COLUMN transacoes_financeiras.valor          IS 'Valor absoluto da transação em reais (sempre positivo)';
COMMENT ON COLUMN transacoes_financeiras.data_transacao IS 'Data em que a transação ocorreu ou foi registrada';
COMMENT ON COLUMN transacoes_financeiras.created_at     IS 'Timestamp de criação do registro';

-- Índice: busca de transações por obra (JOIN e filtros mais frequentes)
CREATE INDEX idx_transacoes_obra_id ON transacoes_financeiras (obra_id);

-- Índice: filtro por tipo financeiro (relatórios de receitas vs despesas)
CREATE INDEX idx_transacoes_tipo ON transacoes_financeiras (tipo);

-- Índice: filtro por categoria (relatórios de custos por categoria)
CREATE INDEX idx_transacoes_categoria ON transacoes_financeiras (categoria);

-- Índice composto: consultas de extrato por obra + período (caso de uso mais comum)
CREATE INDEX idx_transacoes_obra_data ON transacoes_financeiras (obra_id, data_transacao DESC);
