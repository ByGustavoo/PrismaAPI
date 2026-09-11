INSERT INTO categorias (nome, tipo, token_cor) VALUES
    ('Moradia', 'DESPESA', 1),
    ('Alimentação', 'DESPESA', 2),
    ('Transporte', 'DESPESA', 3),
    ('Saúde', 'DESPESA', 4),
    ('Lazer', 'DESPESA', 5),
    ('Educação', 'DESPESA', 6),
    ('Outras despesas', 'DESPESA', 6),
    ('Salário', 'RECEITA', 2),
    ('Freelance', 'RECEITA', 1),
    ('Rendimentos', 'RECEITA', 5),
    ('Outras receitas', 'RECEITA', 4)
ON CONFLICT (nome, tipo) DO NOTHING;