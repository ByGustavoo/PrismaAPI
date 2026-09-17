package br.com.prismaapi.service.aviso;

import br.com.prismaapi.enums.SeveridadeAviso;
import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.fatura.FaturaCartaoDTO;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.cartao.CartaoService;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AvisoService {

    private final CartaoService cartaoService;
    private final FaturaService faturaService;
    private static final int DIAS_DE_ANTECEDENCIA = 15;
    private final LancamentoRepository lancamentoRepository;
    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;
    private static final BigDecimal LIMITE_CRITICO = new BigDecimal("0.9");
    private static final BigDecimal LIMITE_EM_ATENCAO = new BigDecimal("0.7");
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(PT_BR);

    @Transactional(readOnly = true)
    public List<AvisoDTO> listar() {
        log.info("Listando os avisos... - Data: {}", LocalDate.now());
        var hoje = LocalDate.now();
        var avisos = new ArrayList<AvisoDTO>();

        avisos.addAll(faturasVencendo(hoje));
        avisos.addAll(lancamentosEmAberto(hoje));
        avisos.addAll(recorrentesVencendo(hoje));
        avisos.addAll(cartoesPertoDoLimite(hoje));

        return avisos.stream()
                .sorted(Comparator.comparing((AvisoDTO aviso) -> aviso.severidade().getPrioridade())
                        .thenComparing(AvisoDTO::data)
                        .thenComparing(AvisoDTO::titulo, ORDEM_ALFABETICA))
                .toList();
    }

    private List<AvisoDTO> faturasVencendo(LocalDate hoje) {
        return faturaService.listar(null)
                .stream()
                .filter(fatura -> fatura.situacao() != SituacaoFatura.PAGA && fatura.situacao() != SituacaoFatura.FUTURA)
                .filter(fatura -> fatura.total().signum() > 0)
                .filter(fatura -> !fatura.dataVencimento().isAfter(hoje.plusDays(DIAS_DE_ANTECEDENCIA)))
                .filter(fatura -> !fatura.dataVencimento().isBefore(hoje.minusDays(DIAS_DE_ANTECEDENCIA)))
                .map(fatura -> new AvisoDTO(
                        "aviso-fatura-" + fatura.id(),
                        TipoAviso.FATURA_VENCENDO,
                        severidadePorPrazo(hoje, fatura.dataVencimento()),
                        "Fatura do " + fatura.nomeCartao(),
                        descricaoDaFatura(fatura, hoje),
                        fatura.dataVencimento(),
                        fatura.total(),
                        "/faturas"))
                .toList();
    }

    private List<AvisoDTO> lancamentosEmAberto(LocalDate hoje) {
        return lancamentoRepository.buscarNaoPagosAte(hoje.plusDays(DIAS_DE_ANTECEDENCIA))
                .stream()
                .map(lancamento -> avisoDoLancamento(lancamento, hoje))
                .toList();
    }

    private List<AvisoDTO> recorrentesVencendo(LocalDate hoje) {
        var limite = hoje.plusDays(7);
        var lancamentos = lancamentoRepository.buscarComOrigemEntre(YearMonth.from(hoje).atDay(1), YearMonth.from(limite).atEndOfMonth());

        return despesaRecorrenteRepository.findBySituacao(SituacaoDespesaRecorrente.ATIVO)
                .stream()
                .map(despesa -> avisoDaRecorrente(despesa, proximoVencimento(despesa, hoje), lancamentos, hoje))
                .flatMap(Optional::stream)
                .filter(aviso -> !aviso.data().isAfter(limite))
                .toList();
    }

    private List<AvisoDTO> cartoesPertoDoLimite(LocalDate hoje) {
        return cartaoService.listar()
                .stream()
                .filter(cartao -> cartao.tipo() == TipoCartao.CREDITO)
                .filter(cartao -> cartao.limiteCredito() != null && cartao.limiteCredito().signum() > 0 && cartao.limiteComprometido() != null)
                .filter(cartao -> cartao.limiteComprometido().compareTo(cartao.limiteCredito().multiply(LIMITE_EM_ATENCAO)) >= 0)
                .map(cartao -> new AvisoDTO(
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

    private static Optional<AvisoDTO> avisoDaRecorrente(DespesaRecorrente despesa, LocalDate vencimento, List<Lancamento> lancamentos, LocalDate hoje) {
        var descricao = normalizar(despesa.getDescricao());
        var jaLancada = lancamentos.stream()
                .filter(lancamento -> lancamento.getTipo() == TipoLancamento.DESPESA)
                .filter(lancamento -> YearMonth.from(lancamento.getData()).equals(YearMonth.from(vencimento)))
                .anyMatch(lancamento -> normalizar(lancamento.getDescricao()).equals(descricao));

        if (jaLancada) return Optional.empty();

        return Optional.of(new AvisoDTO(
                "aviso-recorrente-" + despesa.getId(),
                TipoAviso.RECORRENTE_VENCENDO,
                severidadePorPrazo(hoje, vencimento),
                despesa.getDescricao(),
                "Despesa recorrente · " + vencimentoEm(hoje, vencimento),
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
            case RECEITA -> categoria + " · a receber " + prazo(hoje, data);
            case TRANSFERENCIA -> "Transferência para " + lancamento.getContaDestino().getNome() + " · " + prazo(hoje, data);
            case DESPESA -> tipo == TipoAviso.CONTA_VENCENDO
                    ? categoria + " · " + vencimentoEm(hoje, data)
                    : categoria + " · débito agendado " + prazo(hoje, data);
        };

        return new AvisoDTO(
                "aviso-lancamento-" + lancamento.getId(),
                tipo,
                tipo == TipoAviso.CONTA_VENCENDO ? severidadePorPrazo(hoje, data) : SeveridadeAviso.INFO,
                lancamento.getDescricao(),
                descricao,
                data,
                lancamento.getValor(),
                "/lancamentos");
    }

    private static String descricaoDaFatura(FaturaCartaoDTO fatura, LocalDate hoje) {
        return switch (fatura.situacao()) {
            case ABERTA -> "Aberta até " + fatura.dataFechamento().format(DateTimeFormatter.ofPattern("dd/MM")) + " · " + vencimentoEm(hoje, fatura.dataVencimento());
            case VENCIDA -> "Sem pagamento registrado · " + vencimentoEm(hoje, fatura.dataVencimento());
            default -> "Fechada · " + vencimentoEm(hoje, fatura.dataVencimento());
        };
    }

    private static String descricaoDoLimite(CartaoDTO cartao) {
        var livre = cartao.limiteCredito().subtract(cartao.limiteComprometido());
        var reais = NumberFormat.getIntegerInstance(PT_BR);
        reais.setRoundingMode(RoundingMode.HALF_UP);

        var folga = livre.signum() >= 0
                ? "R$ " + reais.format(livre) + " livres"
                : "R$ " + reais.format(livre.negate()) + " acima do limite";

        return percentualUtilizado(cartao) + "% do limite em uso, com as parcelas futuras · " + folga;
    }

    private static LocalDate proximoVencimento(DespesaRecorrente despesa, LocalDate hoje) {
        var vencimento = despesa.getProximoVencimento();

        while (vencimento.isBefore(hoje)) {
            vencimento = despesa.getFrequencia().proximaOcorrencia(vencimento);
        }

        return vencimento;
    }

    private static BigDecimal percentualUtilizado(CartaoDTO cartao) {
        return cartao.limiteComprometido()
                .multiply(BigDecimal.valueOf(100))
                .divide(cartao.limiteCredito(), 0, RoundingMode.HALF_UP);
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
        return data.isBefore(hoje) ? "venceu " + prazo(hoje, data) : "vence " + prazo(hoje, data);
    }

    private static String normalizar(String descricao) {
        return descricao.strip().toLowerCase(Locale.ROOT);
    }
}