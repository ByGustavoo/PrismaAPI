package br.com.prismaapi.model.dto.dashboard.projection;

import java.math.BigDecimal;
import java.util.UUID;

public record GastoCategoriaProjecao(

        UUID categoriaId,

        BigDecimal valor

) {}