ALTER TABLE prisma.investimentos ADD COLUMN IF NOT EXISTS reserva_emergencia BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE prisma.investimentos DROP CONSTRAINT IF EXISTS investimentos_classe_ativo_check;

ALTER TABLE prisma.investimentos ADD CONSTRAINT investimentos_classe_ativo_check CHECK (classe_ativo IN (
    'RENDA_FIXA', 'CDB', 'RDB', 'TESOURO', 'POUPANCA', 'PREVIDENCIA', 'ACOES', 'ETF', 'FUNDOS', 'CRIPTO', 'OUTROS'
));

ALTER TABLE prisma.investimentos DROP CONSTRAINT IF EXISTS investimentos_aportado_positivo;

ALTER TABLE prisma.investimentos ADD CONSTRAINT investimentos_aportado_nao_negativo CHECK (aportado >= 0);

CREATE INDEX IF NOT EXISTS investimentos_reserva_emergencia_idx ON prisma.investimentos (reserva_emergencia) WHERE reserva_emergencia;

ALTER TABLE prisma.movimentacoes_investimento ADD COLUMN IF NOT EXISTS id_conta UUID;

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_conta_fk FOREIGN KEY (id_conta) REFERENCES prisma.contas(id) ON DELETE RESTRICT;

ALTER TABLE prisma.movimentacoes_investimento DROP CONSTRAINT IF EXISTS movimentacoes_investimento_tipo_check;

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_tipo_check CHECK (tipo IN ('APORTE', 'RESGATE', 'RENDIMENTO'));

ALTER TABLE prisma.movimentacoes_investimento DROP CONSTRAINT IF EXISTS movimentacoes_investimento_valores_por_tipo_check;

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_valores_por_tipo_check CHECK (
    CASE tipo
        WHEN 'RENDIMENTO' THEN saldo_informado IS NOT NULL AND saldo_informado >= 0 AND valor IS NULL AND id_conta IS NULL
        ELSE valor IS NOT NULL AND valor > 0 AND saldo_informado IS NULL
    END
);

CREATE INDEX IF NOT EXISTS movimentacoes_investimento_conta_idx ON prisma.movimentacoes_investimento (id_conta) WHERE id_conta IS NOT NULL;

ALTER TABLE prisma.lancamentos ADD COLUMN IF NOT EXISTS id_movimentacao_investimento UUID;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_movimentacao_investimento_fk FOREIGN KEY (id_movimentacao_investimento) REFERENCES prisma.movimentacoes_investimento(id) ON DELETE RESTRICT;

CREATE UNIQUE INDEX IF NOT EXISTS lancamentos_movimentacao_investimento_idx ON prisma.lancamentos (id_movimentacao_investimento) WHERE id_movimentacao_investimento IS NOT NULL;

ALTER TABLE prisma.lancamentos DROP CONSTRAINT IF EXISTS lancamentos_origem_unica_check;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_origem_unica_check CHECK (
    num_nonnulls(id_conta, id_cartao) = 1
    OR (id_movimentacao_investimento IS NOT NULL AND id_conta IS NULL AND id_cartao IS NULL)
);

ALTER TABLE prisma.lancamentos DROP CONSTRAINT IF EXISTS lancamentos_transferencia_check;

ALTER TABLE prisma.lancamentos ADD CONSTRAINT lancamentos_transferencia_check CHECK (
    CASE WHEN tipo = 'TRANSFERENCIA' THEN
        id_categoria IS NULL
        AND id_cartao IS NULL
        AND CASE WHEN id_movimentacao_investimento IS NULL THEN
                id_conta IS NOT NULL
                AND id_conta_destino IS NOT NULL
                AND id_conta_destino <> id_conta
            ELSE
                num_nonnulls(id_conta, id_conta_destino) = 1
            END
    ELSE
        id_categoria IS NOT NULL
        AND id_conta_destino IS NULL
        AND id_movimentacao_investimento IS NULL
    END
);