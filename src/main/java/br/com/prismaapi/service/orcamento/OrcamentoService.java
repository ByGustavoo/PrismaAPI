package br.com.prismaapi.service.orcamento;

import br.com.prismaapi.config.cache.LimparCachesDeOrcamento;
import br.com.prismaapi.enums.SituacaoOrcamento;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.CategoriaDeReceitaException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.exceptions.OrcamentoDuplicadoException;
import br.com.prismaapi.exceptions.OrcamentoNaoEncontradoException;
import br.com.prismaapi.model.dto.dashboard.gasto.categoria.GastoCategoriaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.GastoCategoriaProjecao;
import br.com.prismaapi.model.dto.orcamento.ConsumoOrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.OrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.SalvarOrcamentoDTO;
import br.com.prismaapi.model.dto.orcamento.VisaoGeralOrcamentoDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.orcamento.Orcamento;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.model.mapper.orcamento.OrcamentoMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.orcamento.OrcamentoRepository;
import br.com.prismaapi.util.TextoUtil;
import br.com.prismaapi.util.ValorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private final OrcamentoMapper orcamentoMapper;
    private final CategoriaMapper categoriaMapper;
    private final OrcamentoRepository orcamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private static final BigDecimal INICIO_DO_ALERTA = new BigDecimal("0.8");

    @Cacheable("orcamentos")
    @Transactional(readOnly = true)
    public VisaoGeralOrcamentoDTO visaoGeral(YearMonth mes) {
        log.info("Buscando a visão geral do orçamento... - Mês: {}", mes);
        var hoje = LocalDate.now();
        var referencia = mes != null ? mes : YearMonth.from(hoje);
        var diasNoMes = referencia.lengthOfMonth();
        var diasDecorridos = diasDecorridos(referencia, hoje);

        var gastos = lancamentoRepository.agruparPorCategoria(TipoLancamento.DESPESA, referencia.atDay(1), referencia.atEndOfMonth());
        var gastoPorCategoria = gastos.stream()
                .collect(Collectors.toMap(gasto -> gasto.categoria().getId(), GastoCategoriaProjecao::valor));

        var orcamentos = orcamentoRepository.buscarComCategoria();

        var itens = orcamentos.stream()
                .map(orcamento -> consumo(orcamento, gastoPorCategoria.getOrDefault(orcamento.getCategoria().getId(), BigDecimal.ZERO), diasDecorridos, diasNoMes))
                .sorted(Comparator.comparing(ConsumoOrcamentoDTO::consumo, Comparator.reverseOrder())
                        .thenComparing(item -> item.orcamento().categoria().nome(), TextoUtil.ORDEM_ALFABETICA))
                .toList();

        var planejado = ValorUtil.somarEmReais(orcamentos.stream().map(Orcamento::getLimiteMensal));
        var gasto = ValorUtil.somarEmReais(itens.stream().map(ConsumoOrcamentoDTO::gasto));

        return new VisaoGeralOrcamentoDTO(
                referencia,
                planejado,
                gasto,
                planejado.subtract(gasto),
                ValorUtil.fracao(gasto, planejado),
                diasNoMes - diasDecorridos,
                diasDecorridos,
                diasNoMes,
                itens,
                foraDoOrcamento(gastos, orcamentos));
    }

    @Transactional
    @LimparCachesDeOrcamento
    public OrcamentoDTO salvar(SalvarOrcamentoDTO salvarOrcamentoDTO) {
        log.info("Salvando o orçamento... - ID da Categoria: [{}]", salvarOrcamentoDTO.idCategoria());
        var orcamento = orcamentoMapper.toEntity(salvarOrcamentoDTO);
        preencher(orcamento, salvarOrcamentoDTO);

        return orcamentoMapper.toDTO(orcamentoRepository.save(orcamento));
    }

    @Transactional
    @LimparCachesDeOrcamento
    public OrcamentoDTO atualizar(UUID id, SalvarOrcamentoDTO salvarOrcamentoDTO) {
        log.info("Atualizando o orçamento... - ID: [{}]", id);
        var orcamento = buscar(id);

        orcamentoMapper.updateEntity(salvarOrcamentoDTO, orcamento);
        preencher(orcamento, salvarOrcamentoDTO);

        return orcamentoMapper.toDTO(orcamento);
    }

    @Transactional
    @LimparCachesDeOrcamento
    public void deletar(UUID id) {
        log.info("Deletando o orçamento... - ID: [{}]", id);
        orcamentoRepository.delete(buscar(id));
    }

    private Orcamento buscar(UUID id) {
        return orcamentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Orçamento não encontrado! - ID: [{}]", id);
                    return new OrcamentoNaoEncontradoException("Orçamento não encontrado!");
                });
    }

    private void preencher(Orcamento orcamento, SalvarOrcamentoDTO salvarOrcamentoDTO) {
        var categoria = buscarCategoriaDeDespesa(salvarOrcamentoDTO.idCategoria());

        validarDuplicidade(categoria, orcamento.getId());

        orcamento.setCategoria(categoria);
    }

    private Categoria buscarCategoriaDeDespesa(UUID idCategoria) {
        var categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> {
                    log.error("Escolha a categoria do orçamento!");
                    return new CategoriaInexistenteException("Escolha a categoria do orçamento!");
                });

        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            log.error("Só categorias de despesa aceitam orçamento!");
            throw new CategoriaDeReceitaException("Só categorias de despesa aceitam orçamento!");
        }

        return categoria;
    }

    private void validarDuplicidade(Categoria categoria, UUID id) {
        var duplicado = id == null
                ? orcamentoRepository.existsByCategoriaId(categoria.getId())
                : orcamentoRepository.existsByCategoriaIdAndIdNot(categoria.getId(), id);

        if (duplicado) {
            log.error("Já existe um orçamento para {}. Edite o limite existente em vez de criar outro!", categoria.getNome());
            throw new OrcamentoDuplicadoException("Já existe um orçamento para %s. Edite o limite existente em vez de criar outro!".formatted(categoria.getNome()));
        }
    }

    private ConsumoOrcamentoDTO consumo(Orcamento orcamento, BigDecimal gastoNoMes, int diasDecorridos, int diasNoMes) {
        var limite = orcamento.getLimiteMensal();
        var gasto = ValorUtil.dinheiro(gastoNoMes);

        return new ConsumoOrcamentoDTO(
                orcamentoMapper.toDTO(orcamento),
                gasto,
                limite.subtract(gasto),
                ValorUtil.fracao(gasto, limite),
                projecao(gasto, diasDecorridos, diasNoMes),
                situacao(gasto, limite));
    }

    private List<GastoCategoriaDTO> foraDoOrcamento(List<GastoCategoriaProjecao> gastos, List<Orcamento> orcamentos) {
        var orcadas = orcamentos.stream()
                .map(orcamento -> orcamento.getCategoria().getId())
                .collect(Collectors.toSet());

        var semLimite = gastos.stream()
                .filter(gasto -> !orcadas.contains(gasto.categoria().getId()))
                .toList();

        if (semLimite.isEmpty()) {
            return List.of();
        }

        var totalDoMes = ValorUtil.somarEmReais(gastos.stream().map(GastoCategoriaProjecao::valor));

        return semLimite.stream()
                .map(gasto -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(gasto.categoria()),
                        ValorUtil.dinheiro(gasto.valor()),
                        ValorUtil.fracao(gasto.valor(), totalDoMes)))
                .toList();
    }

    private static int diasDecorridos(YearMonth mes, LocalDate hoje) {
        var mesAtual = YearMonth.from(hoje);

        if (mes.isBefore(mesAtual)) {
            return mes.lengthOfMonth();
        }

        return mes.equals(mesAtual) ? hoje.getDayOfMonth() : 0;
    }

    private static BigDecimal projecao(BigDecimal gasto, int diasDecorridos, int diasNoMes) {
        if (diasDecorridos < 10) {
            return ValorUtil.dinheiro(BigDecimal.ZERO);
        }

        return gasto.multiply(BigDecimal.valueOf(diasNoMes)).divide(BigDecimal.valueOf(diasDecorridos), 2, RoundingMode.HALF_UP);
    }

    private static SituacaoOrcamento situacao(BigDecimal gasto, BigDecimal limite) {
        if (gasto.compareTo(limite) >= 0) {
            return SituacaoOrcamento.ESTOURADO;
        }

        return gasto.compareTo(limite.multiply(INICIO_DO_ALERTA)) >= 0 ? SituacaoOrcamento.ALERTA : SituacaoOrcamento.SEGURO;
    }
}