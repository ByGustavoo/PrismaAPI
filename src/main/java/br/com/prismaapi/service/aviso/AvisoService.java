package br.com.prismaapi.service.aviso;

import br.com.prismaapi.enums.SeveridadeAviso;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoAviso;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.dto.aviso.AvisoDTO;
import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.cartao.CartaoService;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AvisoService {

    private final CartaoService cartaoService;
    private final FaturaService faturaService;
    private static final int DIAS_DE_ANTECEDENCIA = 15;
    private final LancamentoRepository lancamentoRepository;
    private static final BigDecimal LIMITE_CRITICO = new BigDecimal("0.9");
    private static final BigDecimal LIMITE_EM_ATENCAO = new BigDecimal("0.7");
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Transactional(readOnly = true)
    public List<AvisoDTO> listar() {
        var hoje = LocalDate.now();
        var avisos = new ArrayList<AvisoDTO>();

        avisos.addAll(faturasVencendo(hoje));
        avisos.addAll(lancamentosEmAberto(hoje));
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
                .filter(fatura -> fatura.situacao() == SituacaoFatura.ABERTA || fatura.situacao() == SituacaoFatura.FECHADA)
                .filter(fatura -> !fatura.dataVencimento().isAfter(hoje.plusDays(DIAS_DE_ANTECEDENCIA)))
                .map(fatura -> new AvisoDTO(
                        "aviso-fatura-" + fatura.id(),
                        TipoAviso.FATURA_VENCENDO,
                        severidadePorPrazo(hoje, fatura.dataVencimento()),
                        "Fatura " + fatura.nomeCartao(),
                        "Fatura " + prazo(hoje, fatura.dataVencimento()),
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
                        percentualUtilizado(cartao) + "% do limite utilizado",
                        hoje,
                        cartao.limiteCredito().subtract(cartao.limiteComprometido()),
                        "/cartoes"))
                .toList();
    }

    private static AvisoDTO avisoDoLancamento(Lancamento lancamento, LocalDate hoje) {
        var pendente = lancamento.getSituacao() == SituacaoLancamento.PENDENTE;
        var prazo = prazo(hoje, lancamento.getData());
        var classificacao = lancamento.getCategoria() != null ? lancamento.getCategoria().getNome() : "Transferência";

        return new AvisoDTO(
                "aviso-lancamento-" + lancamento.getId(),
                pendente ? TipoAviso.CONTA_VENCENDO : TipoAviso.LANCAMENTO_AGENDADO,
                pendente ? severidadePorPrazo(hoje, lancamento.getData()) : SeveridadeAviso.INFO,
                lancamento.getDescricao(),
                pendente ? classificacao + " · " + prazo : "Agendado · " + prazo,
                lancamento.getData(),
                lancamento.getValor(),
                "/lancamentos");
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

        if (dias < 0) return "venceu há %d %s".formatted(-dias, dias == -1 ? "dia" : "dias");
        if (dias == 0) return "vence hoje";
        if (dias == 1) return "vence amanhã";

        return "vence em %d dias".formatted(dias);
    }
}