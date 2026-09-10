package br.com.prismaapi.model.dto.despesarecorrente;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa as despesas recorrentes com os custos e os vencimentos próximos já calculados.")
public record ResumoDespesasRecorrentesDTO(

        List<DespesaRecorrenteDTO> itens,

        BigDecimal custoMensal,

        BigDecimal custoAnual,

        List<DespesaRecorrenteDTO> vencendoEmBreve

) {}