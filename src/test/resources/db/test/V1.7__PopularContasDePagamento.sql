UPDATE prisma.cartoes
SET id_conta = (SELECT id FROM prisma.contas WHERE nome = 'Conta principal' AND instituicao = 'Banco Aurora')
WHERE tipo = 'CREDITO'
  AND id_conta IS NULL
  AND nome IN ('Aurora Platinum', 'Horizonte Gold');