package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Lançamento.")
public record LancamentoDTO(

        UUID id,

        String descricao,

        BigDecimal valor,

        TipoLancamento tipo,

        SituacaoLancamento situacao,

        FormaLancamento forma,

        @JsonFormat(pattern = "dd/MM/yyyy")
        LocalDate data,

        UUID categoriaId,

        UUID contaId,

        UUID cartaoId,

        UUID contaDestinoId,

        String observacoes,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataCriacao,

        @JsonFormat(pattern = "dd/MM/yyyy - HH:mm:ss")
        OffsetDateTime dataAtualizacao

) {}
