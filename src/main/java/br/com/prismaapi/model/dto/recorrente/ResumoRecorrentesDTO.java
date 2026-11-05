package br.com.prismaapi.model.dto.recorrente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa as receitas e despesas recorrentes com os totais por mês e os vencimentos próximos já calculados.")
public record ResumoRecorrentesDTO(

        List<RecorrenteDTO> itens,

        BigDecimal custoMensal,

        BigDecimal custoAnual,

        BigDecimal receitaMensal,

        List<RecorrenteDTO> vencendoEmBreve

) {}