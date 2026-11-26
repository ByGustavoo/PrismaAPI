package br.com.prismaapi.service.aviso;

import br.com.prismaapi.enums.EstadoAviso;
import br.com.prismaapi.enums.SeveridadeAviso;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.SituacaoOrcamento;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoRecorrente;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.aviso.PreferenciasAvisosDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.dto.orcamento.ConsumoOrcamentoDTO;
import br.com.prismaapi.model.dto.recorrente.DespesasLancadasPorMes;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.metapreco.MetaPrecoRepository;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import br.com.prismaapi.service.orcamento.OrcamentoService;
import br.com.prismaapi.service.previsao.PrevisaoService;
import br.com.prismaapi.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class DerivacaoAvisosService {

    private final FaturaService faturaService;
    private final PrevisaoService previsaoService;
    private final CartaoRepository cartaoRepository;
    private final OrcamentoService orcamentoService;
    private final MetaPrecoRepository metaPrecoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final RecorrenteRepository recorrenteRepository;
    private final InvestimentoRepository investimentoRepository;
    private final PreferenciasAvisosService preferenciasAvisosService;
    private static final BigDecimal LIMITE_CRITICO = new BigDecimal("0.9");
    private static final DateTimeFormatter NOME_DO_MES = DateTimeFormatter.ofPattern("MMMM", TextoUtil.PT_BR);

    @Cacheable("avisos")
    @Transactional(readOnly = true)
    public List<AvisoDTO> derivar(LocalDate hoje) {
        var preferencias = preferenciasAvisosService.buscar();
        var desligados = Set.copyOf(preferencias.tiposDesligados());
        var avisos = new ArrayList<AvisoDTO>();

        if (!desligados.contains(TipoAviso.FATURA_VENCENDO) || !desligados.contains(TipoAviso.LIMITE_CARTAO)) {
            var cartoes = cartaoRepository.findByTipo(TipoCartao.CREDITO);
            var faturas = faturaService.faturasRecentes(cartoes, hoje);

            if (!desligados.contains(TipoAviso.FATURA_VENCENDO)) avisos.addAll(faturasVencendo(faturas, hoje, preferencias));
            if (!desligados.contains(TipoAviso.LIMITE_CARTAO)) avisos.addAll(cartoesPertoDoLimite(cartoes, faturas, hoje, preferencias));
        }

        avisos.addAll(lancamentosEmAberto(hoje, preferencias));
        if (!desligados.contains(TipoAviso.RECORRENTE_VENCENDO)) avisos.addAll(recorrentesVencendo(hoje, preferencias));
        if (!desligados.contains(TipoAviso.ORCAMENTO_NO_LIMITE)) avisos.addAll(orcamentosNoLimite(hoje));
        if (!desligados.contains(TipoAviso.SALDO_NEGATIVO_PREVISTO)) saldoNegativoPrevisto(hoje).ifPresent(avisos::add);
        if (!desligados.contains(TipoAviso.INVESTIMENTO_DESATUALIZADO)) avisos.addAll(investimentosSemAtualizacao(hoje, preferencias));
        if (!desligados.contains(TipoAviso.META_SEM_PRECO)) avisos.addAll(metasSemPreco(hoje, preferencias));

        return avisos.stream()
                .filter(aviso -> !desligados.contains(aviso.tipo()))
                .sorted(Comparator.comparing((AvisoDTO aviso) -> aviso.severidade().getPrioridade())
                        .thenComparing(AvisoDTO::data)
                        .thenComparing(AvisoDTO::titulo, TextoUtil.ORDEM_ALFABETICA))
                .toList();
    }

    private static List<AvisoDTO> faturasVencendo(List<FaturaCartaoDTO> faturas, LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        return faturas.stream()
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

    private List<AvisoDTO> cartoesPertoDoLimite(List<Cartao> cartoes, List<FaturaCartaoDTO> faturas, LocalDate hoje, PreferenciasAvisosDTO preferencias) {
        var inicioDoAlerta = BigDecimal.valueOf(preferencias.percentualLimiteCartao()).movePointLeft(2);
        var limitesComprometidos = faturaService.somarLimitesComprometidos(cartoes, faturas);

        return cartoes.stream()
                .filter(cartao -> cartao.getLimiteCredito() != null && cartao.getLimiteCredito().signum() > 0 && limitesComprometidos.get(cartao.getId()) != null)
                .filter(cartao -> limitesComprometidos.get(cartao.getId()).compareTo(cartao.getLimiteCredito().multiply(inicioDoAlerta)) >= 0)
                .map(cartao -> avisoDoLimite(cartao, limitesComprometidos, hoje))
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
        var lancadas = DespesasLancadasPorMes.de(lancamentoRepository.buscarComOrigemEntre(YearMonth.from(hoje).atDay(1), YearMonth.from(limite).atEndOfMonth()));

        return recorrenteRepository.findBySituacao(SituacaoRecorrente.ATIVO)
                .stream()
                .filter(despesa -> despesa.getTipo() == TipoRecorrente.DESPESA)
                .filter(despesa -> !Boolean.TRUE.equals(despesa.getLancarAutomaticamente()))
                .map(despesa -> avisoDaRecorrente(despesa, proximoVencimento(despesa, hoje), lancadas, hoje))
                .flatMap(Optional::stream)
                .filter(aviso -> !aviso.data().isAfter(limite))
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
                        "Sem atualização " + prazo(hoje, investimento.getDataUltimaMovimentacao()) + (investimento.getQuantidade() == null ? " — Confira o saldo em " + investimento.getInstituicao() : " — Confira a cotação de " + investimento.getCodigo()),
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

    private static AvisoDTO avisoDoLimite(Cartao cartao, Map<UUID, BigDecimal> limitesComprometidos, LocalDate hoje) {
        var limite = cartao.getLimiteCredito();
        var comprometido = limitesComprometidos.get(cartao.getId());

        return novo(
                "aviso-cartao-" + cartao.getId(),
                TipoAviso.LIMITE_CARTAO,
                comprometido.compareTo(limite.multiply(LIMITE_CRITICO)) >= 0 ? SeveridadeAviso.CRITICO : SeveridadeAviso.ATENCAO,
                cartao.getNome() + " perto do limite",
                descricaoDoLimite(limite, comprometido),
                hoje,
                null,
                "/cartoes");
    }

    private static Optional<AvisoDTO> avisoDaRecorrente(Recorrente despesa, LocalDate vencimento, DespesasLancadasPorMes lancadas, LocalDate hoje) {
        if (lancadas.contem(despesa, YearMonth.from(vencimento))) return Optional.empty();

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

    private static String descricaoDaFatura(FaturaCartaoDTO fatura, LocalDate hoje) {
        return switch (fatura.situacao()) {
            case ABERTA -> "Aberta até " + fatura.dataFechamento().format(DateTimeFormatter.ofPattern("dd/MM")) + " — " + vencimentoEm(hoje, fatura.dataVencimento());
            case VENCIDA -> "Sem pagamento registrado — " + vencimentoEm(hoje, fatura.dataVencimento());
            default -> "Fechada — " + vencimentoEm(hoje, fatura.dataVencimento());
        };
    }

    private static String descricaoDoLimite(BigDecimal limite, BigDecimal comprometido) {
        var livre = limite.subtract(comprometido);

        var folga = livre.signum() >= 0
                ? reais(livre) + " livres"
                : reais(livre.negate()) + " acima do limite";

        return percentual(comprometido, limite) + "% do limite em uso, com as parcelas futuras — " + folga;
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
        var formato = NumberFormat.getIntegerInstance(TextoUtil.PT_BR);
        formato.setRoundingMode(RoundingMode.HALF_UP);

        return "R$ " + formato.format(valor);
    }

    private static LocalDate proximoVencimento(Recorrente despesa, LocalDate hoje) {
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
}