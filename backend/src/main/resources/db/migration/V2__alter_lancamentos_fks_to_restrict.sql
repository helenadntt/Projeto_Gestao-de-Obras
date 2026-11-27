-- =============================================================
-- Flyway Migration: V2__alter_lancamentos_fks_to_restrict.sql
-- Projeto  : Gestão de Obras API
-- Descrição: Altera as foreign keys de lancamentos_financeiros
--            de CASCADE / SET NULL para RESTRICT, impedindo
--            exclusão ou desvinculação acidental no banco.
--            Compatível com PostgreSQL 16 e H2 (modo PostgreSQL).
-- =============================================================

ALTER TABLE lancamentos_financeiros
    DROP CONSTRAINT fk_lancamentos_obra;

ALTER TABLE lancamentos_financeiros
    ADD CONSTRAINT fk_lancamentos_obra
        FOREIGN KEY (obra_id)
        REFERENCES obras (id)
        ON DELETE RESTRICT;

ALTER TABLE lancamentos_financeiros
    DROP CONSTRAINT fk_lancamentos_centro_custo;

ALTER TABLE lancamentos_financeiros
    ADD CONSTRAINT fk_lancamentos_centro_custo
        FOREIGN KEY (centro_custo_id)
        REFERENCES centros_custo (id)
        ON DELETE RESTRICT;
