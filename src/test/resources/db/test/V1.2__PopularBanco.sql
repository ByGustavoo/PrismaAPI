CREATE FUNCTION pg_temp.categoria(
    p_nome TEXT,
    p_tipo TEXT
) RETURNS UUID AS $$
    SELECT id
    FROM prisma.categorias
    WHERE nome = p_nome
      AND tipo = p_tipo;
$$ LANGUAGE sql;

CREATE FUNCTION pg_temp.lancar(
    p_descricao TEXT,
    p_valor     NUMERIC,
    p_tipo      TEXT,
    p_forma     TEXT,
    p_data      DATE,
    p_categoria UUID,
    p_conta     UUID,
    p_cartao    UUID DEFAULT NULL,
    p_situacao  TEXT DEFAULT NULL
) RETURNS VOID AS $$
    INSERT INTO prisma.lancamentos (descricao, valor, tipo, situacao, forma, data, id_categoria, id_conta, id_cartao)
    VALUES (p_descricao,
            round(p_valor, 2),
            p_tipo,
            coalesce(p_situacao, CASE WHEN p_data <= CURRENT_DATE THEN 'PAGO' ELSE 'AGENDADO' END),
            p_forma,
            p_data,
            p_categoria,
            p_conta,
            p_cartao);
$$ LANGUAGE sql;

CREATE FUNCTION pg_temp.transferir(
    p_descricao TEXT,
    p_valor     NUMERIC,
    p_data      DATE,
    p_origem    UUID,
    p_destino   UUID,
    p_situacao  TEXT DEFAULT NULL
) RETURNS VOID AS $$
    INSERT INTO prisma.lancamentos (descricao, valor, tipo, situacao, forma, data, id_conta, id_conta_destino)
    VALUES (p_descricao,
            round(p_valor, 2),
            'TRANSFERENCIA',
            coalesce(p_situacao, CASE WHEN p_data <= CURRENT_DATE THEN 'PAGO' ELSE 'AGENDADO' END),
            'PIX',
            p_data,
            p_origem,
            p_destino);
$$ LANGUAGE sql;

CREATE FUNCTION pg_temp.proximo_dia(
    p_dia INT
) RETURNS DATE AS $$
    SELECT CASE
               WHEN date_trunc('month', CURRENT_DATE)::DATE + (p_dia - 1) >= CURRENT_DATE
                   THEN date_trunc('month', CURRENT_DATE)::DATE + (p_dia - 1)
               ELSE (date_trunc('month', CURRENT_DATE) + INTERVAL '1 month')::DATE + (p_dia - 1)
           END;
$$ LANGUAGE sql;

CREATE FUNCTION pg_temp.investir(
    p_nome          TEXT,
    p_classe        TEXT,
    p_instituicao   TEXT,
    p_inicial       NUMERIC,
    p_aporte_mensal NUMERIC,
    p_taxa          NUMERIC,
    p_volatilidade  NUMERIC,
    p_meses         INT,
    p_observacoes   TEXT
) RETURNS VOID AS $$
DECLARE
    v_id       UUID;
    v_mes      DATE;
    v_data     DATE;
    v_ultima   DATE;
    v_primeira DATE;
    v_saldo    NUMERIC := 0;
    v_aportado NUMERIC := 0;
BEGIN
    v_primeira := (date_trunc('month', CURRENT_DATE) - make_interval(months => p_meses))::DATE + 14;

    INSERT INTO prisma.investimentos (nome, classe_ativo, instituicao, aportado, valor_atual, data_inicio, data_ultima_movimentacao, observacoes)
    VALUES (p_nome, p_classe, p_instituicao, p_inicial, p_inicial, v_primeira, v_primeira, p_observacoes)
    RETURNING id INTO v_id;

    FOR k IN REVERSE p_meses..0 LOOP
        v_mes  := (date_trunc('month', CURRENT_DATE) - make_interval(months => k))::DATE;
        v_data := v_mes + 14;

        IF k = p_meses THEN
            INSERT INTO prisma.movimentacoes_investimento (id_investimento, tipo, data, valor, descricao)
            VALUES (v_id, 'APORTE', v_data, p_inicial, 'Aplicação inicial');

            v_saldo    := p_inicial;
            v_aportado := p_inicial;
            v_ultima   := v_data;
        ELSIF p_aporte_mensal > 0 AND v_data <= CURRENT_DATE THEN
            INSERT INTO prisma.movimentacoes_investimento (id_investimento, tipo, data, valor, descricao)
            VALUES (v_id, 'APORTE', v_data, p_aporte_mensal, 'Aporte mensal');

            v_saldo    := v_saldo + p_aporte_mensal;
            v_aportado := v_aportado + p_aporte_mensal;
            v_ultima   := v_data;
        END IF;

        v_data := v_mes + 27;

        IF v_data <= CURRENT_DATE THEN
            v_saldo := greatest(round(v_saldo * (1 + p_taxa + p_volatilidade * sin(k * 1.7)::NUMERIC), 2), 0);

            INSERT INTO prisma.movimentacoes_investimento (id_investimento, tipo, data, saldo_informado, descricao)
            VALUES (v_id, 'RENDIMENTO', v_data, v_saldo, 'Extrato do mês');

            v_ultima := v_data;
        END IF;
    END LOOP;

    UPDATE prisma.investimentos
    SET aportado                 = v_aportado,
        valor_atual              = v_saldo,
        data_ultima_movimentacao = v_ultima
    WHERE id = v_id;
END;
$$ LANGUAGE plpgsql;

DO $$
DECLARE
    v_id_meta     UUID;
    v_inicio      DATE;
    v_antiga      UUID;
    v_aurora      UUID;
    v_debito      UUID;
    v_salario     UUID;
    v_reserva     UUID;
    v_poupanca    UUID;
    v_carteira    UUID;
    v_refeicao    UUID;
    v_horizonte   UUID;
    v_principal   UUID;
    v_previdencia UUID;
    v_alimentacao UUID;
    v_mes         DATE := date_trunc('month', CURRENT_DATE)::DATE;
BEGIN
    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Conta principal',       'Banco Aurora',     'CORRENTE',    3200.00, 'ATIVO',   TRUE)
    RETURNING id INTO v_principal;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Conta salário',         'Banco Horizonte',  'SALARIO',     150.00,  'ATIVO',   TRUE)
    RETURNING id INTO v_salario;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Reserva de emergência', 'Banco Aurora',     'EMERGENCIA',  9500.00, 'ATIVO',   TRUE)
    RETURNING id INTO v_reserva;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Poupança',              'Cooperativa Vale', 'POUPANCA',    2100.00, 'ATIVO',   TRUE)
    RETURNING id INTO v_poupanca;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Previdência',           'Seguradora Atlas', 'PREVIDENCIA', 6400.00, 'ATIVO',   FALSE)
    RETURNING id INTO v_previdencia;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Carteira',              'Dinheiro vivo',    'OUTRA',       280.00,  'ATIVO',   TRUE)
    RETURNING id INTO v_carteira;

    INSERT INTO prisma.contas (nome, instituicao, tipo, saldo, situacao, incluir_no_total)
    VALUES ('Conta antiga',          'Banco Horizonte',  'CORRENTE',    19.90,   'INATIVO', FALSE)
    RETURNING id INTO v_antiga;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Aurora Platinum',  'Banco Aurora',    'CREDITO',          'ATIVO',   'Mastercard', '4821', 12000.00, 3,    10,   NULL,        NULL)
    RETURNING id INTO v_aurora;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Horizonte Gold',   'Banco Horizonte', 'CREDITO',          'ATIVO',   'Visa',       '1934', 5000.00,  25,   5,    NULL,        NULL)
    RETURNING id INTO v_horizonte;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Aurora Débito',    'Banco Aurora',    'DEBITO',           'ATIVO',   'Mastercard', '7702', NULL,     NULL, NULL, v_principal, NULL)
    RETURNING id INTO v_debito;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Vale-alimentação', 'Benefícios Sol',  'VALE_ALIMENTACAO', 'ATIVO',   'Elo',        NULL,   NULL,     NULL, NULL, NULL,        820.00)
    RETURNING id INTO v_alimentacao;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Vale-refeição',    'Benefícios Sol',  'VALE_REFEICAO',    'ATIVO',   'Elo',        NULL,   NULL,     NULL, NULL, NULL,        310.00)
    RETURNING id INTO v_refeicao;

    INSERT INTO prisma.cartoes (nome, instituicao, tipo, situacao, bandeira, ultimos_digitos, limite_credito, dia_fechamento, dia_vencimento, id_conta, saldo)
    VALUES ('Cartão antigo',    'Banco Horizonte', 'CREDITO',          'INATIVO', 'Visa',       '0055', 2000.00,  10,   17,   NULL,        NULL);

    FOR k IN 0..11 LOOP
        v_inicio := (v_mes - make_interval(months => k))::DATE;

        PERFORM pg_temp.lancar('Salário',                   6200.00,              'RECEITA', 'CONTA',          v_inicio + 4,  pg_temp.categoria('Salário', 'RECEITA'),           v_salario,     NULL);
        PERFORM pg_temp.lancar('Academia',                  129.90,               'DESPESA', 'CONTA',          v_inicio,      pg_temp.categoria('Saúde', 'DESPESA'),             v_principal,   NULL);
        PERFORM pg_temp.lancar('Streaming',                 55.90,                'DESPESA', 'CARTAO_CREDITO', v_inicio + 6,  pg_temp.categoria('Assinaturas', 'DESPESA'),       NULL,          v_aurora);
        PERFORM pg_temp.lancar('Farmácia',                  60 + (k * 23) % 70,   'DESPESA', 'PIX',            v_inicio + 7,  pg_temp.categoria('Saúde', 'DESPESA'),             v_principal,   NULL);
        PERFORM pg_temp.lancar('Padaria',                   38 + (k * 11) % 30,   'DESPESA', 'CARTAO_CREDITO', v_inicio + 8,  pg_temp.categoria('Alimentação', 'DESPESA'),       NULL,          v_refeicao);
        PERFORM pg_temp.lancar('Aluguel',                   2200.00,              'DESPESA', 'PIX',            v_inicio + 9,  pg_temp.categoria('Moradia', 'DESPESA'),           v_principal,   NULL);
        PERFORM pg_temp.lancar('Plano de saúde',            420.00,               'DESPESA', 'CONTA',          v_inicio + 11, pg_temp.categoria('Saúde', 'DESPESA'),             v_principal,   NULL);
        PERFORM pg_temp.lancar('Combustível',               210 + (k * 31) % 90,  'DESPESA', 'CARTAO_CREDITO', v_inicio + 11, pg_temp.categoria('Transporte', 'DESPESA'),        NULL,          v_horizonte);
        PERFORM pg_temp.lancar('Barbearia',                 60.00,                'DESPESA', 'DINHEIRO',       v_inicio + 13, pg_temp.categoria('Cuidados pessoais', 'DESPESA'), v_carteira,    NULL);
        PERFORM pg_temp.lancar('Energia elétrica',          180 + (k * 17) % 80,  'DESPESA', 'CONTA',          v_inicio + 14, pg_temp.categoria('Contas', 'DESPESA'),            v_principal,   NULL);
        PERFORM pg_temp.lancar('Feira',                     120 + (k * 13) % 60,  'DESPESA', 'CARTAO_CREDITO', v_inicio + 16, pg_temp.categoria('Alimentação', 'DESPESA'),       NULL,          v_alimentacao);
        PERFORM pg_temp.lancar('Internet',                  119.90,               'DESPESA', 'CONTA',          v_inicio + 19, pg_temp.categoria('Contas', 'DESPESA'),            v_principal,   NULL);
        PERFORM pg_temp.lancar('Restaurante',               140 + (k * 41) % 120, 'DESPESA', 'CARTAO_CREDITO', v_inicio + 20, pg_temp.categoria('Lazer', 'DESPESA'),             NULL,          v_horizonte);
        PERFORM pg_temp.lancar('Combustível',               190 + (k * 29) % 70,  'DESPESA', 'CARTAO_CREDITO', v_inicio + 25, pg_temp.categoria('Transporte', 'DESPESA'),        NULL,          v_horizonte);
        PERFORM pg_temp.lancar('Rendimento da reserva',     70 + (k * 7) % 25,    'RECEITA', 'CONTA',          v_inicio + 27, pg_temp.categoria('Rendimentos', 'RECEITA'),       v_reserva,     NULL);
        PERFORM pg_temp.lancar('Rendimento da poupança',    11 + (k * 3) % 6,     'RECEITA', 'CONTA',          v_inicio + 27, pg_temp.categoria('Rendimentos', 'RECEITA'),       v_poupanca,    NULL);
        PERFORM pg_temp.lancar('Rendimento da previdência', 45 + (k * 5) % 20,    'RECEITA', 'CONTA',          v_inicio + 27, pg_temp.categoria('Rendimentos', 'RECEITA'),       v_previdencia, NULL);

        PERFORM pg_temp.transferir('Transferência do salário',    6000.00, v_inicio + 4,  v_salario,   v_principal);
        PERFORM pg_temp.transferir('Depósito na poupança',        200.00,  v_inicio + 5,  v_principal, v_poupanca);
        PERFORM pg_temp.transferir('Aporte na reserva',           600.00,  v_inicio + 6,  v_principal, v_reserva);
        PERFORM pg_temp.transferir('Contribuição da previdência', 300.00,  v_inicio + 14, v_principal, v_previdencia);

        FOR semana IN 0..3 LOOP
            PERFORM pg_temp.lancar('Mercado',                 320 + ((k + 1) * (semana + 3) * 37) % 260, 'DESPESA', 'CARTAO_CREDITO', v_inicio + 2 + semana * 7, pg_temp.categoria('Alimentação', 'DESPESA'),     NULL,        v_aurora);
        END LOOP;

        IF k % 3 = 1 THEN
            PERFORM pg_temp.lancar('Projeto de freelance',    2400 + k * 50,                             'RECEITA', 'PIX',            v_inicio + 17,             pg_temp.categoria('Freelance', 'RECEITA'),       v_principal, NULL);
        END IF;

        IF k % 2 = 0 THEN
            PERFORM pg_temp.lancar('Cinema',                  72.00,                                     'DESPESA', 'CARTAO_CREDITO', v_inicio + 22,             pg_temp.categoria('Lazer', 'DESPESA'),           NULL,        v_aurora);
        END IF;

        IF k % 4 = 2 THEN
            PERFORM pg_temp.lancar('Curso online',            197.00,                                    'DESPESA', 'CARTAO_CREDITO', v_inicio + 3,              pg_temp.categoria('Educação', 'DESPESA'),        NULL,        v_aurora);
            PERFORM pg_temp.lancar('Manutenção da casa',      380 + k * 15,                              'DESPESA', 'PIX',            v_inicio + 18,             pg_temp.categoria('Casa', 'DESPESA'),            v_principal, NULL);
        END IF;

        IF k % 5 = 3 THEN
            PERFORM pg_temp.lancar('Taxa do banco',           34.90,                                     'DESPESA', 'CONTA',          v_inicio + 1,              pg_temp.categoria('Outras despesas', 'DESPESA'), v_principal, NULL);
            PERFORM pg_temp.lancar('Presente de aniversário', 240.00,                                    'DESPESA', 'CARTAO_CREDITO', v_inicio + 12,             pg_temp.categoria('Compras', 'DESPESA'),         NULL,        v_horizonte);
            PERFORM pg_temp.lancar('Venda de item usado',     350.00,                                    'RECEITA', 'PIX',            v_inicio + 23,             pg_temp.categoria('Outras receitas', 'RECEITA'), v_carteira,  NULL);
        END IF;
    END LOOP;

    PERFORM pg_temp.lancar('Conta de gás',          88.00,   'DESPESA', 'CONTA',          CURRENT_DATE - 20,                        pg_temp.categoria('Contas', 'DESPESA'),          v_principal, NULL,        'PENDENTE');
    PERFORM pg_temp.lancar('Água e esgoto',         96.40,   'DESPESA', 'CONTA',          CURRENT_DATE - 2,                         pg_temp.categoria('Contas', 'DESPESA'),          v_principal, NULL,        'PENDENTE');
    PERFORM pg_temp.lancar('Conserto do chuveiro',  180.00,  'DESPESA', 'PIX',            CURRENT_DATE + 1,                         pg_temp.categoria('Casa', 'DESPESA'),            v_principal, NULL,        'PENDENTE');
    PERFORM pg_temp.lancar('IPVA',                  1280.00, 'DESPESA', 'PIX',            CURRENT_DATE + 3,                         pg_temp.categoria('Transporte', 'DESPESA'),      v_principal, NULL,        'PENDENTE');
    PERFORM pg_temp.lancar('Reembolso de despesas', 430.00,  'RECEITA', 'PIX',            CURRENT_DATE + 6,                         pg_temp.categoria('Outras receitas', 'RECEITA'), v_principal, NULL,        'PENDENTE');
    PERFORM pg_temp.lancar('Seguro residencial',    480.00,  'DESPESA', 'CONTA',          CURRENT_DATE + 9,                         pg_temp.categoria('Moradia', 'DESPESA'),         v_principal, NULL,        'AGENDADO');
    PERFORM pg_temp.lancar('Bônus do trimestre',    1500.00, 'RECEITA', 'CONTA',          CURRENT_DATE + 12,                        pg_temp.categoria('Salário', 'RECEITA'),         v_salario,   NULL,        'AGENDADO');
    PERFORM pg_temp.lancar('Consulta médica',       350.00,  'DESPESA', 'PIX',            CURRENT_DATE + 40,                        pg_temp.categoria('Saúde', 'DESPESA'),           v_principal, NULL,        'AGENDADO');
    PERFORM pg_temp.lancar('Fone de ouvido',        349.90,  'DESPESA', 'CARTAO_CREDITO', CURRENT_DATE - 1,                         pg_temp.categoria('Compras', 'DESPESA'),         NULL,        v_aurora,    NULL);
    PERFORM pg_temp.lancar('Jantar de aniversário', 260.00,  'DESPESA', 'CARTAO_CREDITO', CURRENT_DATE,                             pg_temp.categoria('Lazer', 'DESPESA'),           NULL,        v_horizonte, NULL);
    PERFORM pg_temp.lancar('Tarifa encerrada',      19.90,   'DESPESA', 'CONTA',          (v_mes - INTERVAL '14 months')::DATE + 9, pg_temp.categoria('Outras despesas', 'DESPESA'), v_antiga,    NULL,        NULL);

    PERFORM pg_temp.transferir('Reforço da reserva', 1000.00, CURRENT_DATE + 8, v_principal, v_reserva, 'AGENDADO');

    INSERT INTO prisma.compras_parceladas (descricao, valor_total, parcelas, data_compra, primeiro_mes, id_cartao, id_categoria, observacoes)
    VALUES ('Sofá',             2980.00, 8,  (v_mes - INTERVAL '14 months')::DATE + 5, (v_mes - INTERVAL '13 months')::DATE, v_aurora,    pg_temp.categoria('Casa', 'DESPESA'),     'Já quitado.'),
           ('Geladeira',        3600.00, 12, (v_mes - INTERVAL '8 months')::DATE + 21, (v_mes - INTERVAL '7 months')::DATE,  v_horizonte, pg_temp.categoria('Casa', 'DESPESA'),     NULL),
           ('Notebook',         5400.00, 10, (v_mes - INTERVAL '4 months')::DATE + 17, (v_mes - INTERVAL '3 months')::DATE,  v_aurora,    pg_temp.categoria('Compras', 'DESPESA'),  'Para trabalho e estudo.'),
           ('Passagens aéreas', 1860.00, 6,  (v_mes - INTERVAL '1 month')::DATE + 9,   (v_mes - INTERVAL '1 month')::DATE,   v_aurora,    pg_temp.categoria('Lazer', 'DESPESA'),    'Férias de fim de ano.'),
           ('Tênis de corrida', 459.90,  1,  v_mes,                                    v_mes,                                v_horizonte, pg_temp.categoria('Compras', 'DESPESA'),  NULL),
           ('Curso de idiomas', 1200.00, 4,  v_mes,                                    (v_mes + INTERVAL '1 month')::DATE,   v_aurora,    pg_temp.categoria('Educação', 'DESPESA'), NULL);

    INSERT INTO prisma.despesas_recorrentes (descricao, valor, frequencia, proximo_vencimento, situacao, id_categoria, id_conta, id_cartao, observacoes)
    VALUES ('Academia',               129.90,  'MENSAL',     pg_temp.proximo_dia(1),                   'ATIVO',   pg_temp.categoria('Saúde', 'DESPESA'),       v_principal, NULL,        NULL),
           ('Streaming',              55.90,   'MENSAL',     pg_temp.proximo_dia(7),                   'ATIVO',   pg_temp.categoria('Assinaturas', 'DESPESA'), NULL,        v_aurora,    NULL),
           ('Aluguel',                2200.00, 'MENSAL',     pg_temp.proximo_dia(10),                  'ATIVO',   pg_temp.categoria('Moradia', 'DESPESA'),     v_principal, NULL,        'Reajuste anual pelo IPCA.'),
           ('Plano de saúde',         420.00,  'MENSAL',     pg_temp.proximo_dia(12),                  'ATIVO',   pg_temp.categoria('Saúde', 'DESPESA'),       v_principal, NULL,        NULL),
           ('Internet',               119.90,  'MENSAL',     pg_temp.proximo_dia(20),                  'ATIVO',   pg_temp.categoria('Contas', 'DESPESA'),      v_principal, NULL,        NULL),
           ('Seguro do carro',        210.00,  'MENSAL',     CURRENT_DATE + 4,                         'ATIVO',   pg_temp.categoria('Transporte', 'DESPESA'),  v_principal, NULL,        'Contratado neste mês.'),
           ('Diarista',               180.00,  'QUINZENAL',  CURRENT_DATE + 6,                         'ATIVO',   pg_temp.categoria('Casa', 'DESPESA'),        v_carteira,  NULL,        NULL),
           ('Armazenamento em nuvem', 34.90,   'TRIMESTRAL', CURRENT_DATE + 25,                        'ATIVO',   pg_temp.categoria('Assinaturas', 'DESPESA'), NULL,        v_aurora,    NULL),
           ('Anuidade do conselho',   690.00,  'ANUAL',      (v_mes + INTERVAL '3 months')::DATE + 14, 'ATIVO',   pg_temp.categoria('Educação', 'DESPESA'),    v_principal, NULL,        NULL),
           ('Revista digital',        19.90,   'MENSAL',     pg_temp.proximo_dia(15),                  'PAUSADO', pg_temp.categoria('Assinaturas', 'DESPESA'), NULL,        v_horizonte, 'Pausada até o fim do ano.');

    INSERT INTO prisma.orcamentos (id_categoria, limite_mensal)
    VALUES (pg_temp.categoria('Alimentação', 'DESPESA'), 1900.00),
           (pg_temp.categoria('Transporte', 'DESPESA'),  600.00),
           (pg_temp.categoria('Lazer', 'DESPESA'),       350.00),
           (pg_temp.categoria('Saúde', 'DESPESA'),       700.00),
           (pg_temp.categoria('Assinaturas', 'DESPESA'), 50.00),
           (pg_temp.categoria('Contas', 'DESPESA'),      450.00);

    INSERT INTO prisma.metas (nome, url, situacao, observacoes)
    VALUES ('Notebook para edição', 'https://example.com/notebook', 'ACOMPANHANDO', 'Esperar a próxima promoção.')
    RETURNING id INTO v_id_meta;

    INSERT INTO prisma.metas_precos (id_meta, data, preco, observacao)
    VALUES (v_id_meta, (v_mes - INTERVAL '5 months')::DATE + 3,  8999.00, 'Preço de lançamento'),
           (v_id_meta, (v_mes - INTERVAL '4 months')::DATE + 10, 8699.00, NULL),
           (v_id_meta, (v_mes - INTERVAL '3 months')::DATE + 7,  8899.00, NULL),
           (v_id_meta, (v_mes - INTERVAL '2 months')::DATE + 20, 7999.00, 'Promoção de aniversário da loja'),
           (v_id_meta, (v_mes - INTERVAL '1 month')::DATE + 12,  8299.00, NULL),
           (v_id_meta, CURRENT_DATE,                             7849.00, NULL);

    INSERT INTO prisma.metas (nome, url, situacao, observacoes)
    VALUES ('Cadeira ergonômica',   'https://example.com/cadeira',  'ACOMPANHANDO', NULL)
    RETURNING id INTO v_id_meta;

    INSERT INTO prisma.metas_precos (id_meta, data, preco, observacao)
    VALUES (v_id_meta, (v_mes - INTERVAL '2 months')::DATE + 1,  1890.00, NULL),
           (v_id_meta, (v_mes - INTERVAL '1 month')::DATE + 15,  1990.00, NULL),
           (v_id_meta, CURRENT_DATE - 3,                         2090.00, 'Subiu depois do frete');

    INSERT INTO prisma.metas (nome, url, situacao, observacoes)
    VALUES ('Bicicleta',            NULL,                           'COMPRADA',     'Comprada na liquidação.')
    RETURNING id INTO v_id_meta;

    INSERT INTO prisma.metas_precos (id_meta, data, preco, observacao)
    VALUES (v_id_meta, (v_mes - INTERVAL '6 months')::DATE + 4,  2400.00, NULL),
           (v_id_meta, (v_mes - INTERVAL '5 months')::DATE + 18, 2150.00, 'Preço pago');

    INSERT INTO prisma.metas (nome, url, situacao, observacoes)
    VALUES ('Console de videogame', NULL,                           'CANCELADA',    NULL)
    RETURNING id INTO v_id_meta;

    INSERT INTO prisma.metas_precos (id_meta, data, preco, observacao)
    VALUES (v_id_meta, (v_mes - INTERVAL '9 months')::DATE + 2,  3999.00, NULL);

    PERFORM pg_temp.investir('LCI pós-fixada',      'RENDA_FIXA',  'Banco Aurora',           4500.00,  0.00,   0.0078, 0.0002, 2,  'Vence em dois anos.');
    PERFORM pg_temp.investir('Caixinha de viagem',  'RDB',         'Banco Horizonte',        3000.00,  300.00, 0.0080, 0.0003, 5,  'Viagem do próximo ano.');
    PERFORM pg_temp.investir('Fundo multimercado',  'FUNDOS',      'Gestora Alpha',          7000.00,  0.00,   0.0055, 0.0150, 6,  NULL);
    PERFORM pg_temp.investir('Bitcoin',             'CRIPTO',      'Corretora Cripto Norte', 1500.00,  100.00, 0.0100, 0.1200, 8,  NULL);
    PERFORM pg_temp.investir('Carteira de ações',   'ACOES',       'Corretora Prisma',       6000.00,  0.00,   0.0040, 0.0550, 10, 'Dividendos reinvestidos.');
    PERFORM pg_temp.investir('ETF de índice amplo', 'ETF',         'Corretora Prisma',       2500.00,  400.00, 0.0060, 0.0350, 12, NULL);
    PERFORM pg_temp.investir('Tesouro Selic 2029',  'TESOURO',     'Corretora Prisma',       5000.00,  500.00, 0.0085, 0.0005, 14, 'Liquidez diária para objetivos de médio prazo.');
    PERFORM pg_temp.investir('CDB 110% do CDI',     'CDB',         'Banco Aurora',           10000.00, 0.00,   0.0092, 0.0004, 18, NULL);
    PERFORM pg_temp.investir('Previdência privada', 'PREVIDENCIA', 'Seguradora Atlas',       4000.00,  250.00, 0.0070, 0.0030, 20, 'PGBL com contribuição mensal.');

    UPDATE prisma.contas conta
    SET saldo = conta.saldo + coalesce((
        SELECT sum(CASE
                       WHEN lancamento.tipo = 'RECEITA'             THEN lancamento.valor
                       WHEN lancamento.tipo = 'DESPESA'             THEN -lancamento.valor
                       WHEN lancamento.id_conta_destino = conta.id THEN lancamento.valor
                       ELSE -lancamento.valor
                   END)
        FROM prisma.lancamentos lancamento
        WHERE lancamento.situacao = 'PAGO'
          AND (lancamento.id_conta = conta.id OR lancamento.id_conta_destino = conta.id)
    ), 0);
END;
$$;