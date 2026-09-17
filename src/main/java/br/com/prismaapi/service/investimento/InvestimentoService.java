package br.com.prismaapi.service.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.TipoMovimentacaoInvestimento;
import br.com.prismaapi.exceptions.DataAnteriorAUltimaAtualizacaoException;
import br.com.prismaapi.exceptions.InvestimentoNaoEncontradoException;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.investimento.AlocacaoDTO;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.ExtratoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.MovimentacaoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.PontoEvolucaoDTO;
import br.com.prismaapi.model.dto.investimento.PosicaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.model.entity.investimento.Investimento;
import br.com.prismaapi.model.entity.movimentacaoinvestimento.MovimentacaoInvestimento;
import br.com.prismaapi.model.mapper.investimento.InvestimentoMapper;
import br.com.prismaapi.model.mapper.movimentacaoinvestimento.MovimentacaoInvestimentoMapper;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.movimentacaoinvestimento.MovimentacaoInvestimentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private final MovimentacaoInvestimentoMapper movimentacaoInvestimentoMapper;
    private final MovimentacaoInvestimentoRepository movimentacaoInvestimentoRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final Comparator<Investimento> MAIOR_VALOR_PRIMEIRO = Comparator.comparing(Investimento::getValorAtual, Comparator.reverseOrder()).thenComparing(Investimento::getNome, ORDEM_ALFABETICA);
    private static final Comparator<MovimentacaoInvestimento> ORDEM_DA_SERIE = Comparator.comparing(MovimentacaoInvestimento::getData).thenComparing(MovimentacaoInvestimento::getDataCriacao).thenComparing(MovimentacaoInvestimento::getTipo);

    @Transactional(readOnly = true)
    public CarteiraDTO resumirCarteira() {
        log.info("Resumindo a carteira de investimentos...");
        var hoje = LocalDate.now();
        var investimentos = investimentoRepository.findAll();
        var series = seriesPorInvestimento();

        var aportado = somar(investimentos, Investimento::getAportado);
        var valorAtual = somar(investimentos, Investimento::getValorAtual);
        var rendimento = valorAtual.subtract(aportado);

        var primeiraData = series.values().stream()
                .map(serie -> serie.getFirst().data())
                .min(Comparator.naturalOrder())
                .orElse(hoje);

        var historico = pontosDaCarteira(series.values(), janela(primeiraData, hoje), hoje);
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
    public List<PontoEvolucaoDTO> evolucaoNosMeses(List<YearMonth> meses) {
        return pontosDaCarteira(seriesPorInvestimento().values(), meses, LocalDate.now());
    }

    @Transactional(readOnly = true)
    public ExtratoInvestimentoDTO buscarExtrato(UUID id) {
        log.info("Buscando o extrato do investimento... - ID: [{}]", id);
        var hoje = LocalDate.now();
        var investimento = buscar(id);
        var serie = serie(movimentacaoInvestimentoRepository.findByInvestimentoId(id));
        var valorDaCarteira = somar(investimentoRepository.findAll(), Investimento::getValorAtual);

        var aportes = serie.stream()
                .filter(movimentacao -> movimentacao.tipo() == TipoMovimentacaoInvestimento.APORTE)
                .toList();

        var evolucao = janela(serie.getFirst().data(), hoje)
                .stream()
                .map(mes -> ponto(List.of(serie), mes, hoje))
                .toList();

        return new ExtratoInvestimentoDTO(
                posicao(investimento, valorDaCarteira),
                aportes.size(),
                aportes.isEmpty() ? null : aportes.getLast().data(),
                serie.reversed(),
                evolucao);
    }

    @Transactional
    public InvestimentoDTO salvar(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        log.info("Salvando o investimento... - Nome: {}", salvarInvestimentoDTO.nome());
        var investimento = investimentoMapper.toEntity(salvarInvestimentoDTO);

        preencher(investimento, salvarInvestimentoDTO.nome(), salvarInvestimentoDTO.instituicao(), salvarInvestimentoDTO.observacoes());
        investimento.setDataUltimaMovimentacao(salvarInvestimentoDTO.dataInicio());
        investimentoRepository.save(investimento);

        var movimentacoes = new ArrayList<MovimentacaoInvestimento>();
        movimentacoes.add(movimentar(investimento, aplicacaoInicial(salvarInvestimentoDTO)));

        if (salvarInvestimentoDTO.valorAtual().compareTo(salvarInvestimentoDTO.aportado()) != 0) {
            movimentacoes.add(movimentar(investimento, saldoDoCadastro(salvarInvestimentoDTO)));
        }

        return atualizarPosicao(investimento, movimentacoes);
    }

    @Transactional
    public InvestimentoDTO atualizar(UUID id, AtualizarInvestimentoDTO atualizarInvestimentoDTO) {
        log.info("Atualizando o investimento... - ID: [{}]", id);
        var investimento = buscar(id);

        investimentoMapper.updateEntity(atualizarInvestimentoDTO, investimento);
        preencher(investimento, atualizarInvestimentoDTO.nome(), atualizarInvestimentoDTO.instituicao(), atualizarInvestimentoDTO.observacoes());

        return investimentoMapper.toDTO(investimento);
    }

    @Transactional
    public InvestimentoDTO registrarAporte(UUID id, SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO) {
        log.info("Registrando o aporte do investimento... - ID: [{}] - Data: {}", id, salvarAporteInvestimentoDTO.data());
        var investimento = buscar(id);
        validarData(investimento, salvarAporteInvestimentoDTO.data(), "aporte");

        movimentar(investimento, movimentacaoInvestimentoMapper.toEntity(salvarAporteInvestimentoDTO));

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    public InvestimentoDTO registrarSaldo(UUID id, SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO) {
        log.info("Registrando o saldo do investimento... - ID: [{}] - Data: {}", id, salvarSaldoInvestimentoDTO.data());
        var investimento = buscar(id);
        validarData(investimento, salvarSaldoInvestimentoDTO.data(), "saldo");

        movimentar(investimento, movimentacaoInvestimentoMapper.toEntity(salvarSaldoInvestimentoDTO));

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
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

    private MovimentacaoInvestimento movimentar(Investimento investimento, MovimentacaoInvestimento movimentacao) {
        movimentacao.setInvestimento(investimento);
        movimentacao.setDescricao(textoOuNulo(movimentacao.getDescricao()));

        return movimentacaoInvestimentoRepository.saveAndFlush(movimentacao);
    }

    private InvestimentoDTO atualizarPosicao(Investimento investimento, List<MovimentacaoInvestimento> movimentacoes) {
        var serie = serie(movimentacoes);

        investimento.setAportado(serie.getLast().aportadoApos());
        investimento.setValorAtual(serie.getLast().saldoApos());
        investimento.setDataInicio(serie.getFirst().data());
        investimento.setDataUltimaMovimentacao(serie.getLast().data());

        return investimentoMapper.toDTO(investimento);
    }

    private Map<UUID, List<MovimentacaoInvestimentoDTO>> seriesPorInvestimento() {
        return movimentacaoInvestimentoRepository.findAll()
                .stream()
                .collect(Collectors.groupingBy(
                        movimentacao -> movimentacao.getInvestimento().getId(),
                        Collectors.collectingAndThen(Collectors.toList(), this::serie)));
    }

    private List<MovimentacaoInvestimentoDTO> serie(List<MovimentacaoInvestimento> movimentacoes) {
        var saldo = BigDecimal.ZERO;
        var aportado = BigDecimal.ZERO;
        var serie = new ArrayList<MovimentacaoInvestimentoDTO>();

        for (var movimentacao : movimentacoes.stream().sorted(ORDEM_DA_SERIE).toList()) {
            var valor = movimentacao.getTipo() == TipoMovimentacaoInvestimento.APORTE
                    ? movimentacao.getValor()
                    : movimentacao.getSaldoInformado().subtract(saldo);

            saldo = dinheiro(saldo.add(valor));

            if (movimentacao.getTipo() == TipoMovimentacaoInvestimento.APORTE) {
                aportado = dinheiro(aportado.add(valor));
            }

            serie.add(movimentacaoInvestimentoMapper.toDTO(movimentacao, dinheiro(valor), saldo, aportado));
        }

        return serie;
    }

    private static List<PontoEvolucaoDTO> pontosDaCarteira(Collection<List<MovimentacaoInvestimentoDTO>> series, List<YearMonth> meses, LocalDate hoje) {
        return meses.stream()
                .map(mes -> ponto(series, mes, hoje))
                .toList();
    }

    private static PontoEvolucaoDTO ponto(Collection<List<MovimentacaoInvestimentoDTO>> series, YearMonth mes, LocalDate hoje) {
        var fechamento = mes.equals(YearMonth.from(hoje)) ? hoje : mes.atEndOfMonth();
        var aportado = BigDecimal.ZERO;
        var valor = BigDecimal.ZERO;

        for (var serie : series) {
            var ultima = serie.stream()
                    .filter(movimentacao -> !movimentacao.data().isAfter(fechamento))
                    .reduce((anterior, seguinte) -> seguinte);

            if (ultima.isPresent()) {
                aportado = aportado.add(ultima.get().aportadoApos());
                valor = valor.add(ultima.get().saldoApos());
            }
        }

        return new PontoEvolucaoDTO(MesDoAno.rotulo(mes), mes, dinheiro(aportado), dinheiro(valor));
    }

    private static List<YearMonth> janela(LocalDate primeiraData, LocalDate hoje) {
        var mesAtual = YearMonth.from(hoje);
        var inicio = Stream.of(YearMonth.from(primeiraData), mesAtual.minusMonths(1)).min(Comparator.naturalOrder()).orElseThrow();
        var inicioDaJanela = Stream.of(inicio, mesAtual.minusMonths(11)).max(Comparator.naturalOrder()).orElseThrow();

        return Stream.iterate(inicioDaJanela, mes -> !mes.isAfter(mesAtual), mes -> mes.plusMonths(1)).toList();
    }

    private static void validarData(Investimento investimento, LocalDate data, String movimento) {
        var ultimaAtualizacao = investimento.getDataUltimaMovimentacao();

        if (data.isBefore(ultimaAtualizacao)) {
            var formatada = ultimaAtualizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            log.error("A data do {} não pode ser anterior à última atualização, de {}!", movimento, formatada);
            throw new DataAnteriorAUltimaAtualizacaoException("A data do %s não pode ser anterior à última atualização, de %s!".formatted(movimento, formatada));
        }
    }

    private static MovimentacaoInvestimento aplicacaoInicial(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        var aporte = new MovimentacaoInvestimento();

        aporte.setTipo(TipoMovimentacaoInvestimento.APORTE);
        aporte.setData(salvarInvestimentoDTO.dataInicio());
        aporte.setValor(salvarInvestimentoDTO.aportado());
        aporte.setDescricao("Aplicação inicial");

        return aporte;
    }

    private static MovimentacaoInvestimento saldoDoCadastro(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        var saldo = new MovimentacaoInvestimento();

        saldo.setTipo(TipoMovimentacaoInvestimento.RENDIMENTO);
        saldo.setData(LocalDate.now());
        saldo.setSaldoInformado(salvarInvestimentoDTO.valorAtual());
        saldo.setDescricao("Saldo informado no cadastro");

        return saldo;
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
                .map(investimento -> posicao(investimento, valorDaCarteira))
                .toList();
    }

    private PosicaoDTO posicao(Investimento investimento, BigDecimal valorDaCarteira) {
        var rendimento = investimento.getValorAtual().subtract(investimento.getAportado());

        return new PosicaoDTO(
                investimentoMapper.toDTO(investimento),
                rendimento,
                fracao(rendimento, investimento.getAportado()),
                fracao(investimento.getValorAtual(), valorDaCarteira));
    }

    private static void preencher(Investimento investimento, String nome, String instituicao, String observacoes) {
        investimento.setNome(nome.strip());
        investimento.setInstituicao(instituicao.strip());
        investimento.setObservacoes(textoOuNulo(observacoes));
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