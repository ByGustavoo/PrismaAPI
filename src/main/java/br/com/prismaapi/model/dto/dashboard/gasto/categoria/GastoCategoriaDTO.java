package br.com.prismaapi.model.dto.dashboard.gasto.categoria;

import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import java.math.BigDecimal;

public record GastoCategoriaDTO(

        CategoriaDTO categoria,

        BigDecimal valor,

        BigDecimal participacao

) {}