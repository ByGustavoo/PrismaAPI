package br.com.prismaapi.model.dto.despesarecorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Despesa Recorrente.")
public record DespesaRecorrenteDTO(

        UUID id,

        String descricao,

        BigDecimal valor,

        Frequencia frequencia,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate proximoVencimento,

        SituacaoDespesaRecorrente situacao,

        UUID categoriaId,

        UUID contaId,

        UUID cartaoId,

        String observacoes,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
