package br.com.prismaapi.service.aviso;

import br.com.prismaapi.enums.EstadoAviso;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.exceptions.AvisoNaoEncontradoException;
import br.com.prismaapi.model.dto.aviso.AdiarAvisoDTO;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.entity.leituraaviso.LeituraAviso;
import br.com.prismaapi.repository.leituraaviso.LeituraAvisoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvisoService {

    private final LeituraAvisoRepository leituraAvisoRepository;
    private final DerivacaoAvisosService derivacaoAvisosService;
    private static final Set<TipoAviso> DATADOS_HOJE = Set.of(TipoAviso.LIMITE_CARTAO, TipoAviso.ORCAMENTO_NO_LIMITE);

    @Transactional(readOnly = true)
    public List<AvisoDTO> listar() {
        log.info("Listando os avisos... - Data: {}", LocalDate.now());
        var hoje = LocalDate.now();
        var leituras = leiturasPorChave();

        return derivacaoAvisosService.derivar(hoje)
                .stream()
                .map(aviso -> comEstado(aviso, leituras.get(aviso.id()), hoje))
                .toList();
    }

    @Transactional
    public AvisoDTO registrarLeitura(String id) {
        log.info("Marcando o aviso como lido... - ID: {}", id);
        var aviso = encontrar(id);
        var leitura = gravar(aviso, null);

        return comEstado(aviso, leitura, LocalDate.now());
    }

    @Transactional
    public AvisoDTO registrarAdiamento(String id, AdiarAvisoDTO adiarAvisoDTO) {
        log.info("Adiando o aviso... - ID: {} - Dias: {}", id, adiarAvisoDTO.dias());
        var hoje = LocalDate.now();
        var aviso = encontrar(id);
        var leitura = gravar(aviso, hoje.plusDays(adiarAvisoDTO.dias()));

        return comEstado(aviso, leitura, hoje);
    }

    @Transactional
    public void deletarLeitura(String id) {
        log.info("Devolvendo o aviso aos novos... - ID: {}", id);
        leituraAvisoRepository.findByChave(id).ifPresent(leituraAvisoRepository::delete);
    }

    @Transactional
    public List<AvisoDTO> registrarLeituras() {
        log.info("Marcando todos os avisos novos como lidos...");
        var hoje = LocalDate.now();
        var avisos = derivacaoAvisosService.derivar(hoje);
        var leituras = leiturasPorChave();

        var resultado = avisos.stream()
                .map(aviso -> {
                    var atual = comEstado(aviso, leituras.get(aviso.id()), hoje);
                    return atual.estado() == EstadoAviso.NOVO ? comEstado(aviso, gravar(aviso, leituras.get(aviso.id()), null), hoje) : atual;
                })
                .toList();

        limparLeiturasAntigas(avisos);

        return resultado;
    }

    @Transactional
    public void deletarLeituras() {
        log.info("Devolvendo todos os avisos aos novos...");
        leituraAvisoRepository.deleteAllInBatch();
    }

    private Map<String, LeituraAviso> leiturasPorChave() {
        return leituraAvisoRepository.findAll()
                .stream()
                .collect(Collectors.toMap(LeituraAviso::getChave, Function.identity()));
    }

    private AvisoDTO encontrar(String id) {
        return derivacaoAvisosService.derivar(LocalDate.now())
                .stream()
                .filter(aviso -> aviso.id().equals(id))
                .findFirst()
                .orElseThrow(() -> {
                    log.warn("Aviso não encontrado! - ID: [{}]", id);
                    return new AvisoNaoEncontradoException("Este aviso já não existe: a situação que o gerou foi resolvida!");
                });
    }

    private LeituraAviso gravar(AvisoDTO aviso, LocalDate fimDoAdiamento) {
        return gravar(aviso, leituraAvisoRepository.findByChave(aviso.id()).orElse(null), fimDoAdiamento);
    }

    private LeituraAviso gravar(AvisoDTO aviso, LeituraAviso existente, LocalDate fimDoAdiamento) {
        var leitura = existente != null ? existente : new LeituraAviso();

        leitura.setChave(aviso.id());
        leitura.setAssinatura(assinatura(aviso));
        leitura.setDataFimAdiamento(fimDoAdiamento);

        return leituraAvisoRepository.save(leitura);
    }

    private void limparLeiturasAntigas(List<AvisoDTO> avisos) {
        if (avisos.isEmpty()) {
            leituraAvisoRepository.deleteAllInBatch();
            return;
        }

        leituraAvisoRepository.deletarForaDe(avisos.stream().map(AvisoDTO::id).toList());
    }

    private static AvisoDTO comEstado(AvisoDTO aviso, LeituraAviso leitura, LocalDate hoje) {
        if (leitura == null || !leitura.getAssinatura().equals(assinatura(aviso))) return aviso;

        var fimDoAdiamento = leitura.getDataFimAdiamento();

        if (fimDoAdiamento == null) return comEstado(aviso, EstadoAviso.LIDO, null);

        return fimDoAdiamento.isAfter(hoje) ? comEstado(aviso, EstadoAviso.ADIADO, fimDoAdiamento) : aviso;
    }

    private static AvisoDTO comEstado(AvisoDTO aviso, EstadoAviso estado, LocalDate adiadoAte) {
        return new AvisoDTO(
                aviso.id(),
                aviso.tipo(),
                aviso.severidade(),
                aviso.titulo(),
                aviso.descricao(),
                aviso.data(),
                aviso.valor(),
                aviso.rota(),
                estado,
                adiadoAte);
    }

    private static String assinatura(AvisoDTO aviso) {
        var data = DATADOS_HOJE.contains(aviso.tipo()) ? "" : aviso.data().toString();
        var valor = aviso.valor() != null ? aviso.valor().stripTrailingZeros().toPlainString() : "";

        return aviso.severidade() + "|" + data + "|" + valor;
    }
}