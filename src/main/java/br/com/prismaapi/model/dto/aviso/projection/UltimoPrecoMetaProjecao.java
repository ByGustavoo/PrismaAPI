package br.com.prismaapi.model.dto.aviso.projection;

import java.time.LocalDate;
import java.util.UUID;

public record UltimoPrecoMetaProjecao(

        UUID idMeta,

        String nome,

        LocalDate dataUltimoPreco

) {}