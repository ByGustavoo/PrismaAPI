ALTER TABLE prisma.cartoes DROP CONSTRAINT IF EXISTS cartoes_campos_por_tipo_check;

ALTER TABLE prisma.cartoes ADD CONSTRAINT cartoes_campos_por_tipo_check CHECK (
    CASE tipo
        WHEN 'CREDITO' THEN
            limite_credito IS NOT NULL AND limite_credito > 0
            AND dia_fechamento IS NOT NULL AND dia_vencimento IS NOT NULL
            AND saldo IS NULL
        WHEN 'DEBITO' THEN
            id_conta IS NOT NULL
            AND limite_credito IS NULL AND dia_fechamento IS NULL
            AND dia_vencimento IS NULL AND saldo IS NULL
        ELSE
            saldo IS NOT NULL
            AND limite_credito IS NULL AND dia_fechamento IS NULL
            AND dia_vencimento IS NULL AND id_conta IS NULL
    END
);

ALTER TABLE prisma.lancamentos ADD COLUMN IF NOT EXISTS id_parcela_paga UUID;

ALTER TABLE prisma.lancamentos ADD COLUMN IF NOT EXISTS id_conta_pagamento_fatura UUID;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_parcela_paga_fk FOREIGN KEY (id_parcela_paga) REFERENCES prisma.parcelas_pagas(id) ON DELETE SET NULL;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_conta_pagamento_fatura_fk FOREIGN KEY (id_conta_pagamento_fatura) REFERENCES prisma.contas(id) ON DELETE RESTRICT;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_conta_pagamento_fatura_check CHECK (id_conta_pagamento_fatura IS NULL OR (id_cartao IS NOT NULL AND data_pagamento_fatura IS NOT NULL));

CREATE UNIQUE INDEX IF NOT EXISTS lancamentos_parcela_paga_idx ON prisma.lancamentos (id_parcela_paga) WHERE id_parcela_paga IS NOT NULL;

CREATE INDEX IF NOT EXISTS lancamentos_conta_pagamento_fatura_idx ON prisma.lancamentos (id_conta_pagamento_fatura, data_pagamento_fatura) WHERE id_conta_pagamento_fatura IS NOT NULL;