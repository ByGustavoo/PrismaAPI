package br.com.prismaapi.service.tela;

import br.com.prismaapi.model.dto.tela.TelasOcultasDTO;
import br.com.prismaapi.model.entity.telaoculta.TelaOculta;
import br.com.prismaapi.repository.telaoculta.TelaOcultaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelaService {

    private final TelaOcultaRepository telaOcultaRepository;

    @Cacheable("telas-ocultas")
    @Transactional(readOnly = true)
    public TelasOcultasDTO buscarOcultas() {
        log.info("Buscando as telas ocultas...");

        return new TelasOcultasDTO(telaOcultaRepository.findAll()
                .stream()
                .map(TelaOculta::getTela)
                .sorted()
                .toList());
    }

    @Transactional
    @CacheEvict(value = "telas-ocultas", allEntries = true)
    public TelasOcultasDTO atualizarOcultas(TelasOcultasDTO telasOcultasDTO) {
        log.info("Atualizando as telas ocultas... - Telas: {}", telasOcultasDTO.telas());

        var telas = telasOcultasDTO.telas()
                .stream()
                .distinct()
                .sorted()
                .toList();

        telaOcultaRepository.deleteAllInBatch();
        telaOcultaRepository.saveAll(telas.stream()
                .map(tela -> {
                    var telaOculta = new TelaOculta();
                    telaOculta.setTela(tela);
                    return telaOculta;
                })
                .toList());

        return new TelasOcultasDTO(telas);
    }
}