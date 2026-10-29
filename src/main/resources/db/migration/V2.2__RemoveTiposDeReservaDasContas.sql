ALTER TABLE prisma.contas DROP CONSTRAINT IF EXISTS contas_tipo_check;

ALTER TABLE prisma.contas ADD CONSTRAINT contas_tipo_check CHECK (tipo IN ('CORRENTE', 'SALARIO', 'OUTRA'));