package br.com.prismaapi.service.saldo;

import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.dto.dashboard.LinhaDoSaldo;
import br.com.prismaapi.model.dto.dashboard.projection.MovimentoDiarioProjecao;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
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

    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;

    @Transactional(readOnly = true)
    public LinhaDoSaldo linhaDoSaldo(Collection<LocalDate> datas, LocalDate hoje) {
        var inicio = Stream.concat(datas.stream(), Stream.of(hoje)).min(Comparator.naturalOrder()).orElseThrow();
        var fim = Stream.concat(datas.stream(), Stream.of(hoje)).max(Comparator.naturalOrder()).orElseThrow();

        var movimentos = new TreeMap<LocalDate, BigDecimal>();

        lancamentoRepository.agruparMovimentoDoTotalPorDia(inicio, fim)
                .forEach(movimento -> movimentos.merge(movimento.data(), comSinal(movimento), BigDecimal::add));

        lancamentoRepository.agruparTransferenciasQueSaemDoTotal(inicio, fim)
                .forEach(saida -> movimentos.merge(saida.data(), saida.valor().negate(), BigDecimal::add));

        lancamentoRepository.agruparTransferenciasQueEntramNoTotal(inicio, fim)
                .forEach(entrada -> movimentos.merge(entrada.data(), entrada.valor(), BigDecimal::add));

        var saldoDeHoje = contaRepository.somarSaldoDoTotal();

        return new LinhaDoSaldo(hoje, saldoDeHoje != null ? saldoDeHoje : BigDecimal.ZERO, movimentos);
    }

    private static BigDecimal comSinal(MovimentoDiarioProjecao movimento) {
        return movimento.tipo() == TipoLancamento.RECEITA ? movimento.valor() : movimento.valor().negate();
    }
}