package br.com.prismaapi.service.sistema;

import br.com.prismaapi.model.dto.sistema.VersaoSistemaDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.info.BuildProperties;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SistemaService {

    private final BuildProperties buildProperties;

    public VersaoSistemaDTO buscarVersao() {
        log.info("Buscando a versão do sistema...");
        var versao = buildProperties.getVersion().replaceFirst("^v", "");

        return new VersaoSistemaDTO(versao, buildProperties.getTime());
    }
}