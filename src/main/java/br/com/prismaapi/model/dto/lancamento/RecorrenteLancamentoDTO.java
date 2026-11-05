package br.com.prismaapi.model.dto.lancamento;

import br.com.prismaapi.enums.Frequencia;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Identifica a Recorrente da qual o Lançamento faz parte.")
public record RecorrenteLancamentoDTO(

        UUID id,

        Frequencia frequencia

) {}