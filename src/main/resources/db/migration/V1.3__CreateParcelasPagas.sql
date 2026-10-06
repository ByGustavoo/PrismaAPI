CREATE TABLE IF NOT EXISTS prisma.parcelas_pagas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    id_compra_parcelada UUID NOT NULL,
    numero SMALLINT NOT NULL,
    data_pagamento DATE NOT NULL,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT parcelas_pagas_compra_parcelada_fk FOREIGN KEY (id_compra_parcelada) REFERENCES prisma.compras_parceladas(id) ON DELETE CASCADE,
    CONSTRAINT parcelas_pagas_numero_check CHECK (numero BETWEEN 1 AND 48),
    CONSTRAINT parcelas_pagas_sem_duplicata UNIQUE (id_compra_parcelada, numero)
);
