package br.com.prismaapi.service.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.MesDoAno;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.enums.TipoMovimentacaoInvestimento;
import br.com.prismaapi.exceptions.CadastroDeAcaoPorValorException;
import br.com.prismaapi.exceptions.CodigoDaAcaoObrigatorioException;
import br.com.prismaapi.exceptions.ContaVinculadaInexistenteException;
import br.com.prismaapi.exceptions.DataAnteriorAUltimaAtualizacaoException;
import br.com.prismaapi.exceptions.InvestimentoNaoEncontradoException;
import br.com.prismaapi.exceptions.MovimentacaoIncompativelComInvestimentoException;
import br.com.prismaapi.exceptions.QuantidadeDeAcoesObrigatoriaException;
import br.com.prismaapi.exceptions.ResgateMaiorQueSaldoException;
import br.com.prismaapi.exceptions.TaxasMaioresQueVendaException;
import br.com.prismaapi.exceptions.TrocaDeClasseDeAcaoException;
import br.com.prismaapi.exceptions.VendaMaiorQueQuantidadeException;
import br.com.prismaapi.model.dto.dashboard.variacao.VariacaoDTO;
import br.com.prismaapi.model.dto.investimento.AlocacaoDTO;
import br.com.prismaapi.model.dto.investimento.AtualizarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.CarteiraDTO;
import br.com.prismaapi.model.dto.investimento.ExtratoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.InvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.MovimentacaoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.PontoEvolucaoDTO;
import br.com.prismaapi.model.dto.investimento.PosicaoDTO;
import br.com.prismaapi.model.dto.investimento.ReservaEmergenciaDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarAporteInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCompraAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarCotacaoAcaoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarResgateInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarSaldoInvestimentoDTO;
import br.com.prismaapi.model.dto.investimento.SalvarVendaAcaoDTO;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.investimento.Investimento;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.movimentacaoinvestimento.MovimentacaoInvestimento;
import br.com.prismaapi.model.mapper.investimento.InvestimentoMapper;
import br.com.prismaapi.model.mapper.movimentacaoinvestimento.MovimentacaoInvestimentoMapper;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.investimento.InvestimentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.movimentacaoinvestimento.MovimentacaoInvestimentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestimentoService {

    private final ContaRepository contaRepository;
    private static final int CASAS_DA_FRACAO = 4;
    private final InvestimentoMapper investimentoMapper;
    private final LancamentoRepository lancamentoRepository;
    private final InvestimentoRepository investimentoRepository;
    private final MovimentacaoInvestimentoMapper movimentacaoInvestimentoMapper;
    private final MovimentacaoInvestimentoRepository movimentacaoInvestimentoRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final Comparator<Investimento> MAIOR_VALOR_PRIMEIRO = Comparator.comparing(Investimento::getValorAtual, Comparator.reverseOrder()).thenComparing(Investimento::getNome, ORDEM_ALFABETICA);
    private static final Comparator<MovimentacaoInvestimento> ORDEM_DA_SERIE = Comparator.comparing(MovimentacaoInvestimento::getData).thenComparing(MovimentacaoInvestimento::getDataCriacao).thenComparing(MovimentacaoInvestimento::getTipo);

    @Cacheable("investimentos")
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
                reserva(investimentos),
                alocacao(investimentos, valorAtual),
                historico,
                posicoes(investimentos, valorAtual));
    }

    @Transactional(readOnly = true)
    public List<PontoEvolucaoDTO> evolucaoNosMeses(List<YearMonth> meses) {
        return pontosDaCarteira(seriesPorInvestimento().values(), meses, LocalDate.now());
    }

    @Cacheable("investimentos")
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
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO salvar(SalvarInvestimentoDTO salvarInvestimentoDTO) {
        log.info("Salvando o investimento... - Nome: {}", salvarInvestimentoDTO.nome());

        if (salvarInvestimentoDTO.classeAtivo() == ClasseAtivo.ACOES) {
            log.error("Ações são cadastradas pela quantidade e pelo preço por ação!");
            throw new CadastroDeAcaoPorValorException("Ações são cadastradas pela quantidade e pelo preço por ação!");
        }

        var investimento = investimentoMapper.toEntity(salvarInvestimentoDTO);

        preencher(investimento, salvarInvestimentoDTO.nome(), salvarInvestimentoDTO.instituicao(), salvarInvestimentoDTO.observacoes());
        investimento.setDataUltimaMovimentacao(salvarInvestimentoDTO.dataInicio());
        investimentoRepository.save(investimento);

        var conta = buscarConta(salvarInvestimentoDTO.idConta());
        var movimentacoes = new ArrayList<MovimentacaoInvestimento>();
        movimentacoes.add(movimentar(investimento, aplicacaoInicial(salvarInvestimentoDTO), conta));

        if (salvarInvestimentoDTO.valorAtual().compareTo(salvarInvestimentoDTO.aportado()) != 0) {
            movimentacoes.add(movimentar(investimento, saldoDoCadastro(salvarInvestimentoDTO), null));
        }

        return atualizarPosicao(investimento, movimentacoes);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO salvarAcao(SalvarAcaoDTO salvarAcaoDTO) {
        log.info("Salvando a ação... - Código: {}", salvarAcaoDTO.codigo());
        var investimento = investimentoMapper.toEntity(salvarAcaoDTO);

        preencher(investimento, salvarAcaoDTO.nome(), salvarAcaoDTO.instituicao(), salvarAcaoDTO.observacoes());
        investimento.setCodigo(codigoDaAcao(salvarAcaoDTO.codigo()));
        investimento.setAportado(BigDecimal.ZERO);
        investimento.setValorAtual(BigDecimal.ZERO);
        investimento.setDataUltimaMovimentacao(salvarAcaoDTO.dataInicio());
        investimentoRepository.save(investimento);

        var conta = buscarConta(salvarAcaoDTO.idConta());
        var movimentacoes = new ArrayList<MovimentacaoInvestimento>();
        movimentacoes.add(movimentar(investimento, primeiraCompra(salvarAcaoDTO), conta));

        if (salvarAcaoDTO.cotacao() != null && salvarAcaoDTO.cotacao().compareTo(salvarAcaoDTO.precoUnitario()) != 0) {
            movimentacoes.add(movimentar(investimento, cotacaoDoCadastro(salvarAcaoDTO), null));
        }

        return atualizarPosicao(investimento, movimentacoes);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO atualizar(UUID id, AtualizarInvestimentoDTO atualizarInvestimentoDTO) {
        log.info("Atualizando o investimento... - ID: [{}]", id);
        var investimento = buscar(id);
        validarClasse(investimento, atualizarInvestimentoDTO.classeAtivo());

        investimentoMapper.updateEntity(atualizarInvestimentoDTO, investimento);
        preencher(investimento, atualizarInvestimentoDTO.nome(), atualizarInvestimentoDTO.instituicao(), atualizarInvestimentoDTO.observacoes());

        if (investimento.getClasseAtivo() != ClasseAtivo.ACOES) return paraDTO(investimento);

        investimento.setCodigo(codigoDaAcao(atualizarInvestimentoDTO.codigo()));

        if (investimento.getQuantidade() != null) return paraDTO(investimento);

        movimentar(investimento, posicaoInformada(investimento, quantidadeDaAcao(atualizarInvestimentoDTO.quantidade())), null);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarAporte(UUID id, SalvarAporteInvestimentoDTO salvarAporteInvestimentoDTO) {
        log.info("Registrando o aporte do investimento... - ID: [{}] - Data: {}", id, salvarAporteInvestimentoDTO.data());
        var investimento = buscar(id);
        validarPorValor(investimento);
        validarData(investimento, salvarAporteInvestimentoDTO.data(), "do aporte");
        var conta = buscarConta(salvarAporteInvestimentoDTO.idConta());

        movimentar(investimento, movimentacaoInvestimentoMapper.toEntity(salvarAporteInvestimentoDTO), conta);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarResgate(UUID id, SalvarResgateInvestimentoDTO salvarResgateInvestimentoDTO) {
        log.info("Registrando o resgate do investimento... - ID: [{}] - Data: {}", id, salvarResgateInvestimentoDTO.data());
        var investimento = buscar(id);
        validarPorValor(investimento);
        validarData(investimento, salvarResgateInvestimentoDTO.data(), "do resgate");
        validarResgate(investimento, salvarResgateInvestimentoDTO.valor());
        var conta = buscarConta(salvarResgateInvestimentoDTO.idConta());

        movimentar(investimento, movimentacaoInvestimentoMapper.toEntity(salvarResgateInvestimentoDTO), conta);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarSaldo(UUID id, SalvarSaldoInvestimentoDTO salvarSaldoInvestimentoDTO) {
        log.info("Registrando o saldo do investimento... - ID: [{}] - Data: {}", id, salvarSaldoInvestimentoDTO.data());
        var investimento = buscar(id);
        validarPorValor(investimento);
        validarData(investimento, salvarSaldoInvestimentoDTO.data(), "do saldo");

        movimentar(investimento, movimentacaoInvestimentoMapper.toEntity(salvarSaldoInvestimentoDTO), null);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarCompra(UUID id, SalvarCompraAcaoDTO salvarCompraAcaoDTO) {
        log.info("Registrando a compra de ações... - ID: [{}] - Data: {}", id, salvarCompraAcaoDTO.data());
        var investimento = buscar(id);
        validarPorQuantidade(investimento);
        validarData(investimento, salvarCompraAcaoDTO.data(), "da compra");
        var conta = buscarConta(salvarCompraAcaoDTO.idConta());

        var compra = movimentacaoInvestimentoMapper.toEntity(salvarCompraAcaoDTO);
        compra.setValor(custoDaCompra(compra.getQuantidade(), compra.getPrecoUnitario(), compra.getTaxas()));
        movimentar(investimento, compra, conta);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarVenda(UUID id, SalvarVendaAcaoDTO salvarVendaAcaoDTO) {
        log.info("Registrando a venda de ações... - ID: [{}] - Data: {}", id, salvarVendaAcaoDTO.data());
        var investimento = buscar(id);
        validarPorQuantidade(investimento);
        validarData(investimento, salvarVendaAcaoDTO.data(), "da venda");
        validarVenda(investimento, salvarVendaAcaoDTO.quantidade());
        var conta = buscarConta(salvarVendaAcaoDTO.idConta());

        var venda = movimentacaoInvestimentoMapper.toEntity(salvarVendaAcaoDTO);
        venda.setValor(valorDaVenda(venda.getQuantidade(), venda.getPrecoUnitario(), venda.getTaxas()));
        movimentar(investimento, venda, conta);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public InvestimentoDTO registrarCotacao(UUID id, SalvarCotacaoAcaoDTO salvarCotacaoAcaoDTO) {
        log.info("Registrando a cotação da ação... - ID: [{}] - Data: {}", id, salvarCotacaoAcaoDTO.data());
        var investimento = buscar(id);
        validarPorQuantidade(investimento);
        validarData(investimento, salvarCotacaoAcaoDTO.data(), "da cotação");

        var cotacao = movimentacaoInvestimentoMapper.toEntity(salvarCotacaoAcaoDTO);
        cotacao.setSaldoInformado(dinheiro(investimento.getQuantidade().multiply(cotacao.getPrecoUnitario())));
        movimentar(investimento, cotacao, null);

        return atualizarPosicao(investimento, movimentacaoInvestimentoRepository.findByInvestimentoId(id));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "contas", "dashboard", "investimentos", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public void deletar(UUID id) {
        log.info("Deletando o investimento... - ID: [{}]", id);
        var investimento = buscar(id);
        var lancamentos = lancamentoRepository.findByMovimentacaoInvestimentoInvestimentoId(id);

        lancamentos.forEach(InvestimentoService::desfazerNaConta);
        lancamentoRepository.deleteAll(lancamentos);
        lancamentoRepository.flush();

        investimentoRepository.delete(investimento);
    }

    private Investimento buscar(UUID id) {
        return investimentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Investimento não encontrado! - ID: [{}]", id);
                    return new InvestimentoNaoEncontradoException("Investimento não encontrado!");
                });
    }

    private Conta buscarConta(UUID idConta) {
        if (idConta == null) return null;

        return contaRepository.findById(idConta)
                .orElseThrow(() -> {
                    log.error("A conta informada não existe!");
                    return new ContaVinculadaInexistenteException("A conta informada não existe!");
                });
    }

    private MovimentacaoInvestimento movimentar(Investimento investimento, MovimentacaoInvestimento movimentacao, Conta conta) {
        movimentacao.setConta(conta);
        movimentacao.setInvestimento(investimento);
        movimentacao.setDescricao(textoOuNulo(movimentacao.getDescricao()));

        var salva = movimentacaoInvestimentoRepository.saveAndFlush(movimentacao);

        if (conta != null) {
            lancamentoRepository.save(transferencia(investimento, salva, conta));
        }

        return salva;
    }

    private static Lancamento transferencia(Investimento investimento, MovimentacaoInvestimento movimentacao, Conta conta) {
        var resgate = movimentacao.getTipo() == TipoMovimentacaoInvestimento.RESGATE;
        var lancamento = new Lancamento();

        lancamento.setDescricao(descricaoDaTransferencia(investimento, movimentacao, resgate));
        lancamento.setValor(movimentacao.getValor());
        lancamento.setData(movimentacao.getData());
        lancamento.setForma(FormaLancamento.CONTA);
        lancamento.setTipo(TipoLancamento.TRANSFERENCIA);
        lancamento.setSituacao(SituacaoLancamento.PAGO);
        lancamento.setMovimentacaoInvestimento(movimentacao);

        if (resgate) {
            lancamento.setContaDestino(conta);
            conta.setSaldo(conta.getSaldo().add(movimentacao.getValor()));
        } else {
            lancamento.setConta(conta);
            conta.setSaldo(conta.getSaldo().subtract(movimentacao.getValor()));
        }

        return lancamento;
    }

    private static String descricaoDaTransferencia(Investimento investimento, MovimentacaoInvestimento movimentacao, boolean resgate) {
        if (movimentacao.getQuantidade() == null) {
            return (resgate ? "Resgate — " : "Aporte — ") + investimento.getNome();
        }

        return (resgate ? "Venda — " : "Compra — ") + investimento.getCodigo();
    }

    private static void desfazerNaConta(Lancamento lancamento) {
        if (lancamento.getConta() != null) {
            lancamento.getConta().setSaldo(lancamento.getConta().getSaldo().add(lancamento.getValor()));
        }

        if (lancamento.getContaDestino() != null) {
            lancamento.getContaDestino().setSaldo(lancamento.getContaDestino().getSaldo().subtract(lancamento.getValor()));
        }
    }

    private InvestimentoDTO atualizarPosicao(Investimento investimento, List<MovimentacaoInvestimento> movimentacoes) {
        var serie = serie(movimentacoes);

        investimento.setAportado(serie.getLast().aportadoApos());
        investimento.setValorAtual(serie.getLast().saldoApos());
        investimento.setQuantidade(serie.getLast().quantidadeApos());
        investimento.setCotacao(cotacaoAtual(serie));
        investimento.setDataInicio(serie.getFirst().data());
        investimento.setDataUltimaMovimentacao(serie.getLast().data());

        return paraDTO(investimento);
    }

    private InvestimentoDTO paraDTO(Investimento investimento) {
        return investimentoMapper.toDTO(investimento, precoMedio(investimento));
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
        BigDecimal quantidade = null;
        var serie = new ArrayList<MovimentacaoInvestimentoDTO>();

        for (var movimentacao : movimentacoes.stream().sorted(ORDEM_DA_SERIE).toList()) {
            var saldoAnterior = saldo;
            var porQuantidade = movimentacao.getQuantidade() != null;

            switch (movimentacao.getTipo()) {
                case APORTE -> {
                    aportado = dinheiro(aportado.add(movimentacao.getValor()));

                    if (porQuantidade) {
                        quantidade = Objects.requireNonNullElse(quantidade, BigDecimal.ZERO).add(movimentacao.getQuantidade());
                        saldo = dinheiro(quantidade.multiply(movimentacao.getPrecoUnitario()));
                    } else {
                        saldo = dinheiro(saldo.add(movimentacao.getValor()));
                    }
                }
                case RESGATE -> {
                    if (porQuantidade && quantidade != null) {
                        var restante = quantidade.subtract(movimentacao.getQuantidade());
                        aportado = aportadoDepoisDaVenda(aportado, quantidade, restante);
                        quantidade = restante;
                        saldo = dinheiro(quantidade.multiply(movimentacao.getPrecoUnitario()));
                    } else {
                        aportado = aportadoDepoisDoResgate(aportado, saldo, movimentacao.getValor());
                        saldo = dinheiro(saldo.subtract(movimentacao.getValor()));
                    }
                }
                case RENDIMENTO -> {
                    quantidade = porQuantidade ? movimentacao.getQuantidade() : quantidade;
                    saldo = dinheiro(movimentacao.getSaldoInformado());
                }
            }

            var valorExibido = switch (movimentacao.getTipo()) {
                case APORTE -> movimentacao.getValor();
                case RESGATE -> movimentacao.getValor().negate();
                case RENDIMENTO -> saldo.subtract(saldoAnterior);
            };

            serie.add(movimentacaoInvestimentoMapper.toDTO(movimentacao, dinheiro(valorExibido), saldo, aportado, quantidade));
        }

        return serie;
    }

    private static BigDecimal cotacaoAtual(List<MovimentacaoInvestimentoDTO> serie) {
        return serie.reversed()
                .stream()
                .map(MovimentacaoInvestimentoDTO::precoUnitario)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private static BigDecimal precoMedio(Investimento investimento) {
        if (investimento.getQuantidade() == null || investimento.getQuantidade().signum() <= 0) return null;

        return investimento.getAportado().divide(investimento.getQuantidade(), 2, RoundingMode.HALF_UP);
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

    private static BigDecimal aportadoDepoisDoResgate(BigDecimal aportado, BigDecimal saldo, BigDecimal resgatado) {
        if (saldo.signum() <= 0 || resgatado.compareTo(saldo) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        var parteQueFica = saldo.subtract(resgatado).divide(saldo, 10, RoundingMode.HALF_UP);

        return dinheiro(aportado.multiply(parteQueFica));
    }

    private static BigDecimal aportadoDepoisDaVenda(BigDecimal aportado, BigDecimal quantidade, BigDecimal restante) {
        if (quantidade.signum() <= 0 || restante.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        return aportado.multiply(restante).divide(quantidade, 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal custoDaCompra(BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal taxas) {
        return dinheiro(quantidade.multiply(precoUnitario).add(Objects.requireNonNullElse(taxas, BigDecimal.ZERO)));
    }

    private static BigDecimal valorDaVenda(BigDecimal quantidade, BigDecimal precoUnitario, BigDecimal taxas) {
        var valor = dinheiro(quantidade.multiply(precoUnitario).subtract(Objects.requireNonNullElse(taxas, BigDecimal.ZERO)));

        if (valor.signum() <= 0) {
            log.error("As taxas não podem passar do valor da venda!");
            throw new TaxasMaioresQueVendaException("As taxas não podem passar do valor da venda!");
        }

        return valor;
    }

    private static void validarVenda(Investimento investimento, BigDecimal quantidade) {
        if (quantidade.compareTo(investimento.getQuantidade()) > 0) {
            var quantidadeAtual = investimento.getQuantidade().stripTrailingZeros().toPlainString();
            var limite = investimento.getQuantidade().compareTo(BigDecimal.ONE) == 0 ? "da 1 ação" : "das %s ações".formatted(quantidadeAtual);
            log.error("A venda não pode passar {} que você tem!", limite);
            throw new VendaMaiorQueQuantidadeException("A venda não pode passar %s que você tem!".formatted(limite));
        }
    }

    private static void validarPorValor(Investimento investimento) {
        if (investimento.getQuantidade() != null) {
            log.error("Nas ações, registre compras, vendas e cotações!");
            throw new MovimentacaoIncompativelComInvestimentoException("Nas ações, registre compras, vendas e cotações!");
        }
    }

    private static void validarPorQuantidade(Investimento investimento) {
        if (investimento.getQuantidade() == null) {
            log.error("Só ações com a quantidade informada registram compras, vendas e cotações!");
            throw new MovimentacaoIncompativelComInvestimentoException("Só ações com a quantidade informada registram compras, vendas e cotações!");
        }
    }

    private static void validarClasse(Investimento investimento, ClasseAtivo classeAtivo) {
        var eraAcao = investimento.getClasseAtivo() == ClasseAtivo.ACOES;

        if (eraAcao != (classeAtivo == ClasseAtivo.ACOES)) {
            var mensagem = eraAcao
                    ? "Ações não podem trocar de classe depois do cadastro!"
                    : "Para acompanhar uma ação pela quantidade, cadastre-a como um investimento novo!";
            log.error(mensagem);
            throw new TrocaDeClasseDeAcaoException(mensagem);
        }
    }

    private static String codigoDaAcao(String codigo) {
        if (codigo == null || codigo.isBlank()) {
            log.error("Informe o código da ação, como PETR4!");
            throw new CodigoDaAcaoObrigatorioException("Informe o código da ação, como PETR4!");
        }

        return codigo.strip().toUpperCase(Locale.ROOT);
    }

    private static BigDecimal quantidadeDaAcao(BigDecimal quantidade) {
        if (quantidade == null) {
            log.error("Informe quantas ações você tem para acompanhar a cotação!");
            throw new QuantidadeDeAcoesObrigatoriaException("Informe quantas ações você tem para acompanhar a cotação!");
        }

        return quantidade;
    }

    private static void validarResgate(Investimento investimento, BigDecimal valor) {
        if (valor.compareTo(investimento.getValorAtual()) > 0) {
            var saldo = String.format(Locale.forLanguageTag("pt-BR"), "%,.2f", investimento.getValorAtual());
            log.error("O resgate não pode ser maior que o saldo do investimento, de R$ {}!", saldo);
            throw new ResgateMaiorQueSaldoException("O resgate não pode ser maior que o saldo do investimento, de R$ %s!".formatted(saldo));
        }
    }

    private static void validarData(Investimento investimento, LocalDate data, String movimento) {
        var ultimaAtualizacao = investimento.getDataUltimaMovimentacao();

        if (data.isBefore(ultimaAtualizacao)) {
            var formatada = ultimaAtualizacao.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            log.error("A data {} não pode ser anterior à última atualização, de {}!", movimento, formatada);
            throw new DataAnteriorAUltimaAtualizacaoException("A data %s não pode ser anterior à última atualização, de %s!".formatted(movimento, formatada));
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

    private static MovimentacaoInvestimento primeiraCompra(SalvarAcaoDTO salvarAcaoDTO) {
        var compra = new MovimentacaoInvestimento();

        compra.setTipo(TipoMovimentacaoInvestimento.APORTE);
        compra.setData(salvarAcaoDTO.dataInicio());
        compra.setQuantidade(salvarAcaoDTO.quantidade());
        compra.setPrecoUnitario(salvarAcaoDTO.precoUnitario());
        compra.setTaxas(salvarAcaoDTO.taxas());
        compra.setValor(custoDaCompra(salvarAcaoDTO.quantidade(), salvarAcaoDTO.precoUnitario(), salvarAcaoDTO.taxas()));
        compra.setDescricao("Primeira compra");

        return compra;
    }

    private static MovimentacaoInvestimento cotacaoDoCadastro(SalvarAcaoDTO salvarAcaoDTO) {
        var cotacao = new MovimentacaoInvestimento();

        cotacao.setTipo(TipoMovimentacaoInvestimento.RENDIMENTO);
        cotacao.setData(LocalDate.now());
        cotacao.setPrecoUnitario(salvarAcaoDTO.cotacao());
        cotacao.setSaldoInformado(dinheiro(salvarAcaoDTO.quantidade().multiply(salvarAcaoDTO.cotacao())));
        cotacao.setDescricao("Cotação informada no cadastro");

        return cotacao;
    }

    private static MovimentacaoInvestimento posicaoInformada(Investimento investimento, BigDecimal quantidade) {
        var posicao = new MovimentacaoInvestimento();

        posicao.setTipo(TipoMovimentacaoInvestimento.RENDIMENTO);
        posicao.setData(LocalDate.now());
        posicao.setQuantidade(quantidade);
        posicao.setSaldoInformado(investimento.getValorAtual());
        posicao.setPrecoUnitario(investimento.getValorAtual().divide(quantidade, 2, RoundingMode.HALF_UP));

        return posicao;
    }

    private static ReservaEmergenciaDTO reserva(List<Investimento> investimentos) {
        var reservas = investimentos.stream()
                .filter(investimento -> Boolean.TRUE.equals(investimento.getReservaEmergencia()))
                .toList();

        var aportado = somar(reservas, Investimento::getAportado);
        var valorAtual = somar(reservas, Investimento::getValorAtual);

        return new ReservaEmergenciaDTO(aportado, valorAtual, valorAtual.subtract(aportado), reservas.size());
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
                paraDTO(investimento),
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