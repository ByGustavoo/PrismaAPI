package br.com.prismaapi.model.dto.recorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoRecorrente;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de uma Recorrente.")
public record RecorrenteDTO(

        UUID id,

        TipoRecorrente tipo,

        String descricao,

        BigDecimal valor,

        CategoriaDTO categoria,

        Frequencia frequencia,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate proximoVencimento,

        UUID idOrigem,

        String nomeOrigem,

        SituacaoRecorrente situacao,

        Boolean lancarAutomaticamente,

        @JsonInclude(JsonInclude.Include.NON_NULL)
        String observacoes

) {}