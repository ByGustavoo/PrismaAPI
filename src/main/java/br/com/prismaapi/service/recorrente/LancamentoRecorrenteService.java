package br.com.prismaapi.service.recorrente;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoRecorrenteService {

    private final RecorrenteRepository recorrenteRepository;
    private final LancamentoRepository lancamentoRepository;

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "faturas", "lancamentos", "orcamentos", "previsao", "recorrentes", "relatorios"}, allEntries = true)
    public int lancarVencidas() {
        log.info("Lançando as recorrentes que venceram...");
        var hoje = LocalDate.now();

        return recorrenteRepository.buscarComOrigemECategoria()
                .stream()
                .mapToInt(recorrente -> lancar(recorrente, hoje))
                .sum();
    }

    public int lancar(Recorrente recorrente, LocalDate hoje) {
        if (!lancaSozinha(recorrente) || recorrente.getProximoVencimento().isAfter(hoje)) return 0;

        var frequencia = recorrente.getFrequencia();
        var conta = contaDoLancamento(recorrente);
        var cartao = cartaoDoLancamento(recorrente);
        var existentes = lancamentoRepository.buscarComOrigemEntre(recorrente.getProximoVencimento().minusMonths(1), hoje.plusMonths(1));

        var lancadas = 0;
        var vencimento = recorrente.getProximoVencimento();

        for (; !vencimento.isAfter(hoje); vencimento = frequencia.proximaOcorrencia(vencimento)) {
            if (!origemAtiva(conta, cartao) || jaLancada(recorrente, conta, cartao, vencimento, existentes)) continue;

            lancamentoRepository.save(novoLancamento(recorrente, conta, cartao, vencimento));
            lancadas++;
        }

        recorrente.setProximoVencimento(vencimento);

        if (lancadas > 0) {
            log.info("Recorrente lançada! - Descrição: {} - Tipo: {} - Lançamentos: {}", recorrente.getDescricao(), recorrente.getTipo(), lancadas);
        }

        return lancadas;
    }

    public static boolean lancaSozinha(Recorrente recorrente) {
        return recorrente.getSituacao() == SituacaoRecorrente.ATIVO
                && Boolean.TRUE.equals(recorrente.getLancarAutomaticamente())
                && recorrente.getCategoria() != null;
    }

    private static Lancamento novoLancamento(Recorrente recorrente, Conta conta, Cartao cartao, LocalDate vencimento) {
        var lancamento = new Lancamento();
        var tipo = tipoDoLancamento(recorrente);

        lancamento.setTipo(tipo);
        lancamento.setConta(conta);
        lancamento.setCartao(cartao);
        lancamento.setData(vencimento);
        lancamento.setRecorrente(recorrente);
        lancamento.setValor(recorrente.getValor());
        lancamento.setCategoria(recorrente.getCategoria());
        lancamento.setDescricao(recorrente.getDescricao());
        lancamento.setSituacao(SituacaoLancamento.PAGO);
        lancamento.setForma(cartao != null ? FormaLancamento.CARTAO_CREDITO : FormaLancamento.CONTA);

        var efeito = tipo == TipoLancamento.RECEITA ? recorrente.getValor() : recorrente.getValor().negate();

        if (conta != null) {
            conta.setSaldo(conta.getSaldo().add(efeito));
        }

        if (cartao != null && cartao.getSaldo() != null) {
            cartao.setSaldo(cartao.getSaldo().add(efeito));
        }

        return lancamento;
    }

    private static boolean jaLancada(Recorrente recorrente, Conta conta, Cartao cartao, LocalDate vencimento, List<Lancamento> existentes) {
        var tipo = tipoDoLancamento(recorrente);
        var descricao = normalizar(recorrente.getDescricao());

        return existentes.stream()
                .filter(lancamento -> lancamento.getTipo() == tipo)
                .filter(lancamento -> mesmaOrigem(lancamento, conta, cartao))
                .filter(lancamento -> mesmaOcorrencia(recorrente.getFrequencia(), lancamento.getData(), vencimento))
                .anyMatch(lancamento -> lancamento.getRecorrente() != null
                        ? lancamento.getRecorrente().getId().equals(recorrente.getId())
                        : normalizar(lancamento.getDescricao()).equals(descricao));
    }

    private static TipoLancamento tipoDoLancamento(Recorrente recorrente) {
        return TipoLancamento.valueOf(recorrente.getTipo().name());
    }

    private static boolean mesmaOrigem(Lancamento lancamento, Conta conta, Cartao cartao) {
        if (conta != null) return lancamento.getConta() != null && Objects.equals(lancamento.getConta().getId(), conta.getId());
        return lancamento.getCartao() != null && Objects.equals(lancamento.getCartao().getId(), cartao.getId());
    }

    private static boolean mesmaOcorrencia(Frequencia frequencia, LocalDate data, LocalDate vencimento) {
        if (frequencia == Frequencia.SEMANAL || frequencia == Frequencia.QUINZENAL) {
            return Math.abs(ChronoUnit.DAYS.between(data, vencimento)) <= 3;
        }

        return YearMonth.from(data).equals(YearMonth.from(vencimento));
    }

    private static Conta contaDoLancamento(Recorrente recorrente) {
        if (recorrente.getConta() != null) return recorrente.getConta();
        return recorrente.getCartao().getTipo() == TipoCartao.DEBITO ? recorrente.getCartao().getConta() : null;
    }

    private static Cartao cartaoDoLancamento(Recorrente recorrente) {
        var cartao = recorrente.getCartao();
        return cartao != null && cartao.getTipo() != TipoCartao.DEBITO ? cartao : null;
    }

    private static boolean origemAtiva(Conta conta, Cartao cartao) {
        if (conta != null) return conta.getSituacao() == Situacao.ATIVO;
        return cartao != null && cartao.getSituacao() == Situacao.ATIVO;
    }

    private static String normalizar(String descricao) {
        return descricao.strip().toLowerCase(Locale.ROOT);
    }
}