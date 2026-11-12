ALTER TABLE prisma.investimentos ADD COLUMN IF NOT EXISTS codigo VARCHAR(12);

ALTER TABLE prisma.investimentos ADD COLUMN IF NOT EXISTS quantidade NUMERIC(18, 8);

ALTER TABLE prisma.investimentos ADD COLUMN IF NOT EXISTS cotacao NUMERIC(14, 2);

ALTER TABLE prisma.investimentos ADD CONSTRAINT investimentos_quantidade_nao_negativa CHECK (quantidade >= 0);

ALTER TABLE prisma.investimentos ADD CONSTRAINT investimentos_cotacao_nao_negativa CHECK (cotacao >= 0);

ALTER TABLE prisma.investimentos ADD CONSTRAINT investimentos_posicao_so_em_acoes_check CHECK (
    classe_ativo = 'ACOES' OR (codigo IS NULL AND quantidade IS NULL AND cotacao IS NULL)
);

ALTER TABLE prisma.movimentacoes_investimento ADD COLUMN IF NOT EXISTS quantidade NUMERIC(18, 8);

ALTER TABLE prisma.movimentacoes_investimento ADD COLUMN IF NOT EXISTS preco_unitario NUMERIC(14, 2);

ALTER TABLE prisma.movimentacoes_investimento ADD COLUMN IF NOT EXISTS taxas NUMERIC(14, 2);

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_quantidade_positiva CHECK (quantidade > 0);

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_preco_unitario_nao_negativo CHECK (preco_unitario >= 0);

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_taxas_nao_negativas CHECK (taxas >= 0);

ALTER TABLE prisma.movimentacoes_investimento ADD CONSTRAINT movimentacoes_investimento_quantidade_com_preco_check CHECK (
    (quantidade IS NULL OR preco_unitario IS NOT NULL)
    AND (taxas IS NULL OR quantidade IS NOT NULL)
    AND (tipo = 'RENDIMENTO' OR (quantidade IS NULL) = (preco_unitario IS NULL))
);