package br.com.prismaapi.model.dto.orcamento;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.UUID;

@Schema(description = "Representa o modelo de dados de um Orçamento.")
public record OrcamentoDTO(

        UUID id,

        CategoriaDTO categoria,

        BigDecimal limiteMensal

) {}