package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Representa o modelo de dados de um Lançamento.")
public record LancamentoDTO(

        UUID id,

        String descricao,

        BigDecimal valor,

        TipoLancamento tipo,

        SituacaoLancamento situacao,

        FormaLancamento forma,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate data,

        CategoriaDTO categoria,

        UUID idOrigem,

        String nomeOrigem,

        UUID idContaDestino,

        String nomeContaDestino,

        String observacoes

) {}