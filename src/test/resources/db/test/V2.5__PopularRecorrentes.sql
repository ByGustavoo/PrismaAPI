UPDATE prisma.recorrentes SET lancar_automaticamente = TRUE WHERE descricao = 'Streaming';

INSERT INTO prisma.recorrentes (tipo, descricao, valor, frequencia, proximo_vencimento, situacao, lancar_automaticamente, id_categoria, id_conta, id_cartao)
VALUES ('RECEITA', 'Salário mensal', 6500.00, 'MENSAL', (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month 4 days')::DATE, 'ATIVO', TRUE,
        (SELECT id FROM prisma.categorias WHERE nome = 'Salário' AND tipo = 'RECEITA'),
        (SELECT id FROM prisma.contas WHERE nome = 'Conta salário'),
        NULL),
       ('RECEITA', 'Recarga do vale-alimentação', 1000.00, 'MENSAL', (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')::DATE, 'ATIVO', TRUE,
        (SELECT id FROM prisma.categorias WHERE nome = 'Outras receitas' AND tipo = 'RECEITA'),
        NULL,
        (SELECT id FROM prisma.cartoes WHERE nome = 'Vale-alimentação'));