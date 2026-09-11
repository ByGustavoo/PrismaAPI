package br.com.prismaapi.service.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.exceptions.InvestimentoNaoEncontradoException;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.investimento.AlocacaoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.EvolucaoCarteiraDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.PosicaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.entity.investimento.Investimento;
import br.com.prismaapi.model.mapper.investimento.InvestimentoMapper;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestimentoService {

    private static final int CASAS_DA_FRACAO = 4;
    private final InvestimentoMapper investimentoMapper;
    private final InvestimentoRepository investimentoRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final Comparator<Investimento> MAIOR_VALOR_PRIMEIRO = Comparator.comparing(Investimento::getValorAtual, Comparator.reverseOrder()).thenComparing(Investimento::getNome, ORDEM_ALFABETICA);

    @Transactional(readOnly = true)
    public CarteiraDTO resumirCarteira() {
        log.info("Resumindo a carteira de investimentos...");
        var investimentos = investimentoRepository.findAll();

        var aportado = somar(investimentos, Investimento::getAportado);
        var valorAtual = somar(investimentos, Investimento::getValorAtual);
        var rendimento = valorAtual.subtract(aportado);

        var historico = historico(investimentos, YearMonth.now());
        var valorDoMesAnterior = historico.get(historico.size() - 2).valor();

        return new CarteiraDTO(
                aportado,
                valorAtual,
                rendimento,
                fracao(rendimento, aportado),
                VariacaoDTO.entre(valorAtual, valorDoMesAnterior),
                alocacao(investimentos, valorAtual),
                historico,
                posicoes(investimentos, valorAtual));
    }

    @Transactional(readOnly = true)
    public List<EvolucaoCarteiraDTO> evolucaoNosMeses(List<YearMonth> meses) {
        var investimentos = investimentoRepository.findAll();
        var mesAtual = YearMonth.now();

        return meses.stream()
                .map(mes -> evolucao(investimentos, mes, mesAtual))
                .toList();
    }

    @Transactional
    public InvestimentoDTO salvar(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        log.info("Salvando o investimento... - Nome: {}", salvarInvestimentoDTO.nome());
        var investimento = investimentoMapper.toEntity(salvarInvestimentoDTO);
        preencher(investimento, salvarInvestimentoDTO);

        return investimentoMapper.toDTO(investimentoRepository.save(investimento));
    }

    @Transactional
    public InvestimentoDTO atualizar(UUID id, SalvarInvestimentoDTO salvarInvestimentoDTO) {
        log.info("Atualizando o investimento... - ID: [{}]", id);
        var investimento = buscar(id);

        investimentoMapper.updateEntity(salvarInvestimentoDTO, investimento);
        preencher(investimento, salvarInvestimentoDTO);

        return investimentoMapper.toDTO(investimento);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando o investimento... - ID: [{}]", id);
        investimentoRepository.delete(buscar(id));
    }

    private Investimento buscar(UUID id) {
        return investimentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Investimento não encontrado! - ID: [{}]", id);
                    return new InvestimentoNaoEncontradoException("Investimento não encontrado!");
                });
    }

    private static List<EvolucaoCarteiraDTO> historico(List<Investimento> investimentos, YearMonth mesAtual) {
        return Stream.iterate(mesAtual.minusMonths(11), mes -> !mes.isAfter(mesAtual), mes -> mes.plusMonths(1))
                .map(mes -> evolucao(investimentos, mes, mesAtual))
                .toList();
    }

    private static EvolucaoCarteiraDTO evolucao(List<Investimento> investimentos, YearMonth mes, YearMonth mesAtual) {
        var aportado = BigDecimal.ZERO;
        var valor = BigDecimal.ZERO;

        for (var investimento : investimentos) {
            var progresso = progresso(investimento, mes, mesAtual);
            var aportadoNoMes = investimento.getAportado().multiply(progresso);
            var rendimentoNoMes = investimento.getValorAtual().subtract(investimento.getAportado()).multiply(progresso).multiply(progresso);

            aportado = aportado.add(aportadoNoMes);
            valor = valor.add(aportadoNoMes).add(rendimentoNoMes);
        }

        return new EvolucaoCarteiraDTO(MesDoAno.rotulo(mes), mes, dinheiro(aportado), dinheiro(valor));
    }

    private static BigDecimal progresso(Investimento investimento, YearMonth mes, YearMonth mesAtual) {
        var inicio = YearMonth.from(investimento.getDataInicio());

        if (mes.isBefore(inicio)) {
            return BigDecimal.ZERO;
        }

        var mesesDecorridos = inicio.until(mes, ChronoUnit.MONTHS) + 1;
        var mesesDeVida = inicio.until(mesAtual, ChronoUnit.MONTHS) + 1;

        if (mesesDecorridos >= mesesDeVida) {
            return BigDecimal.ONE;
        }

        return BigDecimal.valueOf(mesesDecorridos).divide(BigDecimal.valueOf(mesesDeVida), 10, RoundingMode.HALF_UP);
    }

    private static List<AlocacaoDTO> alocacao(List<Investimento> investimentos, BigDecimal valorDaCarteira) {
        return investimentos.stream()
                .collect(Collectors.groupingBy(Investimento::getClasseAtivo, () -> new EnumMap<>(ClasseAtivo.class), Collectors.toList()))
                .entrySet()
                .stream()
                .map(classe -> {
                    var aportado = somar(classe.getValue(), Investimento::getAportado);
                    var valorAtual = somar(classe.getValue(), Investimento::getValorAtual);

                    return new AlocacaoDTO(
                            classe.getKey(),
                            aportado,
                            valorAtual,
                            valorAtual.subtract(aportado),
                            fracao(valorAtual, valorDaCarteira),
                            classe.getValue().size());
                })
                .sorted(Comparator.comparing(AlocacaoDTO::valorAtual, Comparator.reverseOrder()))
                .toList();
    }

    private List<PosicaoDTO> posicoes(List<Investimento> investimentos, BigDecimal valorDaCarteira) {
        return investimentos.stream()
                .sorted(MAIOR_VALOR_PRIMEIRO)
                .map(investimento -> {
                    var rendimento = investimento.getValorAtual().subtract(investimento.getAportado());

                    return new PosicaoDTO(
                            investimentoMapper.toDTO(investimento),
                            rendimento,
                            fracao(rendimento, investimento.getAportado()),
                            fracao(investimento.getValorAtual(), valorDaCarteira));
                })
                .toList();
    }

    private static void preencher(Investimento investimento, SalvarInvestimentoDTO salvarInvestimentoDTO) {
        investimento.setNome(salvarInvestimentoDTO.nome().strip());
        investimento.setInstituicao(salvarInvestimentoDTO.instituicao().strip());
        investimento.setObservacoes(textoOuNulo(salvarInvestimentoDTO.observacoes()));
    }

    private static BigDecimal somar(List<Investimento> investimentos, Function<Investimento, BigDecimal> valor) {
        return dinheiro(investimentos.stream()
                .map(valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private static BigDecimal fracao(BigDecimal parte, BigDecimal total) {
        if (total.signum() == 0) {
            return BigDecimal.ZERO.setScale(CASAS_DA_FRACAO, RoundingMode.HALF_UP);
        }

        return parte.divide(total, CASAS_DA_FRACAO, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinheiro(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}