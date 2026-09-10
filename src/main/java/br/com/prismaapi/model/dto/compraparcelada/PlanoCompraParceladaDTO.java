package br.com.prismaapi.model.dto.compraparcelada;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Representa uma Compra Parcelada com o cronograma de parcelas e os totais já calculados.")
public record PlanoCompraParceladaDTO(

        CompraParceladaDTO compra,

        BigDecimal valorParcela,

        Integer parcelasPagas,

        Integer parcelasRestantes,

        BigDecimal valorPago,

        BigDecimal valorRestante,

        ParcelaDTO parcelaAtual,

        List<ParcelaDTO> cronograma

) {}