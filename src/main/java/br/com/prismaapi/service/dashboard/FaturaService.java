package br.com.prismaapi.service.dashboard;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.SituacaoFatura;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.dto.dashboard.fatura.FaturaDTO;
import br.com.prismaapi.model.dto.dashboard.projection.ParcelaProjecao;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FaturaService {

    private static final String SEM_CARTAO = "Nenhum cartão";

    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompraParceladaRepository compraParceladaRepository;

    @Transactional(readOnly = true)
    public FaturaDTO faturaEmDestaque(YearMonth mes, LocalDate hoje) {
        var cartoes = cartaoRepository.findByTipoAndSituacao(TipoCartao.CREDITO, Situacao.ATIVO);
        var parcelas = compraParceladaRepository.buscarParcelasAte(mes.atDay(1));

        return cartoes.stream()
                .filter(FaturaService::temCicloDefinido)
                .map(cartao -> montar(cartao, mes, hoje, parcelas))
                .filter(fatura -> fatura.total().signum() > 0)
                .max(Comparator.comparing((FaturaDTO fatura) -> fatura.situacao() == SituacaoFatura.ABERTA)
                        .thenComparing(FaturaDTO::total))
                .orElseGet(() -> semCartaoMovimentado(mes, hoje));
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
                .filter(parcela -> alcancaOMes(parcela, mes))
                .map(parcela -> parcela.valorTotal()
                        .divide(BigDecimal.valueOf(parcela.parcelas()), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static boolean alcancaOMes(ParcelaProjecao parcela, YearMonth mes) {
        var ultimaParcela = YearMonth.from(parcela.primeiroMes()).plusMonths(parcela.parcelas() - 1L);
        return !ultimaParcela.isBefore(mes);
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

    private static FaturaDTO semCartaoMovimentado(YearMonth mes, LocalDate hoje) {
        var situacao = mes.isBefore(YearMonth.from(hoje)) ? SituacaoFatura.PAGA : SituacaoFatura.ABERTA;

        return new FaturaDTO(
                BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP),
                SEM_CARTAO,
                mes.plusMonths(1).atEndOfMonth().toString(),
                situacao);
    }

    private static BigDecimal zeroSeNulo(BigDecimal valor) {
        return valor != null ? valor : BigDecimal.ZERO;
    }
}