UPDATE prisma.contas conta
SET saldo = conta.saldo + coalesce((
    SELECT sum(lancamento.valor)
    FROM prisma.lancamentos lancamento
    JOIN prisma.contas destino ON destino.id = lancamento.id_conta_destino
    WHERE lancamento.id_conta = conta.id
      AND lancamento.situacao = 'PAGO'
      AND destino.tipo IN ('EMERGENCIA', 'POUPANCA', 'PREVIDENCIA')
), 0)
WHERE conta.tipo NOT IN ('EMERGENCIA', 'POUPANCA', 'PREVIDENCIA');

DELETE FROM prisma.lancamentos lancamento
USING prisma.contas conta
WHERE conta.tipo IN ('EMERGENCIA', 'POUPANCA', 'PREVIDENCIA')
  AND conta.id IN (lancamento.id_conta, lancamento.id_conta_destino);

DELETE FROM prisma.contas WHERE tipo IN ('EMERGENCIA', 'POUPANCA', 'PREVIDENCIA');

UPDATE prisma.investimentos SET reserva_emergencia = TRUE WHERE nome = 'Tesouro Selic 2029';

WITH poupanca AS (
    INSERT INTO prisma.investimentos (nome, classe_ativo, instituicao, aportado, valor_atual, data_inicio, data_ultima_movimentacao, reserva_emergencia, observacoes)
    SELECT 'Poupança', 'POUPANCA', 'Cooperativa Vale', 2000.00, 2100.00, CURRENT_DATE - 180, CURRENT_DATE - 10, TRUE, 'Parte da reserva com liquidez imediata.'
    WHERE NOT EXISTS (SELECT 1 FROM prisma.investimentos WHERE nome = 'Poupança' AND classe_ativo = 'POUPANCA')
    RETURNING id, data_inicio, data_ultima_movimentacao
)
INSERT INTO prisma.movimentacoes_investimento (id_investimento, tipo, data, valor, saldo_informado, descricao)
SELECT id, 'APORTE', data_inicio, 2000.00, NULL, 'Aplicação inicial' FROM poupanca
UNION ALL
SELECT id, 'RENDIMENTO', data_ultima_movimentacao, NULL, 2100.00, 'Extrato do mês' FROM poupanca;