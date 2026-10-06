INSERT INTO prisma.parcelas_pagas (id_compra_parcelada, numero, data_pagamento)
SELECT id, 1, CURRENT_DATE
FROM prisma.compras_parceladas
WHERE descricao = 'Curso de idiomas'
ON CONFLICT (id_compra_parcelada, numero) DO NOTHING;
