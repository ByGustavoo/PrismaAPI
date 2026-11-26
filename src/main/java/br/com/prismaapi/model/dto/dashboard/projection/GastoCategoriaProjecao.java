package br.com.prismaapi.model.dto.dashboard.projection;

import br.com.prismaapi.model.entity.categoria.Categoria;

import java.math.BigDecimal;

public record GastoCategoriaProjecao(

        Categoria categoria,

        BigDecimal valor

) {}