INSERT INTO prisma.categorias (nome, tipo, token_cor) VALUES ('Benefícios', 'RECEITA', 4)
ON CONFLICT (nome, tipo) DO NOTHING;