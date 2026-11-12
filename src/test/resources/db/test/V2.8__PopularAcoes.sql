WITH acao AS (
    INSERT INTO prisma.investimentos (nome, codigo, classe_ativo, instituicao, aportado, valor_atual, quantidade, cotacao, data_inicio, data_ultima_movimentacao, reserva_emergencia, observacoes)
    SELECT 'Petrobras', 'PETR4', 'ACOES', 'Corretora Prisma', 3254.90, 3620.00, 100, 36.20, CURRENT_DATE - 120, CURRENT_DATE - 10, FALSE, 'Dividendos trimestrais.'
    WHERE NOT EXISTS (SELECT 1 FROM prisma.investimentos WHERE codigo = 'PETR4')
    RETURNING id, data_inicio, data_ultima_movimentacao
)
INSERT INTO prisma.movimentacoes_investimento (id_investimento, tipo, data, valor, saldo_informado, quantidade, preco_unitario, taxas, descricao)
SELECT id, 'APORTE', data_inicio, 3254.90, NULL, 100, 32.50, 4.90, 'Primeira compra' FROM acao
UNION ALL
SELECT id, 'RENDIMENTO', data_ultima_movimentacao, NULL, 3620.00, NULL, 36.20, NULL, 'Home broker' FROM acao;