package br.com.prismaapi.service.fatura;

import br.com.prismaapi.config.cache.LimparCachesDeFatura;
import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.SituacaoParcela;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.CartaoSemContaDePagamentoException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.exceptions.FaturaJaPagaException;
import br.com.prismaapi.exceptions.FaturaNaoEncontradaException;
import br.com.prismaapi.exceptions.PagamentoDeFaturaNaoEncontradoException;
import br.com.prismaapi.model.dto.cartao.projection.DespesaCartaoProjecao;
import br.com.prismaapi.model.dto.compraparcelada.ParcelaDTO;
import br.com.prismaapi.model.dto.compraparcelada.projection.ParcelaPagaProjecao;
import br.com.prismaapi.model.dto.dashboard.fatura.FaturaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao;
import br.com.prismaapi.model.dto.fatura.DetalheFaturaDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.dto.fatura.ItemFaturaDTO;
import br.com.prismaapi.model.dto.fatura.ParcelaItemFaturaDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.parcelapaga.ParcelaPaga;
import br.com.prismaapi.model.mapper.fatura.FaturaMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.parcelapaga.ParcelaPagaRepository;
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
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaturaService {

    private final FaturaMapper faturaMapper;
    private final CartaoRepository cartaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;
    private final ParcelaPagaRepository parcelaPagaRepository;
    private final CompraParceladaRepository compraParceladaRepository;
    private static final Pattern FORMATO_DO_ID = Pattern.compile("^([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})-(\\d{4}-(?:0[1-9]|1[0-2]))$");

    @Cacheable("faturas")
    @Transactional(readOnly = true)
    public List<FaturaCartaoDTO> listar(UUID idCartao) {
        log.info("Listando as faturas... - ID do Cartão: [{}]", idCartao);
        var cartoes = cartaoRepository.findByTipo(TipoCartao.CREDITO)
                .stream()
                .filter(cartao -> idCartao == null || cartao.getId().equals(idCartao))
                .toList();

        return montarFaturas(cartoes, LocalDate.now(), null, pagamentosPorCompra())
                .stream()
                .sorted(Comparator.comparing(FaturaCartaoDTO::dataVencimento)
                        .thenComparing(FaturaCartaoDTO::nomeCartao, TextoUtil.ORDEM_ALFABETICA))
                .toList();
    }

    @Cacheable("faturas")
    @Transactional(readOnly = true)
    public DetalheFaturaDTO detalhar(String id) {
        log.info("Detalhando a fatura... - ID: [{}]", id);
        var hoje = LocalDate.now();
        var partes = partesDoId(id);
        var mes = YearMonth.parse(partes.group(2));
        var cartao = buscarCartao(partes, id);
        var pagamentos = pagamentosPorCompra();

        return faturaMapper.toDetalheDTO(buscarFatura(cartao, mes, hoje, id, pagamentos), itens(cartao, mes, hoje, pagamentos));
    }

    @Transactional
    @LimparCachesDeFatura
    public FaturaCartaoDTO registrarPagamento(String id) {
        log.info("Registrando o pagamento da fatura... - ID: [{}]", id);
        var hoje = LocalDate.now();
        var partes = partesDoId(id);
        var mes = YearMonth.parse(partes.group(2));
        var cartao = buscarCartao(partes, id);
        var pagamentos = pagamentosPorCompra();

        if (buscarFatura(cartao, mes, hoje, id, pagamentos).valorRestante().signum() == 0) {
            log.error("Essa fatura já está paga!");
            throw new FaturaJaPagaException("Essa fatura já está paga!");
        }

        var conta = contaDePagamento(cartao);
        var abertura = aberturaDoCiclo(cartao, mes);
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());

        var emAberto = comprasDaFatura(cartao, mes)
                .stream()
                .filter(compra -> !pagamentos.getOrDefault(compra.getId(), Map.of()).containsKey(numeroNoMes(compra.getPrimeiroMes(), mes)))
                .toList();

        var semCategoria = emAberto.stream().anyMatch(compra -> compra.getCategoria() == null) ? outrasDespesas() : null;

        emAberto.forEach(compra -> pagarParcela(compra, numeroNoMes(compra.getPrimeiroMes(), mes), hoje, compra.getCategoria() != null ? compra.getCategoria() : semCategoria));

        var despesasEmAberto = ValorUtil.zeroSeNulo(lancamentoRepository.somarDespesasDoCartao(cartao.getId(), abertura, fechamento))
                .subtract(ValorUtil.zeroSeNulo(lancamentoRepository.somarDespesasPagasDoCartao(cartao.getId(), abertura, fechamento)));

        lancamentoRepository.marcarDespesasDoCartaoComoPagas(cartao.getId(), abertura, fechamento, hoje, conta);
        conta.setSaldo(conta.getSaldo().subtract(despesasEmAberto));

        return buscarFatura(cartao, mes, hoje, id, pagamentosPorCompra());
    }

    @Transactional
    @LimparCachesDeFatura
    public void deletarPagamento(String id) {
        log.info("Deletando o pagamento da fatura... - ID: [{}]", id);
        var partes = partesDoId(id);
        var mes = YearMonth.parse(partes.group(2));
        var cartao = buscarCartao(partes, id);

        if (buscarFatura(cartao, mes, LocalDate.now(), id, pagamentosPorCompra()).valorPago().signum() == 0) {
            log.warn("Pagamento de fatura não encontrado! - ID: [{}]", id);
            throw new PagamentoDeFaturaNaoEncontradoException("Essa fatura não tem pagamento registrado!");
        }

        var abertura = aberturaDoCiclo(cartao, mes);
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());

        var numeros = comprasDaFatura(cartao, mes)
                .stream()
                .collect(Collectors.toMap(CompraParcelada::getId, compra -> (short) numeroNoMes(compra.getPrimeiroMes(), mes)));

        var pagamentosDaFatura = numeros.isEmpty() ? List.<ParcelaPaga>of() : parcelaPagaRepository.findByCompraParceladaIdIn(numeros.keySet())
                .stream()
                .filter(pagamento -> pagamento.getNumero().equals(numeros.get(pagamento.getCompraParcelada().getId())))
                .toList();

        desfazerPagamentos(pagamentosDaFatura);

        lancamentoRepository.buscarDespesasDoCartao(cartao.getId(), abertura, fechamento)
                .stream()
                .filter(despesa -> despesa.getContaPagamentoFatura() != null)
                .forEach(despesa -> {
                    var conta = despesa.getContaPagamentoFatura();
                    conta.setSaldo(conta.getSaldo().add(despesa.getValor()));
                });

        lancamentoRepository.desmarcarDespesasDoCartao(cartao.getId(), abertura, fechamento);
    }

    @Transactional
    public ParcelaPaga pagarParcela(CompraParcelada compra, int numero, LocalDate hoje) {
        return pagarParcela(compra, numero, hoje, compra.getCategoria() != null ? compra.getCategoria() : outrasDespesas());
    }

    @Transactional
    public void desfazerPagamentoDeParcela(ParcelaPaga pagamento) {
        desfazerPagamentos(List.of(pagamento));
    }

    @Transactional(readOnly = true)
    public FaturaDTO faturaEmDestaque(YearMonth mes, LocalDate hoje) {
        var faturas = montarFaturas(cartaoRepository.findByTipo(TipoCartao.CREDITO), hoje, mes, pagamentosPorCompra());
        var faturasDoMes = faturasDoMes(faturas, mes);

        if (!mes.equals(YearMonth.from(hoje)) || faturasDoMes.stream().anyMatch(fatura -> fatura.situacao() != SituacaoFatura.PAGA)) {
            return destaque(faturasDoMes);
        }

        var faturasDoProximoMes = faturasDoMes(faturas, mes.plusMonths(1));
        return faturasDoProximoMes.isEmpty() ? destaque(faturasDoMes) : destaque(faturasDoProximoMes);
    }

    @Transactional(readOnly = true)
    public List<FaturaCartaoDTO> faturasRecentes(List<Cartao> cartoes, LocalDate hoje) {
        return montarFaturas(cartoes, hoje, YearMonth.from(hoje).minusMonths(2), pagamentosPorCompra());
    }

    @Transactional(readOnly = true)
    public Map<UUID, BigDecimal> limitesComprometidos(List<Cartao> cartoes, LocalDate hoje) {
        return somarLimitesComprometidos(cartoes, faturasRecentes(cartoes, hoje));
    }

    public Map<UUID, BigDecimal> somarLimitesComprometidos(List<Cartao> cartoes, List<FaturaCartaoDTO> faturas) {
        var limites = cartoes.stream()
                .filter(cartao -> cartao.getTipo() == TipoCartao.CREDITO)
                .filter(FaturaService::temCicloDefinido)
                .collect(Collectors.toMap(Cartao::getId, cartao -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP), (primeiro, segundo) -> primeiro, HashMap::new));

        faturas.stream()
                .filter(fatura -> limites.containsKey(fatura.idCartao()))
                .filter(fatura -> fatura.situacao() != SituacaoFatura.VENCIDA)
                .forEach(fatura -> limites.merge(fatura.idCartao(), fatura.valorRestante(), BigDecimal::add));

        return limites;
    }

    public List<ParcelaDTO> cronograma(CompraParcelada compra, LocalDate hoje, Map<Integer, LocalDate> pagamentos) {
        var cartao = compra.getCartao();
        var cronograma = new ArrayList<ParcelaDTO>();

        for (var indice = 0; indice < compra.getParcelas(); indice++) {
            var mes = YearMonth.from(compra.getPrimeiroMes()).plusMonths(indice);
            var dataVencimento = temCicloDefinido(cartao) ? vencimento(cartao, mes) : mes.atEndOfMonth();
            var vencida = dataVencimento.isBefore(hoje);
            var registrada = pagamentos.containsKey(indice + 1);

            cronograma.add(new ParcelaDTO(
                    indice + 1,
                    mes.toString(),
                    dataVencimento,
                    valorDaParcela(compra.getValorTotal(), compra.getParcelas(), indice),
                    situacaoDaParcela(vencida || registrada, cronograma),
                    registrada && !vencida));
        }

        return cronograma;
    }

    @Transactional(readOnly = true)
    public Map<UUID, Map<Integer, LocalDate>> pagamentosPorCompra() {
        return parcelaPagaRepository.buscarPagamentos()
                .stream()
                .collect(Collectors.groupingBy(
                        ParcelaPagaProjecao::compraParceladaId,
                        Collectors.toMap(pagamento -> pagamento.numero().intValue(), ParcelaPagaProjecao::dataPagamento)));
    }

    @Transactional(readOnly = true)
    public NavigableMap<LocalDate, BigDecimal> parcelasPorDataDePagamento(LocalDate hoje) {
        var parcelas = new TreeMap<LocalDate, BigDecimal>();
        var pagamentos = pagamentosPorCompra();

        compraParceladaRepository.buscarComCartao().forEach(compra -> {
            var pagamentosDaCompra = pagamentos.getOrDefault(compra.getId(), Map.of());

            cronograma(compra, hoje, pagamentosDaCompra)
                    .forEach(parcela -> parcelas.merge(dataDePagamento(parcela, pagamentosDaCompra), parcela.valor(), BigDecimal::add));
        });

        return parcelas;
    }

    private ParcelaPaga pagarParcela(CompraParcelada compra, int numero, LocalDate hoje, Categoria categoria) {
        var conta = contaDePagamento(compra.getCartao());
        var valor = valorDaParcela(compra.getValorTotal(), compra.getParcelas(), numero - 1L);
        var pagamento = parcelaPagaRepository.save(parcelaPaga(compra, numero, hoje));
        var lancamento = new Lancamento();

        lancamento.setData(hoje);
        lancamento.setValor(valor);
        lancamento.setConta(conta);
        lancamento.setCategoria(categoria);
        lancamento.setParcelaPaga(pagamento);
        lancamento.setForma(FormaLancamento.CONTA);
        lancamento.setTipo(TipoLancamento.DESPESA);
        lancamento.setSituacao(SituacaoLancamento.PAGO);
        lancamento.setDescricao(descricaoDaParcela(compra, numero));

        lancamentoRepository.save(lancamento);
        conta.setSaldo(conta.getSaldo().subtract(valor));

        return pagamento;
    }

    private void desfazerPagamentos(List<ParcelaPaga> pagamentos) {
        if (pagamentos.isEmpty()) return;

        var lancamentos = lancamentoRepository.findByParcelaPagaIdIn(pagamentos.stream().map(ParcelaPaga::getId).toList());

        lancamentos.forEach(lancamento -> {
            var conta = lancamento.getConta();
            conta.setSaldo(conta.getSaldo().add(lancamento.getValor()));
        });

        lancamentoRepository.deleteAllInBatch(lancamentos);
        parcelaPagaRepository.deleteAllInBatch(pagamentos);
    }

    private List<FaturaCartaoDTO> montarFaturas(List<Cartao> cartoes, LocalDate hoje, YearMonth desde, Map<UUID, Map<Integer, LocalDate>> pagamentos) {
        var cartoesDeCredito = cartoes.stream()
                .filter(cartao -> cartao.getTipo() == TipoCartao.CREDITO)
                .filter(FaturaService::temCicloDefinido)
                .toList();

        if (cartoesDeCredito.isEmpty()) {
            return List.of();
        }

        var idsCartoes = cartoesDeCredito.stream().map(Cartao::getId).toList();
        var inicio = desde != null ? desde.minusMonths(1).atDay(1) : null;

        var despesas = lancamentoRepository.agruparDespesasDosCartoes(idsCartoes, inicio)
                .stream()
                .collect(Collectors.groupingBy(DespesaCartaoProjecao::cartaoId));

        var parcelas = compraParceladaRepository.buscarParcelasDosCartoes(idsCartoes)
                .stream()
                .collect(Collectors.groupingBy(ParcelaProjecao::cartaoId));

        return cartoesDeCredito.stream()
                .flatMap(cartao -> montarFaturasDoCartao(cartao, hoje, desde, despesas.getOrDefault(cartao.getId(), List.of()), parcelas.getOrDefault(cartao.getId(), List.of()), pagamentos).stream())
                .toList();
    }

    private static List<FaturaCartaoDTO> montarFaturasDoCartao(Cartao cartao, LocalDate hoje, YearMonth desde, List<DespesaCartaoProjecao> despesas, List<ParcelaProjecao> parcelas, Map<UUID, Map<Integer, LocalDate>> pagamentos) {
        var totais = new TreeMap<YearMonth, BigDecimal>();
        var pagos = new HashMap<YearMonth, BigDecimal>();
        var quantidades = new HashMap<YearMonth, Long>();

        despesas.forEach(despesa -> {
            var mes = mesDaFatura(cartao, despesa.data());
            totais.merge(mes, despesa.valor(), BigDecimal::add);
            pagos.merge(mes, ValorUtil.zeroSeNulo(despesa.valorPago()), BigDecimal::add);
            quantidades.merge(mes, despesa.quantidade(), Long::sum);
        });

        parcelas.forEach(parcela -> {
            var pagamentosDaCompra = pagamentos.getOrDefault(parcela.id(), Map.of());

            for (var indice = 0; indice < parcela.parcelas(); indice++) {
                var mes = YearMonth.from(parcela.primeiroMes()).plusMonths(indice);

                if (desde != null && mes.isBefore(desde)) continue;

                var valor = valorDaParcela(parcela.valorTotal(), parcela.parcelas(), indice);

                totais.merge(mes, valor, BigDecimal::add);
                quantidades.merge(mes, 1L, Long::sum);

                if (pagamentosDaCompra.containsKey(indice + 1)) {
                    pagos.merge(mes, valor, BigDecimal::add);
                }
            }
        });

        var faturas = new ArrayList<FaturaCartaoDTO>();
        BigDecimal totalAnterior = null;

        for (var fatura : (desde != null ? totais.tailMap(desde, true) : totais).entrySet()) {
            var mes = fatura.getKey();
            var montada = montarFatura(cartao, mes, fatura.getValue(), pagos.getOrDefault(mes, BigDecimal.ZERO), quantidades.get(mes), totalAnterior, hoje);

            faturas.add(montada);
            totalAnterior = montada.total();
        }

        return faturas;
    }

    private static FaturaCartaoDTO montarFatura(Cartao cartao, YearMonth mes, BigDecimal total, BigDecimal pago, Long quantidadeItens, BigDecimal totalAnterior, LocalDate hoje) {
        var fechamento = diaDoMes(mes, cartao.getDiaFechamento());
        var vencimento = vencimento(cartao, mes);
        var valorTotal = ValorUtil.dinheiro(total);
        var valorPago = ValorUtil.dinheiro(pago);

        return new FaturaCartaoDTO(
                "%s-%s".formatted(cartao.getId(), mes),
                cartao.getId(),
                cartao.getNome(),
                mes.toString(),
                valorTotal,
                valorPago,
                valorTotal.subtract(valorPago),
                situacao(aberturaDoCiclo(cartao, mes), fechamento, vencimento, hoje, quitada(valorTotal, valorPago)),
                fechamento,
                vencimento,
                quantidadeItens.intValue(),
                totalAnterior);
    }

    private FaturaCartaoDTO buscarFatura(Cartao cartao, YearMonth mes, LocalDate hoje, String id, Map<UUID, Map<Integer, LocalDate>> pagamentos) {
        var recentes = montarFaturas(List.of(cartao), hoje, mes.minusMonths(1), pagamentos);
        var temAnterior = recentes.stream().anyMatch(fatura -> fatura.mes().equals(mes.minusMonths(1).toString()));

        return (temAnterior ? recentes : montarFaturas(List.of(cartao), hoje, null, pagamentos))
                .stream()
                .filter(montada -> montada.mes().equals(mes.toString()))
                .findFirst()
                .orElseThrow(() -> faturaNaoEncontrada(id));
    }

    private List<FaturaDTO> faturasDoMes(List<FaturaCartaoDTO> faturas, YearMonth mes) {
        return faturas.stream()
                .filter(fatura -> fatura.mes().equals(mes.toString()))
                .filter(fatura -> fatura.total().signum() > 0)
                .map(faturaMapper::toResumoDTO)
                .toList();
    }

    private static FaturaDTO destaque(List<FaturaDTO> faturas) {
        return faturas.stream()
                .max(Comparator.comparing((FaturaDTO fatura) -> fatura.situacao() == SituacaoFatura.ABERTA)
                        .thenComparing(FaturaDTO::total))
                .orElse(null);
    }

    private Cartao buscarCartao(Matcher partes, String id) {
        return cartaoRepository.findById(UUID.fromString(partes.group(1)))
                .filter(encontrado -> encontrado.getTipo() == TipoCartao.CREDITO)
                .filter(FaturaService::temCicloDefinido)
                .orElseThrow(() -> faturaNaoEncontrada(id));
    }

    private List<CompraParcelada> comprasDaFatura(Cartao cartao, YearMonth mes) {
        return compraParceladaRepository.buscarDoCartaoAte(cartao.getId(), mes.atDay(1))
                .stream()
                .filter(compra -> alcancaOMes(compra.getPrimeiroMes(), compra.getParcelas(), mes))
                .toList();
    }

    private List<ItemFaturaDTO> itens(Cartao cartao, YearMonth mes, LocalDate hoje, Map<UUID, Map<Integer, LocalDate>> pagamentos) {
        var despesas = lancamentoRepository.buscarDespesasDoCartao(cartao.getId(), aberturaDoCiclo(cartao, mes), diaDoMes(mes, cartao.getDiaFechamento()))
                .stream()
                .map(faturaMapper::toItemDTO);

        var parcelas = comprasDaFatura(cartao, mes)
                .stream()
                .map(compra -> itemDaParcela(compra, mes, hoje, pagamentos.getOrDefault(compra.getId(), Map.of())));

        return Stream.concat(despesas, parcelas)
                .sorted(Comparator.comparing(ItemFaturaDTO::data).reversed()
                        .thenComparing(ItemFaturaDTO::descricao, TextoUtil.ORDEM_ALFABETICA))
                .toList();
    }

    private ItemFaturaDTO itemDaParcela(CompraParcelada compra, YearMonth mes, LocalDate hoje, Map<Integer, LocalDate> pagamentos) {
        var numero = numeroNoMes(compra.getPrimeiroMes(), mes);
        var parcela = cronograma(compra, hoje, pagamentos).get(numero - 1);

        var parcelaDoItem = new ParcelaItemFaturaDTO(
                parcela.numero(),
                compra.getParcelas().intValue(),
                compra.getId(),
                parcela.situacao(),
                parcela.pagamentoAntecipado());

        return faturaMapper.toItemParceladoDTO(compra, parcelaDoItem, parcela.valor(), pagamentos.containsKey(numero));
    }

    private Categoria outrasDespesas() {
        return categoriaRepository.findByNomeAndTipo("Outras despesas", TipoCategoria.DESPESA)
                .orElseThrow(() -> {
                    log.error("A compra parcelada não tem categoria: escolha uma antes de registrar o pagamento!");
                    return new CategoriaInexistenteException("A compra parcelada não tem categoria: escolha uma antes de registrar o pagamento!");
                });
    }

    private static Conta contaDePagamento(Cartao cartao) {
        if (cartao.getConta() == null) {
            log.error("O cartão não tem conta de pagamento! - ID: [{}]", cartao.getId());
            throw new CartaoSemContaDePagamentoException("Escolha a conta de pagamento do cartão %s antes de registrar o pagamento!".formatted(cartao.getNome()));
        }

        return cartao.getConta();
    }

    private static String descricaoDaParcela(CompraParcelada compra, int numero) {
        var sufixo = " (%d/%d)".formatted(numero, compra.getParcelas());
        var limite = 160 - sufixo.length();
        var descricao = compra.getDescricao();

        return (descricao.length() > limite ? descricao.substring(0, limite).strip() : descricao) + sufixo;
    }

    private static ParcelaPaga parcelaPaga(CompraParcelada compra, int numero, LocalDate dataPagamento) {
        var parcelaPaga = new ParcelaPaga();

        parcelaPaga.setCompraParcelada(compra);
        parcelaPaga.setNumero((short) numero);
        parcelaPaga.setDataPagamento(dataPagamento);

        return parcelaPaga;
    }

    private static int numeroNoMes(LocalDate primeiroMes, YearMonth mes) {
        return (int) YearMonth.from(primeiroMes).until(mes, ChronoUnit.MONTHS) + 1;
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

    private static SituacaoParcela situacaoDaParcela(boolean paga, List<ParcelaDTO> anteriores) {
        if (paga) return SituacaoParcela.PAGA;

        var atualJaDefinida = anteriores.stream().anyMatch(parcela -> parcela.situacao() != SituacaoParcela.PAGA);
        return atualJaDefinida ? SituacaoParcela.FUTURA : SituacaoParcela.ATUAL;
    }

    private static LocalDate dataDePagamento(ParcelaDTO parcela, Map<Integer, LocalDate> pagamentos) {
        var registrada = pagamentos.get(parcela.numero());
        return registrada != null && registrada.isBefore(parcela.dataVencimento()) ? registrada : parcela.dataVencimento();
    }

    private static boolean quitada(BigDecimal valorTotal, BigDecimal valorPago) {
        return valorPago.signum() > 0 && valorPago.compareTo(valorTotal) >= 0;
    }

    private static SituacaoFatura situacao(LocalDate abertura, LocalDate fechamento, LocalDate vencimento, LocalDate hoje, boolean quitada) {
        if (hoje.isBefore(abertura)) return SituacaoFatura.FUTURA;
        if (!hoje.isAfter(fechamento)) return SituacaoFatura.ABERTA;
        if (quitada) return SituacaoFatura.PAGA;
        if (!hoje.isAfter(vencimento)) return SituacaoFatura.FECHADA;
        return SituacaoFatura.VENCIDA;
    }

    private static LocalDate aberturaDoCiclo(Cartao cartao, YearMonth mes) {
        return diaDoMes(mes.minusMonths(1), cartao.getDiaFechamento()).plusDays(1);
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

    private static Matcher partesDoId(String id) {
        var partes = FORMATO_DO_ID.matcher(id);

        if (!partes.matches()) {
            throw faturaNaoEncontrada(id);
        }

        return partes;
    }

    private static FaturaNaoEncontradaException faturaNaoEncontrada(String id) {
        log.warn("Fatura não encontrada! - ID: [{}]", id);
        return new FaturaNaoEncontradaException("Fatura não encontrada!");
    }
}