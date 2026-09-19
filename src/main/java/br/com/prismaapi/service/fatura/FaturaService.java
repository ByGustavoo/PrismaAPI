package br.com.prismaapi.service.fatura;

import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoParcela;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.exceptions.FaturaNaoEncontradaException;
import br.com.prismaapi.model.dto.cartao.projection.DespesaCartaoProjecao;
import br.com.prismaapi.model.dto.compraparcelada.ParcelaDTO;
import br.com.prismaapi.model.dto.dashboard.fatura.FaturaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao;
import br.com.prismaapi.model.dto.fatura.DetalheFaturaDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.dto.fatura.ItemFaturaDTO;
import br.com.prismaapi.model.dto.fatura.ParcelaItemFaturaDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.model.mapper.fatura.FaturaMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaMapper faturaMapper;
    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompraParceladaRepository compraParceladaRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final Pattern FORMATO_DO_ID = Pattern.compile("^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})-(\\d{4}-(?:0[1-9]|1[0-2]))$");

    @Cacheable("faturas")
    @Transactional(readOnly = true)
    public List<FaturaCartaoDTO> listar(UUID idCartao) {
        log.info("Listando as faturas... - ID do Cartão: [{}]", idCartao);
        var cartoes = cartaoRepository.findByTipo(TipoCartao.CREDITO)
                .stream()
                .filter(cartao -> idCartao == null || cartao.getId().equals(idCartao))
                .toList();

        return montarFaturas(cartoes, LocalDate.now())
                .stream()
                .sorted(Comparator.comparing(FaturaCartaoDTO::dataVencimento)
                        .thenComparing(FaturaCartaoDTO::nomeCartao, ORDEM_ALFABETICA))
                .toList();
    }

    @Cacheable("faturas")
    @Transactional(readOnly = true)
    public DetalheFaturaDTO detalhar(String id) {
        log.info("Detalhando a fatura... - ID: [{}]", id);
        var partes = FORMATO_DO_ID.matcher(id);

        if (!partes.matches()) {
            throw faturaNaoEncontrada(id);
        }

        var mes = YearMonth.parse(partes.group(2));
        var cartao = cartaoRepository.findById(UUID.fromString(partes.group(1)))
                .filter(encontrado -> encontrado.getTipo() == TipoCartao.CREDITO)
                .orElseThrow(() -> faturaNaoEncontrada(id));

        var fatura = montarFaturas(List.of(cartao), LocalDate.now())
                .stream()
                .filter(montada -> montada.mes().equals(mes.toString()))
                .findFirst()
                .orElseThrow(() -> faturaNaoEncontrada(id));

        return faturaMapper.toDetalheDTO(fatura, itens(cartao, mes));
    }

    @Transactional(readOnly = true)
    public FaturaDTO faturaEmDestaque(YearMonth mes, LocalDate hoje) {
        var cartoes = cartaoRepository.findByTipo(TipoCartao.CREDITO);
        var parcelas = compraParceladaRepository.buscarParcelasAte(mes.atDay(1));

        return cartoes.stream()
                .filter(FaturaService::temCicloDefinido)
                .map(cartao -> montar(cartao, mes, hoje, parcelas))
                .filter(fatura -> fatura.total().signum() > 0)
                .max(Comparator.comparing((FaturaDTO fatura) -> fatura.situacao() == SituacaoFatura.ABERTA)
                        .thenComparing(FaturaDTO::total))
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> limitesComprometidos(List<Cartao> cartoes, LocalDate hoje) {
        var limites = cartoes.stream()
                .filter(cartao -> cartao.getTipo() == TipoCartao.CREDITO)
                .filter(FaturaService::temCicloDefinido)
                .collect(Collectors.toMap(Cartao::getId, cartao -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), (primeiro, segundo) -> primeiro, HashMap::new));

        montarFaturas(cartoes, hoje)
                .stream()
                .filter(fatura -> fatura.situacao() != SituacaoFatura.VENCIDA)
                .forEach(fatura -> limites.merge(fatura.idCartao(), fatura.total(), BigDecimal::add));

        return limites;
    }

    public List<ParcelaDTO> cronograma(CompraParcelada compra, LocalDate hoje) {
        var cartao = compra.getCartao();
        var cronograma = new ArrayList<ParcelaDTO>();

        for (var indice = 0; indice < compra.getParcelas(); indice++) {
            var mes = YearMonth.from(compra.getPrimeiroMes()).plusMonths(indice);
            var dataVencimento = temCicloDefinido(cartao) ? vencimento(cartao, mes) : mes.atEndOfMonth();

            cronograma.add(new ParcelaDTO(
                    indice + 1,
                    mes.toString(),
                    dataVencimento,
                    valorDaParcela(compra.getValorTotal(), compra.getParcelas(), indice),
                    situacaoDaParcela(dataVencimento, hoje, cronograma)));
        }

        return cronograma;
    }

    @Transactional(readOnly = true)
    public NavigableMap<LocalDate, BigDecimal> parcelasPorVencimento(LocalDate hoje) {
        var parcelas = new TreeMap<LocalDate, BigDecimal>();

        compraParceladaRepository.buscarComCartao()
                .stream()
                .flatMap(compra -> cronograma(compra, hoje).stream())
                .forEach(parcela -> parcelas.merge(parcela.dataVencimento(), parcela.valor(), BigDecimal::add));

        return parcelas;
    }

    private List<FaturaCartaoDTO> montarFaturas(List<Cartao> cartoes, LocalDate hoje) {
        var cartoesDeCredito = cartoes.stream()
                .filter(cartao -> cartao.getTipo() == TipoCartao.CREDITO)
                .filter(FaturaService::temCicloDefinido)
                .toList();

        if (cartoesDeCredito.isEmpty()) {
            return List.of();
        }

        var idsCartoes = cartoesDeCredito.stream().map(Cartao::getId).toList();
        var despesas = lancamentoRepository.agruparDespesasDosCartoes(idsCartoes);
        var parcelas = compraParceladaRepository.buscarParcelasDosCartoes(idsCartoes);

        return cartoesDeCredito.stream()
                .flatMap(cartao -> montarFaturasDoCartao(cartao, hoje, despesas, parcelas).stream())
                .toList();
    }

    private static List<FaturaCartaoDTO> montarFaturasDoCartao(Cartao cartao, LocalDate hoje, List<DespesaCartaoProjecao> despesas, List<ParcelaProjecao> parcelas) {
        var totais = new TreeMap<YearMonth, BigDecimal>();
        var quantidades = new HashMap<YearMonth, Long>();

        despesas.stream()
                .filter(despesa -> despesa.cartaoId().equals(cartao.getId()))
                .forEach(despesa -> {
                    var mes = mesDaFatura(cartao, despesa.data());
                    totais.merge(mes, despesa.valor(), BigDecimal::add);
                    quantidades.merge(mes, despesa.quantidade(), Long::sum);
                });

        parcelas.stream()
                .filter(parcela -> parcela.cartaoId().equals(cartao.getId()))
                .forEach(parcela -> {
                    for (var indice = 0; indice < parcela.parcelas(); indice++) {
                        var mes = YearMonth.from(parcela.primeiroMes()).plusMonths(indice);
                        totais.merge(mes, valorDaParcela(parcela.valorTotal(), parcela.parcelas(), indice), BigDecimal::add);
                        quantidades.merge(mes, 1L, Long::sum);
                    }
                });

        var faturas = new ArrayList<FaturaCartaoDTO>();
        BigDecimal totalAnterior = null;

        for (var fatura : totais.entrySet()) {
            var mes = fatura.getKey();
            var montada = montarFatura(cartao, mes, fatura.getValue(), quantidades.get(mes), totalAnterior, hoje);

            faturas.add(montada);
            totalAnterior = montada.total();
        }

        return faturas;
    }

    private static FaturaCartaoDTO montarFatura(Cartao cartao, YearMonth mes, BigDecimal total, Long quantidadeItens, BigDecimal totalAnterior, LocalDate hoje) {
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());
        var aberturaDoCiclo = diaDoMes(mes.minusMonths(1), cartao.getDiaFechamento()).plusDays(1);
        var vencimento = vencimento(cartao, mes);

        return new FaturaCartaoDTO(
                "%s-%s".formatted(cartao.getId(), mes),
                cartao.getId(),
                cartao.getNome(),
                mes.toString(),
                total.setScale(2, RoundingMode.HALF_UP),
                situacao(aberturaDoCiclo, fechamento, vencimento, hoje),
                fechamento,
                vencimento,
                quantidadeItens.intValue(),
                totalAnterior);
    }

    private List<ItemFaturaDTO> itens(Cartao cartao, YearMonth mes) {
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());
        var aberturaDoCiclo = diaDoMes(mes.minusMonths(1), cartao.getDiaFechamento()).plusDays(1);

        var despesas = lancamentoRepository.buscarDespesasDoCartao(cartao.getId(), aberturaDoCiclo, fechamento)
                .stream()
                .map(faturaMapper::toItemDTO);

        var parcelas = compraParceladaRepository.buscarDoCartaoAte(cartao.getId(), mes.atDay(1))
                .stream()
                .filter(compra -> alcancaOMes(compra.getPrimeiroMes(), compra.getParcelas(), mes))
                .map(compra -> itemDaParcela(compra, mes));

        return Stream.concat(despesas, parcelas)
                .sorted(Comparator.comparing(ItemFaturaDTO::data).reversed()
                        .thenComparing(ItemFaturaDTO::descricao, ORDEM_ALFABETICA))
                .toList();
    }

    private ItemFaturaDTO itemDaParcela(CompraParcelada compra, YearMonth mes) {
        var indice = YearMonth.from(compra.getPrimeiroMes()).until(mes, ChronoUnit.MONTHS);
        var parcela = new ParcelaItemFaturaDTO((int) indice + 1, compra.getParcelas().intValue(), compra.getId());

        return faturaMapper.toItemParceladoDTO(compra, parcela, valorDaParcela(compra.getValorTotal(), compra.getParcelas(), indice));
    }

    private FaturaDTO montar(Cartao cartao, YearMonth mes, LocalDate hoje, List<ParcelaProjecao> parcelas) {
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());
        var aberturaDoCiclo = diaDoMes(mes.minusMonths(1), cartao.getDiaFechamento()).plusDays(1);
        var vencimento = vencimento(cartao, mes);

        var lancado = zeroSeNulo(lancamentoRepository.somarDespesasDoCartao(cartao.getId(), aberturaDoCiclo, fechamento));
        var parcelado = somarParcelasDoMes(cartao, mes, parcelas);

        return new FaturaDTO(
                lancado.add(parcelado).setScale(2, RoundingMode.HALF_UP),
                cartao.getNome(),
                vencimento.toString(),
                situacao(aberturaDoCiclo, fechamento, vencimento, hoje));
    }

    private BigDecimal somarParcelasDoMes(Cartao cartao, YearMonth mes, List<ParcelaProjecao> parcelas) {
        return parcelas.stream()
                .filter(parcela -> parcela.cartaoId().equals(cartao.getId()))
                .filter(parcela -> alcancaOMes(parcela.primeiroMes(), parcela.parcelas(), mes))
                .map(parcela -> valorDaParcela(parcela.valorTotal(), parcela.parcelas(), YearMonth.from(parcela.primeiroMes()).until(mes, ChronoUnit.MONTHS)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static YearMonth mesDaFatura(Cartao cartao, LocalDate data) {
        var mes = YearMonth.from(data);
        return data.isAfter(diaDoMes(mes, cartao.getDiaFechamento())) ? mes.plusMonths(1) : mes;
    }

    private static BigDecimal valorDaParcela(BigDecimal valorTotal, Short parcelas, long indice) {
        var base = valorTotal.divide(BigDecimal.valueOf(parcelas), 2, RoundingMode.DOWN);

        if (indice < parcelas - 1L) return base;

        return valorTotal.subtract(base.multiply(BigDecimal.valueOf(parcelas - 1L)));
    }

    private static boolean alcancaOMes(LocalDate primeiroMes, Short parcelas, YearMonth mes) {
        var ultimaParcela = YearMonth.from(primeiroMes).plusMonths(parcelas - 1L);
        return !ultimaParcela.isBefore(mes);
    }

    private static SituacaoParcela situacaoDaParcela(LocalDate dataVencimento, LocalDate hoje, List<ParcelaDTO> anteriores) {
        if (dataVencimento.isBefore(hoje)) return SituacaoParcela.PAGA;

        var atualJaDefinida = anteriores.stream().anyMatch(parcela -> parcela.situacao() != SituacaoParcela.PAGA);
        return atualJaDefinida ? SituacaoParcela.FUTURA : SituacaoParcela.ATUAL;
    }

    private static SituacaoFatura situacao(LocalDate abertura, LocalDate fechamento, LocalDate vencimento, LocalDate hoje) {
        if (hoje.isBefore(abertura)) return SituacaoFatura.FUTURA;
        if (!hoje.isAfter(fechamento)) return SituacaoFatura.ABERTA;
        if (!hoje.isAfter(vencimento)) return SituacaoFatura.FECHADA;
        return SituacaoFatura.VENCIDA;
    }

    private static LocalDate vencimento(Cartao cartao, YearMonth mes) {
        var mesDoVencimento = cartao.getDiaVencimento() > cartao.getDiaFechamento() ? mes : mes.plusMonths(1);
        return diaDoMes(mesDoVencimento, cartao.getDiaVencimento());
    }

    private static LocalDate diaDoMes(YearMonth mes, Short dia) {
        return mes.atDay(Math.min(dia, mes.lengthOfMonth()));
    }

    private static boolean temCicloDefinido(Cartao cartao) {
        return cartao.getDiaFechamento() != null && cartao.getDiaVencimento() != null;
    }

    private static FaturaNaoEncontradaException faturaNaoEncontrada(String id) {
        log.warn("Fatura não encontrada! - ID: [{}]", id);
        return new FaturaNaoEncontradaException("Fatura não encontrada!");
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}