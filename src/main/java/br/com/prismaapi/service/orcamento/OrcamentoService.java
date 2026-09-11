package br.com.prismaapi.service.orcamento;

import br.com.prismaapi.enums.SituacaoOrcamento;
import br.com.prismaapi.enums.TipoCategoria;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrcamentoService {

    private static final int CASAS_DA_FRACAO = 4;
    private final OrcamentoMapper orcamentoMapper;
    private final CategoriaMapper categoriaMapper;
    private final OrcamentoRepository orcamentoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private static final BigDecimal INICIO_DO_ALERTA = new BigDecimal("0.8");
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Transactional(readOnly = true)
    public VisaoGeralOrcamentoDTO visaoGeral(YearMonth mes) {
        log.info("Buscando a visão geral do orçamento... - Mês: {}", mes);
        var hoje = LocalDate.now();
        var referencia = mes != null ? mes : YearMonth.from(hoje);
        var diasNoMes = referencia.lengthOfMonth();
        var diasDecorridos = diasDecorridos(referencia, hoje);

        var gastos = lancamentoRepository.agruparDespesasPorCategoria(referencia.atDay(1), referencia.atEndOfMonth());
        var gastoPorCategoria = gastos.stream()
                .collect(Collectors.toMap(GastoCategoriaProjecao::categoriaId, GastoCategoriaProjecao::valor));

        var orcamentos = orcamentoRepository.buscarComCategoria();

        var itens = orcamentos.stream()
                .map(orcamento -> consumo(orcamento, gastoPorCategoria.getOrDefault(orcamento.getCategoria().getId(), BigDecimal.ZERO), diasDecorridos, diasNoMes))
                .sorted(Comparator.comparing(ConsumoOrcamentoDTO::consumo, Comparator.reverseOrder())
                        .thenComparing(item -> item.orcamento().categoria().nome(), ORDEM_ALFABETICA))
                .toList();

        var planejado = somar(orcamentos.stream().map(Orcamento::getLimiteMensal));
        var gasto = somar(itens.stream().map(ConsumoOrcamentoDTO::gasto));

        return new VisaoGeralOrcamentoDTO(
                referencia,
                planejado,
                gasto,
                planejado.subtract(gasto),
                fracao(gasto, planejado),
                diasNoMes - diasDecorridos,
                diasDecorridos,
                diasNoMes,
                itens,
                foraDoOrcamento(gastos, orcamentos));
    }

    @Transactional
    public OrcamentoDTO salvar(SalvarOrcamentoDTO salvarOrcamentoDTO) {
        log.info("Salvando o orçamento... - ID da Categoria: [{}]", salvarOrcamentoDTO.idCategoria());
        var orcamento = orcamentoMapper.toEntity(salvarOrcamentoDTO);
        preencher(orcamento, salvarOrcamentoDTO);

        return orcamentoMapper.toDTO(orcamentoRepository.save(orcamento));
    }

    @Transactional
    public OrcamentoDTO atualizar(UUID id, SalvarOrcamentoDTO salvarOrcamentoDTO) {
        log.info("Atualizando o orçamento... - ID: [{}]", id);
        var orcamento = buscar(id);

        orcamentoMapper.updateEntity(salvarOrcamentoDTO, orcamento);
        preencher(orcamento, salvarOrcamentoDTO);

        return orcamentoMapper.toDTO(orcamento);
    }

    @Transactional
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
        var gasto = dinheiro(gastoNoMes);

        return new ConsumoOrcamentoDTO(
                orcamentoMapper.toDTO(orcamento),
                gasto,
                limite.subtract(gasto),
                fracao(gasto, limite),
                projecao(gasto, diasDecorridos, diasNoMes),
                situacao(gasto, limite));
    }

    private List<GastoCategoriaDTO> foraDoOrcamento(List<GastoCategoriaProjecao> gastos, List<Orcamento> orcamentos) {
        var orcadas = orcamentos.stream()
                .map(orcamento -> orcamento.getCategoria().getId())
                .collect(Collectors.toSet());

        var semLimite = gastos.stream()
                .filter(gasto -> !orcadas.contains(gasto.categoriaId()))
                .toList();

        if (semLimite.isEmpty()) {
            return List.of();
        }

        var totalDoMes = somar(gastos.stream().map(GastoCategoriaProjecao::valor));
        var categorias = categoriaRepository.findAllById(semLimite.stream().map(GastoCategoriaProjecao::categoriaId).toList())
                .stream()
                .collect(Collectors.toMap(Categoria::getId, Function.identity()));

        return semLimite.stream()
                .map(gasto -> new GastoCategoriaDTO(
                        categoriaMapper.toDTO(categorias.get(gasto.categoriaId())),
                        dinheiro(gasto.valor()),
                        fracao(gasto.valor(), totalDoMes)))
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
            return dinheiro(BigDecimal.ZERO);
        }

        return gasto.multiply(BigDecimal.valueOf(diasNoMes)).divide(BigDecimal.valueOf(diasDecorridos), 2, RoundingMode.HALF_UP);
    }

    private static SituacaoOrcamento situacao(BigDecimal gasto, BigDecimal limite) {
        if (gasto.compareTo(limite) >= 0) {
            return SituacaoOrcamento.ESTOURADO;
        }

        return gasto.compareTo(limite.multiply(INICIO_DO_ALERTA)) >= 0 ? SituacaoOrcamento.ALERTA : SituacaoOrcamento.SEGURO;
    }

    private static BigDecimal somar(Stream<BigDecimal> valores) {
        return dinheiro(valores.reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal fracao(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_DA_FRACAO, RoundingMode.HALF_UP);
        }

        return parte.divide(total, CASAS_DA_FRACAO, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}