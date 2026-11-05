ALTER TABLE prisma.despesas_recorrentes RENAME TO recorrentes;

DO $$
DECLARE
    restricao RECORD;
    indice RECORD;
BEGIN
    FOR restricao IN
        SELECT conname
        FROM pg_constraint
        WHERE conrelid = 'prisma.recorrentes'::regclass
          AND conname LIKE 'despesas_recorrentes_%'
    LOOP
        EXECUTE format('ALTER TABLE prisma.recorrentes RENAME CONSTRAINT %I TO %I', restricao.conname, replace(restricao.conname, 'despesas_recorrentes_', 'recorrentes_'));
    END LOOP;

    FOR indice IN
        SELECT indexname
        FROM pg_indexes
        WHERE schemaname = 'prisma'
          AND tablename = 'recorrentes'
          AND indexname LIKE 'despesas_recorrentes_%'
    LOOP
        EXECUTE format('ALTER INDEX prisma.%I RENAME TO %I', indice.indexname, replace(indice.indexname, 'despesas_recorrentes_', 'recorrentes_'));
    END LOOP;
END $$;

ALTER TABLE prisma.recorrentes ADD COLUMN IF NOT EXISTS tipo VARCHAR(10) NOT NULL DEFAULT 'DESPESA';

ALTER TABLE prisma.recorrentes ADD COLUMN IF NOT EXISTS lancar_automaticamente BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE prisma.recorrentes ADD CONSTRAINT recorrentes_tipo_check CHECK (tipo IN ('RECEITA', 'DESPESA'));

ALTER TABLE prisma.recorrentes ADD CONSTRAINT recorrentes_lancamento_automatico_categoria_check CHECK (NOT lancar_automaticamente OR id_categoria IS NOT NULL);

ALTER TABLE prisma.lancamentos ADD COLUMN IF NOT EXISTS id_recorrente UUID;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_recorrente_fk FOREIGN KEY (id_recorrente) REFERENCES prisma.recorrentes(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS lancamentos_recorrente_idx ON prisma.lancamentos (id_recorrente, data) WHERE id_recorrente IS NOT NULL;