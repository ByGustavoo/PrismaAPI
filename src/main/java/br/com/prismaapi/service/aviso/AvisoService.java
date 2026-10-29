package br.com.prismaapi.service.aviso;

import br.com.prismaapi.enums.EstadoAviso;
import br.com.prismaapi.enums.SeveridadeAviso;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.SituacaoOrcamento;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.AvisoNaoEncontradoException;
import br.com.prismaapi.model.dto.aviso.AdiarAvisoDTO;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.dto.orcamento.ConsumoOrcamentoDTO;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.leituraaviso.LeituraAviso;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.leituraaviso.LeituraAvisoRepository;
import br.com.prismaapi.repository.metapreco.MetaPrecoRepository;
import br.com.prismaapi.service.cartao.CartaoService;
import br.com.prismaapi.service.fatura.FaturaService;
import br.com.prismaapi.service.orcamento.OrcamentoService;
import br.com.prismaapi.service.previsao.PrevisaoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvisoService {

    private final CartaoService cartaoService;
    private final FaturaService faturaService;
    private final PrevisaoService previsaoService;
    private final OrcamentoService orcamentoService;
    private final MetaPrecoRepository metaPrecoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final LeituraAvisoRepository leituraAvisoRepository;
    private final InvestimentoRepository investimentoRepository;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private final PreferenciasAvisosService preferenciasAvisosService;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;
    private static final BigDecimal LIMITE_CRITICO = new BigDecimal("0.9");
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(PT_BR);
    private static final DateTimeFormatter NOME_DO_MES = DateTimeFormatter.ofPattern("MMMM", PT_BR);
    private static final Set<TipoAviso> DATADOS_HOJE = Set.of(TipoAviso.LIMITE_CARTAO, TipoAviso.ORCAMENTO_NO_LIMITE);

    @Cacheable("avisos")
    @Transactional(readOnly = true)
    public List<AvisoDTO> listar() {
        log.info("Listando os avisos... - Data: {}", LocalDate.now());
        var hoje = LocalDate.now();
        var leituras = leiturasPorChave();

        return derivar(hoje).stream()
                .map(aviso -> comEstado(aviso, leituras.get(aviso.id()), hoje))
                .toList();
    }

    @Transactional
    @CacheEvict(value = "avisos", allEntries = true)
    public AvisoDTO registrarLeitura(String id) {
        log.info("Marcando o aviso como lido... - ID: {}", id);
        var aviso = encontrar(id);
        var leitura = gravar(aviso, null);

        return comEstado(aviso, leitura, LocalDate.now());
    }

    @Transactional
    @CacheEvict(value = "avisos", allEntries = true)
    public AvisoDTO registrarAdiamento(String id, AdiarAvisoDTO adiarAvisoDTO) {
        log.info("Adiando o aviso... - ID: {} - Dias: {}", id, adiarAvisoDTO.dias());
        var hoje = LocalDate.now();
        var aviso = encontrar(id);
        var leitura = gravar(aviso, hoje.plusDays(adiarAvisoDTO.dias()));

        return comEstado(aviso, leitura, hoje);
    }

    @Transactional
    @CacheEvict(value = "avisos", allEntries = true)
    public void deletarLeitura(String id) {
        log.info("Devolvendo o aviso aos novos... - ID: {}", id);
        leituraAvisoRepository.findByChave(id).ifPresent(leituraAvisoRepository::delete);
    }

    @Transactional
    @CacheEvict(value = "avisos", allEntries = true)
    public List<AvisoDTO> registrarLeituras() {
        log.info("Marcando todos os avisos novos como lidos...");
        var hoje = LocalDate.now();
        var avisos = derivar(hoje);
        var leituras = leiturasPorChave();

        var resultado = avisos.stream()
                .map(aviso -> {
                    var atual = comEstado(aviso, leituras.get(aviso.id()), hoje);
                    return atual.estado() == EstadoAviso.NOVO ? comEstado(aviso, gravar(aviso, null), hoje) : atual;
                })
                .toList();

        limparLeiturasAntigas(avisos);

        return resultado;
    }

    @Transactional
    @CacheEvict(value = "avisos", allEntries = true)
    public void deletarLeituras() {
        log.info("Devolvendo todos os avisos aos novos...");
        leituraAvisoRepository.deleteAllInBatch();
    }

    private List<AvisoDTO> derivar(LocalDate hoje) {
        var preferencias = preferenciasAvisosService.buscar();
        var desligados = Set.copyOf(preferencias.tiposDesligados());
        var avisos = new ArrayList<AvisoDTO>();

        if (!desligados.contains(TipoAviso.FATURA_VENCENDO)) avisos.addAll(faturasVencendo(hoje, preferencias));
        avisos.addAll(lancamentosEmAberto(hoje, preferencias));
        if (!desligados.contains(TipoAviso.RECORRENTE_VENCENDO)) avisos.addAll(recorrentesVencendo(hoje, preferencias));
        if (!desligados.contains(TipoAviso.LIMITE_CARTAO)) avisos.addAll(cartoesPertoDoLimite(hoje, preferencias));
        if (!desligados.contains(TipoAviso.ORCAMENTO_NO_LIMITE)) avisos.addAll(orcamentosNoLimite(hoje));
        if (!desligados.contains(TipoAviso.SALDO_NEGATIVO_PREVISTO)) saldoNegativoPrevisto(hoje).ifPresent(avisos::add);
        if (!desligados.contains(TipoAviso.INVESTIMENTO_DESATUALIZADO)) avisos.addAll(investimentosSemAtualizacao(hoje, preferencias));
        if (!desligados.contains(TipoAviso.META_SEM_PRECO)) avisos.addAll(metasSemPreco(hoje, preferencias));

        return avisos.stream()
                .filter(aviso -> !desligados.contains(aviso.tipo()))
                .sorted(Comparator.comparing((AvisoDTO aviso) -> aviso.severidade().getPrioridade())
                        .thenComparing(AvisoDTO::data)
                        .thenComparing(AvisoDTO::titulo, ORDEM_ALFABETICA))
                .toList();
    }

    private Map<String, LeituraAviso> leiturasPorChave() {
        return leituraAvisoRepository.findAll()
                .stream()
                .collect(Collectors.toMap(LeituraAviso::getChave, Function.identity()));
    }

    private AvisoDTO encontrar(String id) {
        return derivar(LocalDate.now()).stream()
                .filter(aviso -> aviso.id().equals(id))
                .findFirst()
                .orElseThrow(() -> new AvisoNaoEncontradoException("Este aviso já não existe: a situação que o gerou foi resolvida!"));
    }

    private LeituraAviso gravar(AvisoDTO aviso, LocalDate fimDoAdiamento) {
        var leitura = leituraAvisoRepository.findByChave(aviso.id()).orElseGet(LeituraAviso::new);

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

    private List<AvisoDTO> faturasVencendo(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        return faturaService.listar(null)
                .stream()
                .filter(fatura -> fatura.situacao() != SituacaoFatura.PAGA && fatura.situacao() != SituacaoFatura.FUTURA)
                .filter(fatura -> fatura.valorRestante().signum() > 0)
                .filter(fatura -> !fatura.dataVencimento().isAfter(hoje.plusDays(preferencias.diasAntecedencia())))
                .filter(fatura -> !fatura.dataVencimento().isBefore(hoje.minusDays(15)))
                .map(fatura -> novo(
                        "aviso-fatura-" + fatura.id(),
                        TipoAviso.FATURA_VENCENDO,
                        severidadePorPrazo(hoje, fatura.dataVencimento()),
                        "Fatura do " + fatura.nomeCartao(),
                        descricaoDaFatura(fatura, hoje),
                        fatura.dataVencimento(),
                        fatura.valorRestante(),
                        "/faturas?fatura=" + fatura.id()))
                .toList();
    }

    private List<AvisoDTO> lancamentosEmAberto(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        return lancamentoRepository.buscarNaoPagosAte(hoje.plusDays(preferencias.diasAntecedencia()))
                .stream()
                .map(lancamento -> avisoDoLancamento(lancamento, hoje))
                .toList();
    }

    private List<AvisoDTO> recorrentesVencendo(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        var limite = hoje.plusDays(preferencias.diasAntecedencia());
        var lancamentos = lancamentoRepository.buscarComOrigemEntre(YearMonth.from(hoje).atDay(1), YearMonth.from(limite).atEndOfMonth());

        return despesaRecorrenteRepository.findBySituacao(SituacaoDespesaRecorrente.ATIVO)
                .stream()
                .map(despesa -> avisoDaRecorrente(despesa, proximoVencimento(despesa, hoje), lancamentos, hoje))
                .flatMap(Optional::stream)
                .filter(aviso -> !aviso.data().isAfter(limite))
                .toList();
    }

    private List<AvisoDTO> cartoesPertoDoLimite(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        var inicioDoAlerta = BigDecimal.valueOf(preferencias.percentualLimiteCartao()).movePointLeft(2);

        return cartaoService.listar()
                .stream()
                .filter(cartao -> cartao.tipo() == TipoCartao.CREDITO)
                .filter(cartao -> cartao.limiteCredito() != null && cartao.limiteCredito().signum() > 0 && cartao.limiteComprometido() != null)
                .filter(cartao -> cartao.limiteComprometido().compareTo(cartao.limiteCredito().multiply(inicioDoAlerta)) >= 0)
                .map(cartao -> novo(
                        "aviso-cartao-" + cartao.id(),
                        TipoAviso.LIMITE_CARTAO,
                        cartao.limiteComprometido().compareTo(cartao.limiteCredito().multiply(LIMITE_CRITICO)) >= 0 ? SeveridadeAviso.CRITICO : SeveridadeAviso.ATENCAO,
                        cartao.nome() + " perto do limite",
                        descricaoDoLimite(cartao),
                        hoje,
                        null,
                        "/cartoes"))
                .toList();
    }

    private List<AvisoDTO> orcamentosNoLimite(LocalDate hoje) {
        var mes = YearMonth.from(hoje);

        return orcamentoService.visaoGeral(mes)
                .itens()
                .stream()
                .filter(item -> item.situacao() != SituacaoOrcamento.SEGURO)
                .map(item -> novo(
                        "aviso-orcamento-" + item.orcamento().id() + "-" + mes,
                        TipoAviso.ORCAMENTO_NO_LIMITE,
                        item.situacao() == SituacaoOrcamento.ESTOURADO ? SeveridadeAviso.CRITICO : SeveridadeAviso.ATENCAO,
                        "Orçamento de " + item.orcamento().categoria().nome(),
                        descricaoDoOrcamento(item, mes),
                        hoje,
                        null,
                        "/planejamento/orcamento"))
                .toList();
    }

    private Optional<AvisoDTO> saldoNegativoPrevisto(LocalDate hoje) {
        var previsao = previsaoService.prever(null);
        var mesAtual = YearMonth.from(hoje);

        return Stream.concat(Stream.of(previsao.restanteMesAtual()), previsao.meses().stream())
                .filter(mes -> mes.saldoFinal().signum() < 0)
                .findFirst()
                .map(mes -> novo(
                        "aviso-saldo-negativo-previsto",
                        TipoAviso.SALDO_NEGATIVO_PREVISTO,
                        mes.mes().isAfter(mesAtual.plusMonths(1)) ? SeveridadeAviso.ATENCAO : SeveridadeAviso.CRITICO,
                        "Saldo previsto negativo",
                        "Pela previsão, o saldo fica " + reais(mes.saldoFinal().negate()) + " abaixo de zero no fim de " + nomeDoMes(mes.mes(), mesAtual),
                        mes.mes().atEndOfMonth(),
                        null,
                        "/planejamento/previsao"));
    }

    private List<AvisoDTO> investimentosSemAtualizacao(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        return investimentoRepository.findByDataUltimaMovimentacaoLessThanEqual(hoje.minusDays(preferencias.diasLembreteInvestimento()))
                .stream()
                .map(investimento -> novo(
                        "aviso-investimento-" + investimento.getId(),
                        TipoAviso.INVESTIMENTO_DESATUALIZADO,
                        SeveridadeAviso.ATENCAO,
                        investimento.getNome(),
                        "Sem atualização " + prazo(hoje, investimento.getDataUltimaMovimentacao()) + " — Confira o saldo em " + investimento.getInstituicao(),
                        investimento.getDataUltimaMovimentacao(),
                        null,
                        "/investimentos?investimento=" + investimento.getId()))
                .toList();
    }

    private List<AvisoDTO> metasSemPreco(LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        return metaPrecoRepository.buscarAcompanhadasSemPrecoDesde(hoje.minusDays(preferencias.diasLembreteMeta()))
                .stream()
                .map(meta -> novo(
                        "aviso-meta-" + meta.idMeta(),
                        TipoAviso.META_SEM_PRECO,
                        SeveridadeAviso.ATENCAO,
                        meta.nome(),
                        "Sem preço novo " + prazo(hoje, meta.dataUltimoPreco()) + " — Registre o valor de hoje",
                        meta.dataUltimoPreco(),
                        null,
                        "/planejamento/metas?meta=" + meta.idMeta()))
                .toList();
    }

    private static Optional<AvisoDTO> avisoDaRecorrente(DespesaRecorrente despesa, LocalDate vencimento, List<Lancamento> lancamentos, LocalDate hoje) {
        var descricao = normalizar(despesa.getDescricao());
        var jaLancada = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.DESPESA)
                .filter(lancamento -> YearMonth.from(lancamento.getData()).equals(YearMonth.from(vencimento)))
                .anyMatch(lancamento -> normalizar(lancamento.getDescricao()).equals(descricao));

        if (jaLancada) return Optional.empty();

        return Optional.of(novo(
                "aviso-recorrente-" + despesa.getId(),
                TipoAviso.RECORRENTE_VENCENDO,
                severidadePorPrazo(hoje, vencimento),
                despesa.getDescricao(),
                "Despesa recorrente — " + vencimentoEm(hoje, vencimento),
                vencimento,
                despesa.getValor(),
                "/planejamento/recorrentes"));
    }

    private static AvisoDTO avisoDoLancamento(Lancamento lancamento, LocalDate hoje) {
        var data = lancamento.getData();
        var categoria = lancamento.getCategoria() != null ? lancamento.getCategoria().getNome() : null;

        var tipo = switch (lancamento.getTipo()) {
            case RECEITA -> TipoAviso.RECEITA_PREVISTA;
            case TRANSFERENCIA -> TipoAviso.LANCAMENTO_AGENDADO;
            case DESPESA -> lancamento.getSituacao() == SituacaoLancamento.PENDENTE ? TipoAviso.CONTA_VENCENDO : TipoAviso.LANCAMENTO_AGENDADO;
        };

        var descricao = switch (lancamento.getTipo()) {
            case RECEITA -> categoria + " — A receber " + prazo(hoje, data);
            case TRANSFERENCIA -> "Transferência para " + lancamento.getContaDestino().getNome() + " — " + maiuscula(prazo(hoje, data));
            case DESPESA -> tipo == TipoAviso.CONTA_VENCENDO
                    ? categoria + " — " + vencimentoEm(hoje, data)
                    : categoria + " — Débito agendado " + prazo(hoje, data);
        };

        return novo(
                "aviso-lancamento-" + lancamento.getId(),
                tipo,
                tipo == TipoAviso.CONTA_VENCENDO ? severidadePorPrazo(hoje, data) : SeveridadeAviso.INFO,
                lancamento.getDescricao(),
                descricao,
                data,
                lancamento.getValor(),
                "/lancamentos");
    }

    private static AvisoDTO novo(String id, TipoAviso tipo, SeveridadeAviso severidade, String titulo, String descricao, LocalDate data, BigDecimal valor, String rota) {
        return new AvisoDTO(id, tipo, severidade, titulo, descricao, data, valor, rota, EstadoAviso.NOVO, null);
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

    private static String descricaoDaFatura(FaturaCartaoDTO fatura, LocalDate hoje) {
        return switch (fatura.situacao()) {
            case ABERTA -> "Aberta até " + fatura.dataFechamento().format(DateTimeFormatter.ofPattern("dd/MM")) + " — " + vencimentoEm(hoje, fatura.dataVencimento());
            case VENCIDA -> "Sem pagamento registrado — " + vencimentoEm(hoje, fatura.dataVencimento());
            default -> "Fechada — " + vencimentoEm(hoje, fatura.dataVencimento());
        };
    }

    private static String descricaoDoLimite(CartaoDTO cartao) {
        var livre = cartao.limiteCredito().subtract(cartao.limiteComprometido());

        var folga = livre.signum() >= 0
                ? reais(livre) + " livres"
                : reais(livre.negate()) + " acima do limite";

        return percentual(cartao.limiteComprometido(), cartao.limiteCredito()) + "% do limite em uso, com as parcelas futuras — " + folga;
    }

    private static String descricaoDoOrcamento(ConsumoOrcamentoDTO item, YearMonth mes) {
        var limite = item.orcamento().limiteMensal();
        var mesDoLimite = mes.format(NOME_DO_MES);

        if (item.situacao() == SituacaoOrcamento.ESTOURADO) {
            return "Passou do limite de " + mesDoLimite + " em " + reais(item.gasto().subtract(limite)) + " — " + reais(item.gasto()) + " de " + reais(limite);
        }

        return percentual(item.gasto(), limite) + "% do limite de " + mesDoLimite + " — " + reais(limite.subtract(item.gasto())) + " livres";
    }

    private static String nomeDoMes(YearMonth mes, YearMonth mesAtual) {
        return mes.getYear() == mesAtual.getYear() ? mes.format(NOME_DO_MES) : mes.format(NOME_DO_MES) + " de " + mes.getYear();
    }

    private static String reais(BigDecimal valor) {
        var formato = NumberFormat.getIntegerInstance(PT_BR);
        formato.setRoundingMode(RoundingMode.HALF_UP);

        return "R$ " + formato.format(valor);
    }

    private static LocalDate proximoVencimento(DespesaRecorrente despesa, LocalDate hoje) {
        var vencimento = despesa.getProximoVencimento();

        while (vencimento.isBefore(hoje)) {
            vencimento = despesa.getFrequencia().proximaOcorrencia(vencimento);
        }

        return vencimento;
    }

    private static BigDecimal percentual(BigDecimal parte, BigDecimal total) {
        return parte
                .multiply(BigDecimal.valueOf(100))
                .divide(total, 0, RoundingMode.HALF_UP);
    }

    private static SeveridadeAviso severidadePorPrazo(LocalDate hoje, LocalDate data) {
        var dias = ChronoUnit.DAYS.between(hoje, data);

        if (dias <= 2) return SeveridadeAviso.CRITICO;

        return dias <= 7 ? SeveridadeAviso.ATENCAO : SeveridadeAviso.INFO;
    }

    private static String prazo(LocalDate hoje, LocalDate data) {
        var dias = ChronoUnit.DAYS.between(hoje, data);

        if (dias < 0) return "há %d %s".formatted(-dias, dias == -1 ? "dia" : "dias");
        if (dias == 0) return "hoje";
        if (dias == 1) return "amanhã";

        return "em %d dias".formatted(dias);
    }

    private static String vencimentoEm(LocalDate hoje, LocalDate data) {
        return data.isBefore(hoje) ? "Venceu " + prazo(hoje, data) : "Vence " + prazo(hoje, data);
    }

    private static String maiuscula(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String normalizar(String descricao) {
        return descricao.strip().toLowerCase(Locale.ROOT);
    }
}