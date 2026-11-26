package br.com.prismaapi.service.saldo;

import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import br.com.prismaapi.util.ValorUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SaldoService {

    private final FaturaService faturaService;
    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;

    @Transactional(readOnly = true)
    public LinhaDoSaldo linhaDoSaldo(Collection<LocalDate> datas, LocalDate hoje) {
        var inicio = Stream.concat(datas.stream(), Stream.of(hoje)).min(Comparator.naturalOrder()).orElseThrow();
        var fim = Stream.concat(datas.stream(), Stream.of(hoje)).max(Comparator.naturalOrder()).orElseThrow();

        var movimentos = new TreeMap<LocalDate, BigDecimal>();

        lancamentoRepository.agruparMovimentosDoTotalPorDia(inicio, fim, hoje)
                .forEach(movimento -> movimentos.merge(movimento.data(), movimento.valor(), BigDecimal::add));

        lancamentoRepository.agruparPagamentosDeFaturaDoTotalPorDia(inicio, hoje)
                .forEach(pagamento -> movimentos.merge(pagamento.data(), pagamento.valor().negate(), BigDecimal::add));

        faturaService.parcelasPorDataDePagamento(hoje)
                .subMap(hoje, false, fim, true)
                .forEach((vencimento, valor) -> movimentos.merge(vencimento, valor.negate(), BigDecimal::add));

        return new LinhaDoSaldo(hoje, ValorUtil.zeroSeNulo(contaRepository.somarSaldoDoTotal()), movimentos);
    }
}