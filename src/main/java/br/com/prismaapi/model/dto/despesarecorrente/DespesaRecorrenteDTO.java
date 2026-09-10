package br.com.prismaapi.model.dto.despesarecorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Despesa Recorrente.")
public record DespesaRecorrenteDTO(

        UUID id,

        String descricao,

        BigDecimal valor,

        CategoriaDTO categoria,

        Frequencia frequencia,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate proximoVencimento,

        UUID idOrigem,

        String nomeOrigem,

        SituacaoDespesaRecorrente situacao,

        String observacoes

) {}