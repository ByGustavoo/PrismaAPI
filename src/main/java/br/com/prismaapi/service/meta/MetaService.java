package br.com.prismaapi.service.meta;

import br.com.prismaapi.enums.LeituraMeta;
import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.enums.Tendencia;
import br.com.prismaapi.exceptions.DataAnteriorAoPrimeiroPrecoException;
import br.com.prismaapi.exceptions.MetaNaoEncontradaException;
import br.com.prismaapi.exceptions.PrecoDuplicadoException;
import br.com.prismaapi.model.dto.meta.AcompanhamentoMetaDTO;
import br.com.prismaapi.model.dto.meta.AnaliseMetaDTO;
import br.com.prismaapi.model.dto.meta.AtualizarMetaDTO;
import br.com.prismaapi.model.dto.meta.MetaDTO;
import br.com.prismaapi.model.dto.meta.ResumoMetasDTO;
import br.com.prismaapi.model.dto.meta.SalvarMetaDTO;
import br.com.prismaapi.model.dto.metapreco.SalvarMetaPrecoDTO;
import br.com.prismaapi.model.entity.meta.Meta;
import br.com.prismaapi.model.entity.metapreco.MetaPreco;
import br.com.prismaapi.model.mapper.meta.MetaMapper;
import br.com.prismaapi.model.mapper.metapreco.MetaPrecoMapper;
import br.com.prismaapi.repository.meta.MetaRepository;
import br.com.prismaapi.repository.meta.MetaSpecification;
import br.com.prismaapi.repository.metapreco.MetaPrecoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetaService {

    private final MetaMapper metaMapper;
    private final MetaRepository metaRepository;
    private final MetaPrecoMapper metaPrecoMapper;
    private final MetaPrecoRepository metaPrecoRepository;
    private static final BigDecimal FOLGA_DOS_EXTREMOS = new BigDecimal("0.05");
    private static final BigDecimal FAIXA_DA_ESTABILIDADE = new BigDecimal("0.005");
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Cacheable("metas")
    @Transactional(readOnly = true)
    public ResumoMetasDTO listar(SituacaoMeta situacao, String busca) {
        log.info("Listando as metas... - Situação: {} - Busca: {}", situacao, busca);
        var metas = metaRepository.findAll(MetaSpecification.filtrar(situacao, busca));

        if (metas.isEmpty()) {
            var zero = somar(Stream.empty());
            return new ResumoMetasDTO(List.of(), 0, 0, zero, zero, zero, zero);
        }

        var historicos = metaPrecoRepository.findByMetaIdInOrderByDataAscDataCriacaoAsc(metas.stream().map(Meta::getId).toList())
                .stream()
                .collect(Collectors.groupingBy(preco -> preco.getMeta().getId()));

        var itens = metas.stream()
                .map(meta -> acompanhar(meta, historicos.get(meta.getId())))
                .sorted(Comparator.comparing((AcompanhamentoMetaDTO item) -> item.analise().ultimaAtualizacao(), Comparator.reverseOrder())
                        .thenComparing(item -> item.meta().nome(), ORDEM_ALFABETICA))
                .toList();

        var acompanhando = itens.stream()
                .filter(item -> item.meta().situacao() == SituacaoMeta.ACOMPANHANDO)
                .toList();

        var totalAtual = somar(acompanhando.stream().map(item -> item.analise().precoAtual()));
        var totalInicial = somar(acompanhando.stream().map(item -> item.analise().precoInicial()));

        return new ResumoMetasDTO(
                itens,
                acompanhando.size(),
                (int) itens.stream().filter(item -> item.meta().situacao() == SituacaoMeta.COMPRADA).count(),
                totalAtual,
                totalInicial,
                totalAtual.subtract(totalInicial),
                somar(acompanhando.stream().map(item -> item.analise().economia())));
    }

    @Transactional
    @CacheEvict(value = "metas", allEntries = true)
    public MetaDTO salvar(SalvarMetaDTO salvarMetaDTO) {
        log.info("Salvando a meta... - Nome: {}", salvarMetaDTO.nome());
        var meta = metaMapper.toEntity(salvarMetaDTO);
        preencher(meta, salvarMetaDTO.nome(), salvarMetaDTO.url(), salvarMetaDTO.urlImagem(), salvarMetaDTO.observacoes());
        metaRepository.save(meta);

        var primeiroPreco = novoPreco(meta, new SalvarMetaPrecoDTO(salvarMetaDTO.preco(), salvarMetaDTO.data(), null));

        return toDTO(meta, List.of(metaPrecoRepository.save(primeiroPreco)));
    }

    @Transactional
    @CacheEvict(value = "metas", allEntries = true)
    public MetaDTO atualizar(UUID id, AtualizarMetaDTO atualizarMetaDTO) {
        log.info("Atualizando a meta... - ID: [{}]", id);
        var meta = buscar(id);

        metaMapper.updateEntity(atualizarMetaDTO, meta);
        preencher(meta, atualizarMetaDTO.nome(), atualizarMetaDTO.url(), atualizarMetaDTO.urlImagem(), atualizarMetaDTO.observacoes());

        return toDTO(meta, metaPrecoRepository.findByMetaIdOrderByDataAscDataCriacaoAsc(id));
    }

    @Transactional
    @CacheEvict(value = "metas", allEntries = true)
    public MetaDTO registrarPreco(UUID id, SalvarMetaPrecoDTO salvarMetaPrecoDTO) {
        log.info("Registrando o preço da meta... - ID: [{}] - Data: {}", id, salvarMetaPrecoDTO.data());
        var meta = buscar(id);
        var historico = new ArrayList<>(metaPrecoRepository.findByMetaIdOrderByDataAscDataCriacaoAsc(id));

        if (salvarMetaPrecoDTO.data().isBefore(historico.getFirst().getData())) {
            log.error("A data do registro não pode ser anterior ao primeiro preço!");
            throw new DataAnteriorAoPrimeiroPrecoException("A data do registro não pode ser anterior ao primeiro preço!");
        }

        if (metaPrecoRepository.existsByMetaIdAndDataAndPreco(id, salvarMetaPrecoDTO.data(), salvarMetaPrecoDTO.preco())) {
            log.error("Já existe um registro com esse preço nesta data!");
            throw new PrecoDuplicadoException("Já existe um registro com esse preço nesta data!");
        }

        historico.add(metaPrecoRepository.save(novoPreco(meta, salvarMetaPrecoDTO)));
        historico.sort(Comparator.comparing(MetaPreco::getData));

        return toDTO(meta, historico);
    }

    @Transactional
    @CacheEvict(value = "metas", allEntries = true)
    public void deletar(UUID id) {
        log.info("Deletando a meta... - ID: [{}]", id);
        metaRepository.delete(buscar(id));
    }

    private Meta buscar(UUID id) {
        return metaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Meta não encontrada! - ID: [{}]", id);
                    return new MetaNaoEncontradaException("Meta não encontrada!");
                });
    }

    private MetaPreco novoPreco(Meta meta, SalvarMetaPrecoDTO salvarMetaPrecoDTO) {
        var preco = metaPrecoMapper.toEntity(salvarMetaPrecoDTO);

        preco.setMeta(meta);
        preco.setObservacao(textoOuNulo(salvarMetaPrecoDTO.observacao()));

        return preco;
    }

    private MetaDTO toDTO(Meta meta, List<MetaPreco> historico) {
        return metaMapper.toDTO(meta, historico.getFirst().getData(), historico);
    }

    private AcompanhamentoMetaDTO acompanhar(Meta meta, List<MetaPreco> historico) {
        return new AcompanhamentoMetaDTO(toDTO(meta, historico), analisar(historico));
    }

    private static AnaliseMetaDTO analisar(List<MetaPreco> historico) {
        var precos = historico.stream().map(MetaPreco::getPreco).toList();

        var precoInicial = historico.getFirst().getPreco();
        var precoAtual = historico.getLast().getPreco();
        var menorPreco = Collections.min(precos);
        var maiorPreco = Collections.max(precos);
        var precoMedio = precos.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(precos.size()), 2, RoundingMode.HALF_UP);
        var variacao = precoAtual.subtract(precoInicial);

        return new AnaliseMetaDTO(
                precoInicial,
                precoAtual,
                menorPreco,
                maiorPreco,
                precoMedio,
                variacao,
                variacao.multiply(BigDecimal.valueOf(100)).divide(precoInicial, 2, RoundingMode.HALF_UP),
                tendencia(variacao, precoInicial),
                maiorPreco.subtract(precoAtual),
                historico.getLast().getData(),
                historico.size(),
                leitura(precoAtual, menorPreco, maiorPreco, precoMedio, historico.size()));
    }

    private static Tendencia tendencia(BigDecimal variacao, BigDecimal precoInicial) {
        var proporcao = variacao.abs().divide(precoInicial, 10, RoundingMode.HALF_UP);

        if (proporcao.compareTo(FAIXA_DA_ESTABILIDADE) <= 0) {
            return Tendencia.ESTAVEL;
        }

        return variacao.signum() > 0 ? Tendencia.ALTA : Tendencia.BAIXA;
    }

    private static LeituraMeta leitura(BigDecimal precoAtual, BigDecimal menorPreco, BigDecimal maiorPreco, BigDecimal precoMedio, int quantidadeRegistros) {
        if (quantidadeRegistros < 2) {
            return LeituraMeta.PRIMEIRO;
        }

        var faixa = maiorPreco.subtract(menorPreco);

        if (faixa.signum() == 0) {
            return LeituraMeta.ESTAVEL;
        }

        var posicao = precoAtual.subtract(menorPreco).divide(faixa, 10, RoundingMode.HALF_UP);

        if (posicao.compareTo(FOLGA_DOS_EXTREMOS) <= 0) {
            return LeituraMeta.MENOR;
        }

        if (posicao.compareTo(BigDecimal.ONE.subtract(FOLGA_DOS_EXTREMOS)) >= 0) {
            return LeituraMeta.MAIOR;
        }

        return precoAtual.compareTo(precoMedio) < 0 ? LeituraMeta.ABAIXO_DA_MEDIA : LeituraMeta.ACIMA_DA_MEDIA;
    }

    private static void preencher(Meta meta, String nome, String url, String urlImagem, String observacoes) {
        meta.setNome(nome.strip());
        meta.setUrl(textoOuNulo(url));
        meta.setUrlImagem(textoOuNulo(urlImagem));
        meta.setObservacoes(textoOuNulo(observacoes));
    }

    private static BigDecimal somar(Stream<BigDecimal> valores) {
        return valores.reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2, RoundingMode.HALF_UP);
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}