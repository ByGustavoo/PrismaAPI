package br.com.prismaapi.model.dto.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Representa a fatia da carteira ocupada por uma classe de ativo.")
public record AlocacaoDTO(

        ClasseAtivo classeAtivo,

        BigDecimal aportado,

        BigDecimal valorAtual,

        BigDecimal rendimento,

        BigDecimal participacao,

        Integer quantidade

) {}