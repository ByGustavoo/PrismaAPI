package br.com.prismaapi.service.aviso;

import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import br.com.prismaapi.model.entity.preferenciasavisos.PreferenciasAvisos;
import br.com.prismaapi.model.mapper.preferenciasavisos.PreferenciasAvisosMapper;
import br.com.prismaapi.repository.preferenciasavisos.PreferenciasAvisosRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreferenciasAvisosService {

    private static final Set<Integer> LEMBRETES = Set.of(15, 30, 60, 90);
    private final PreferenciasAvisosMapper preferenciasAvisosMapper;
    private final PreferenciasAvisosRepository preferenciasAvisosRepository;

    @Cacheable("preferencias-avisos")
    @Transactional(readOnly = true)
    public PreferenciasAvisosDTO buscar() {
        log.info("Buscando as preferências dos avisos...");

        return preferenciasAvisosRepository.findFirstByOrderByDataAtualizacaoAsc()
                .map(preferenciasAvisosMapper::toDTO)
                .orElseGet(() -> preferenciasAvisosMapper.toDTO(padrao()));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "preferencias-avisos"}, allEntries = true)
    public PreferenciasAvisosDTO atualizar(PreferenciasAvisosDTO preferenciasAvisosDTO) {
        log.info("Atualizando as preferências dos avisos... - Preferências: {}", preferenciasAvisosDTO);
        validar(preferenciasAvisosDTO);

        var preferencias = preferenciasAvisosRepository.findFirstByOrderByDataAtualizacaoAsc().orElseGet(PreferenciasAvisosService::padrao);
        preferenciasAvisosMapper.updateEntity(preferenciasAvisosDTO, preferencias);

        return preferenciasAvisosMapper.toDTO(preferenciasAvisosRepository.save(preferencias));
    }

    private static void validar(PreferenciasAvisosDTO preferenciasAvisosDTO) {
        if (!Set.of(3, 7, 15, 30).contains(preferenciasAvisosDTO.diasAntecedencia())) {
            log.warn("Escolha uma antecedência de 3, 7, 15 ou 30 dias!");
            throw new RequisicaoInvalidaException("Escolha uma antecedência de 3, 7, 15 ou 30 dias!");
        }

        if (!Set.of(70, 80, 90).contains(preferenciasAvisosDTO.percentualLimiteCartao())) {
            log.warn("Escolha um limite do cartão de 70%, 80% ou 90%!");
            throw new RequisicaoInvalidaException("Escolha um limite do cartão de 70%, 80% ou 90%!");
        }

        if (!LEMBRETES.contains(preferenciasAvisosDTO.diasLembreteInvestimento()) || !LEMBRETES.contains(preferenciasAvisosDTO.diasLembreteMeta())) {
            log.warn("Escolha um lembrete de 15, 30, 60 ou 90 dias!");
            throw new RequisicaoInvalidaException("Escolha um lembrete de 15, 30, 60 ou 90 dias!");
        }
    }

    private static PreferenciasAvisos padrao() {
        var preferencias = new PreferenciasAvisos();

        preferencias.setDiasAntecedencia((short) 15);
        preferencias.setPercentualLimiteCartao((short) 70);
        preferencias.setDiasLembreteInvestimento((short) 30);
        preferencias.setDiasLembreteMeta((short) 30);

        return preferencias;
    }
}