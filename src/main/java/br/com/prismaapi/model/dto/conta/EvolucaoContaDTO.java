package br.com.prismaapi.model.dto.conta;

import br.com.prismaapi.enums.FinalidadeConta;
import br.com.prismaapi.model.dto.investimento.PontoEvolucaoDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "Representa a evolução de uma Conta nos últimos doze meses, com aportes, resgates e rendimentos.")
public record EvolucaoContaDTO(

        ContaDTO conta,

        FinalidadeConta finalidade,

        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate dataInicial,

        BigDecimal saldoInicial,

        BigDecimal aportes,

        BigDecimal resgates,

        BigDecimal rendimentos,

        BigDecimal saldoAtual,

        BigDecimal rentabilidade,

        List<PontoEvolucaoDTO> evolucao,

        List<MovimentacaoContaDTO> movimentacoes

) {}