package br.com.prismaapi.model.dto.dashboard.variacao;

import br.com.prismaapi.enums.Tendencia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Variação de um total contra a janela anterior de mesmo tamanho.")
public record VariacaoDTO(

        BigDecimal percentual,

        Tendencia tendencia

) {}