INSERT INTO prisma.categorias (nome, tipo, token_cor) VALUES
    ('Moradia', 'DESPESA', 1),
    ('Contas', 'DESPESA', 2),
    ('Casa', 'DESPESA', 3),
    ('Alimentação', 'DESPESA', 4),
    ('Transporte', 'DESPESA', 5),
    ('Saúde', 'DESPESA', 6),
    ('Educação', 'DESPESA', 7),
    ('Cuidados pessoais', 'DESPESA', 8),
    ('Lazer', 'DESPESA', 9),
    ('Assinaturas', 'DESPESA', 10),
    ('Compras', 'DESPESA', 11),
    ('Investimentos', 'DESPESA', 12),
    ('Outras despesas', 'DESPESA', 13),
    ('Salário', 'RECEITA', 14),
    ('Freelance', 'RECEITA', 15),
    ('Rendimentos', 'RECEITA', 16),
    ('Outras receitas', 'RECEITA', 13)
ON CONFLICT (nome, tipo) DO UPDATE SET token_cor = EXCLUDED.token_cor;