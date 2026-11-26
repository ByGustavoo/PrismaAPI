ALTER TABLE prisma.recorrentes ADD COLUMN IF NOT EXISTS dia_vencimento SMALLINT;

UPDATE prisma.recorrentes SET dia_vencimento = EXTRACT(DAY FROM proximo_vencimento) WHERE dia_vencimento IS NULL;

ALTER TABLE prisma.recorrentes ALTER COLUMN dia_vencimento SET NOT NULL;

ALTER TABLE prisma.recorrentes ADD CONSTRAINT recorrentes_dia_vencimento_check CHECK (dia_vencimento BETWEEN 1 AND 31);