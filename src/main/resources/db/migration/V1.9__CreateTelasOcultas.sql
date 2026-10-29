CREATE TABLE IF NOT EXISTS prisma.telas_ocultas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tela VARCHAR(30) NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT telas_ocultas_tela_unica UNIQUE (tela),
    CONSTRAINT telas_ocultas_tela_check CHECK (tela IN (
        'LANCAMENTOS',
        'RECEITAS',
        'DESPESAS',
        'TRANSFERENCIAS',
        'CONTAS',
        'CARTOES',
        'FATURAS',
        'PARCELAMENTOS',
        'METAS',
        'ORCAMENTO',
        'PREVISAO',
        'RECORRENTES',
        'INVESTIMENTOS',
        'RELATORIOS'
    ))
);