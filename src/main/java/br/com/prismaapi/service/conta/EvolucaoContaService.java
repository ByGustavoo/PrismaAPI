package br.com.prismaapi.service.conta;

import br.com.prismaapi.enums.FinalidadeConta;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.enums.TipoMovimentacaoConta;
import br.com.prismaapi.exceptions.ContaNaoEncontradaException;
import br.com.prismaapi.model.dto.conta.ContaDTO;
import br.com.prismaapi.model.dto.conta.EfeitoNaConta;
import br.com.prismaapi.model.dto.conta.EvolucaoContaDTO;
import br.com.prismaapi.model.dto.conta.MovimentacaoContaDTO;
import br.com.prismaapi.model.dto.investimento.PontoEvolucaoDTO;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.conta.ContaMapper;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EvolucaoContaService {

    private final ContaMapper contaMapper;
    private final ContaService contaService;
    private final ContaRepository contaRepository;
    private final LancamentoRepository lancamentoRepository;
    private static final Comparator<Lancamento> ORDEM_CRONOLOGICA = Comparator.comparing(Lancamento::getData).thenComparing(Lancamento::getDataCriacao);

    @Transactional(readOnly = true)
    public List<EvolucaoContaDTO> listarReservas() {
        log.info("Listando as reservas...");
        var reservas = contaService.listar()
                .stream()
                .filter(conta -> conta.tipo().getFinalidade() == FinalidadeConta.RESERVA)
                .toList();

        if (reservas.isEmpty()) {
            return List.of();
        }

        var hoje = LocalDate.now();
        var lancamentos = buscarLancamentos(reservas.stream().map(ContaDTO::id).toList(), hoje);

        return reservas.stream()
                .map(conta -> evoluir(conta, lancamentos, hoje))
                .toList();
    }

    @Transactional(readOnly = true)
    public EvolucaoContaDTO buscarEvolucao(UUID id) {
        log.info("Buscando a evolução da conta... - ID: [{}]", id);
        var conta = contaRepository.findById(id)
                .map(contaMapper::toDTO)
                .orElseThrow(() -> {
                    log.warn("Conta não encontrada! - ID: [{}]", id);
                    return new ContaNaoEncontradaException("Conta não encontrada!");
                });

        var hoje = LocalDate.now();

        return evoluir(conta, buscarLancamentos(List.of(id), hoje), hoje);
    }

    private List<Lancamento> buscarLancamentos(List<UUID> idsContas, LocalDate hoje) {
        return lancamentoRepository.buscarPagosDasContas(idsContas, inicioDaJanela(hoje).atDay(1), hoje)
                .stream()
                .sorted(ORDEM_CRONOLOGICA)
                .toList();
    }

    private static EvolucaoContaDTO evoluir(ContaDTO conta, List<Lancamento> lancamentos, LocalDate hoje) {
        var efeitos = lancamentos.stream()
                .filter(lancamento -> tocaAConta(lancamento, conta.id()))
                .map(lancamento -> new EfeitoNaConta(lancamento, tipo(lancamento, conta.id())))
                .toList();

        var aportes = somar(efeitos, TipoMovimentacaoConta.APORTE);
        var resgates = somar(efeitos, TipoMovimentacaoConta.RESGATE);
        var rendimentos = somar(efeitos, TipoMovimentacaoConta.RENDIMENTO);

        var saldoAtual = dinheiro(conta.saldo());
        var saldoInicial = saldoAtual.subtract(aportes).add(resgates).subtract(rendimentos);
        var aplicado = saldoInicial.add(aportes).subtract(resgates);

        return new EvolucaoContaDTO(
                conta,
                conta.tipo().getFinalidade(),
                inicioDaJanela(hoje).atDay(1),
                saldoInicial,
                aportes,
                resgates,
                rendimentos,
                saldoAtual,
                aplicado.signum() > 0 ? rendimentos.divide(aplicado, 4, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP),
                pontos(efeitos, saldoInicial, hoje),
                movimentacoes(efeitos, saldoInicial));
    }

    private static List<PontoEvolucaoDTO> pontos(List<EfeitoNaConta> efeitos, BigDecimal saldoInicial, LocalDate hoje) {
        var mesAtual = YearMonth.from(hoje);

        return Stream.iterate(inicioDaJanela(hoje), mes -> !mes.isAfter(mesAtual), mes -> mes.plusMonths(1))
                .map(mes -> {
                    var fechamento = mes.equals(mesAtual) ? hoje : mes.atEndOfMonth();
                    var ate = efeitos.stream().filter(efeito -> !efeito.lancamento().getData().isAfter(fechamento)).toList();
                    var aplicado = saldoInicial.add(somar(ate, TipoMovimentacaoConta.APORTE)).subtract(somar(ate, TipoMovimentacaoConta.RESGATE));

                    return new PontoEvolucaoDTO(MesDoAno.rotulo(mes), mes, aplicado, aplicado.add(somar(ate, TipoMovimentacaoConta.RENDIMENTO)));
                })
                .toList();
    }

    private static List<MovimentacaoContaDTO> movimentacoes(List<EfeitoNaConta> efeitos, BigDecimal saldoInicial) {
        var saldo = saldoInicial;
        var movimentacoes = new ArrayList<MovimentacaoContaDTO>();

        for (var efeito : efeitos) {
            var lancamento = efeito.lancamento();
            var valor = dinheiro(lancamento.getValor());

            saldo = efeito.tipo() == TipoMovimentacaoConta.RESGATE ? saldo.subtract(valor) : saldo.add(valor);
            movimentacoes.add(new MovimentacaoContaDTO(lancamento.getId(), efeito.tipo(), lancamento.getData(), lancamento.getDescricao(), valor, saldo));
        }

        return movimentacoes.reversed();
    }

    private static boolean tocaAConta(Lancamento lancamento, UUID idConta) {
        var naOrigem = lancamento.getConta() != null && lancamento.getConta().getId().equals(idConta);
        var noDestino = lancamento.getContaDestino() != null && lancamento.getContaDestino().getId().equals(idConta);

        return naOrigem || noDestino;
    }

    private static TipoMovimentacaoConta tipo(Lancamento lancamento, UUID idConta) {
        if (lancamento.getTipo() == TipoLancamento.TRANSFERENCIA) {
            return lancamento.getContaDestino().getId().equals(idConta) ? TipoMovimentacaoConta.APORTE : TipoMovimentacaoConta.RESGATE;
        }

        if (lancamento.getTipo() == TipoLancamento.DESPESA) {
            return TipoMovimentacaoConta.RESGATE;
        }

        var categoria = lancamento.getCategoria();
        var rendimento = categoria != null && categoria.getTipo() == TipoCategoria.RECEITA && "Rendimentos".equalsIgnoreCase(categoria.getNome());

        return rendimento ? TipoMovimentacaoConta.RENDIMENTO : TipoMovimentacaoConta.APORTE;
    }

    private static BigDecimal somar(List<EfeitoNaConta> efeitos, TipoMovimentacaoConta tipo) {
        return dinheiro(efeitos.stream()
                .filter(efeito -> efeito.tipo() == tipo)
                .map(efeito -> efeito.lancamento().getValor())
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static YearMonth inicioDaJanela(LocalDate hoje) {
        return YearMonth.from(hoje).minusMonths(11);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}