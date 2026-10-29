CREATE TABLE IF NOT EXISTS prisma.preferencias_avisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    dias_antecedencia SMALLINT NOT NULL DEFAULT 15,
    percentual_limite_cartao SMALLINT NOT NULL DEFAULT 70,
    dias_lembrete_investimento SMALLINT NOT NULL DEFAULT 30,
    dias_lembrete_meta SMALLINT NOT NULL DEFAULT 30,
    data_atualizacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT preferencias_avisos_dias_antecedencia_check CHECK (dias_antecedencia IN (3, 7, 15, 30)),
    CONSTRAINT preferencias_avisos_percentual_limite_cartao_check CHECK (percentual_limite_cartao IN (70, 80, 90)),
    CONSTRAINT preferencias_avisos_dias_lembrete_investimento_check CHECK (dias_lembrete_investimento IN (15, 30, 60, 90)),
    CONSTRAINT preferencias_avisos_dias_lembrete_meta_check CHECK (dias_lembrete_meta IN (15, 30, 60, 90))
);

CREATE TABLE IF NOT EXISTS prisma.tipos_aviso_desligados (
    id_preferencias_avisos UUID NOT NULL,
    tipo VARCHAR(40) NOT NULL,
    CONSTRAINT tipos_aviso_desligados_pk PRIMARY KEY (id_preferencias_avisos, tipo),
    CONSTRAINT tipos_aviso_desligados_preferencias_avisos_fk FOREIGN KEY (id_preferencias_avisos) REFERENCES prisma.preferencias_avisos(id) ON DELETE CASCADE,
    CONSTRAINT tipos_aviso_desligados_tipo_check CHECK (tipo IN (
        'FATURA_VENCENDO',
        'CONTA_VENCENDO',
        'RECORRENTE_VENCENDO',
        'LANCAMENTO_AGENDADO',
        'RECEITA_PREVISTA',
        'LIMITE_CARTAO',
        'INVESTIMENTO_DESATUALIZADO',
        'ORCAMENTO_NO_LIMITE',
        'SALDO_NEGATIVO_PREVISTO',
        'META_SEM_PRECO'
    ))
);

INSERT INTO prisma.preferencias_avisos (id)
SELECT gen_random_uuid()
WHERE NOT EXISTS (SELECT 1 FROM prisma.preferencias_avisos);

CREATE TABLE IF NOT EXISTS prisma.leituras_avisos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    chave VARCHAR(120) NOT NULL,
    assinatura VARCHAR(200) NOT NULL,
    data_fim_adiamento DATE,
    data_criacao TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT leituras_avisos_chave_unica UNIQUE (chave)
);
