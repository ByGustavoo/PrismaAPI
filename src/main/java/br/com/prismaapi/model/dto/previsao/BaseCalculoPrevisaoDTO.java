package br.com.prismaapi.model.dto.previsao;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa as médias dos meses fechados que servem de base para a previsão.")
public record BaseCalculoPrevisaoDTO(

        List<String> meses,

        BigDecimal receitaMedia,

        BigDecimal despesaMedia,

        BigDecimal recorrentesMedia,

        BigDecimal aportesMedia

) {}