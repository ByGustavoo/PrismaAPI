package br.com.prismaapi.service.recorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoRecorrente;
import br.com.prismaapi.exceptions.CategoriaDeDespesaException;
import br.com.prismaapi.exceptions.CategoriaDeReceitaException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.exceptions.CategoriaObrigatoriaException;
import br.com.prismaapi.exceptions.OrigemDeReceitaInvalidaException;
import br.com.prismaapi.exceptions.OrigemInexistenteException;
import br.com.prismaapi.exceptions.RecorrenteNaoEncontradaException;
import br.com.prismaapi.model.dto.recorrente.RecorrenteDTO;
import br.com.prismaapi.model.dto.recorrente.ResumoRecorrentesDTO;
import br.com.prismaapi.model.dto.recorrente.SalvarRecorrenteDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.entity.recorrente.Recorrente;
import br.com.prismaapi.model.mapper.recorrente.RecorrenteMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.recorrente.RecorrenteRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecorrenteService {

    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final RecorrenteMapper recorrenteMapper;
    private final CategoriaRepository categoriaRepository;
    private final RecorrenteRepository recorrenteRepository;
    private final LancamentoRecorrenteService lancamentoRecorrenteService;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Cacheable("recorrentes")
    @Transactional(readOnly = true)
    public ResumoRecorrentesDTO resumir() {
        log.info("Resumindo as recorrentes...");
        var hoje = LocalDate.now();

        var recorrentes = recorrenteRepository.buscarComOrigemECategoria()
                .stream()
                .map(recorrente -> toDTO(recorrente, hoje))
                .sorted(Comparator.comparing((RecorrenteDTO recorrente) -> recorrente.situacao() != SituacaoRecorrente.ATIVO)
                        .thenComparing(RecorrenteDTO::proximoVencimento)
                        .thenComparing(RecorrenteDTO::descricao, ORDEM_ALFABETICA))
                .toList();

        var custoMensal = somarPorMes(recorrentes, TipoRecorrente.DESPESA);

        var vencendoEmBreve = recorrentes.stream()
                .filter(recorrente -> ativa(recorrente, TipoRecorrente.DESPESA))
                .filter(recorrente -> !recorrente.proximoVencimento().isAfter(hoje.plusDays(7)))
                .toList();

        return new ResumoRecorrentesDTO(
                recorrentes,
                custoMensal,
                custoMensal.multiply(BigDecimal.valueOf(12)),
                somarPorMes(recorrentes, TipoRecorrente.RECEITA),
                vencendoEmBreve);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "faturas", "lancamentos", "orcamentos", "previsao", "recorrentes", "relatorios"}, allEntries = true)
    public RecorrenteDTO salvar(SalvarRecorrenteDTO salvarRecorrenteDTO) {
        log.info("Salvando a recorrente... - Descrição: {} - Tipo: {}", salvarRecorrenteDTO.descricao(), salvarRecorrenteDTO.tipo());
        var hoje = LocalDate.now();
        var recorrente = recorrenteMapper.toEntity(salvarRecorrenteDTO);

        preencher(recorrente, salvarRecorrenteDTO, hoje);
        recorrenteRepository.save(recorrente);
        lancamentoRecorrenteService.lancar(recorrente, hoje);

        return toDTO(recorrente, hoje);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "faturas", "lancamentos", "orcamentos", "previsao", "recorrentes", "relatorios"}, allEntries = true)
    public RecorrenteDTO atualizar(UUID id, SalvarRecorrenteDTO salvarRecorrenteDTO) {
        log.info("Atualizando a recorrente... - ID: [{}]", id);
        var hoje = LocalDate.now();
        var recorrente = buscar(id);

        recorrenteMapper.updateEntity(salvarRecorrenteDTO, recorrente);
        preencher(recorrente, salvarRecorrenteDTO, hoje);
        lancamentoRecorrenteService.lancar(recorrente, hoje);

        return toDTO(recorrente, hoje);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "faturas", "lancamentos", "orcamentos", "previsao", "recorrentes", "relatorios"}, allEntries = true)
    public void deletar(UUID id) {
        log.info("Deletando a recorrente... - ID: [{}]", id);
        recorrenteRepository.delete(buscar(id));
    }

    public Recorrente repetir(Lancamento lancamento, Frequencia frequencia) {
        log.info("Repetindo o lançamento... - Descrição: {} - Frequência: {}", lancamento.getDescricao(), frequencia);
        var hoje = LocalDate.now();
        var recorrente = new Recorrente();

        recorrente.setFrequencia(frequencia);
        recorrente.setConta(lancamento.getConta());
        recorrente.setCartao(lancamento.getCartao());
        recorrente.setValor(lancamento.getValor());
        recorrente.setLancarAutomaticamente(true);
        recorrente.setCategoria(lancamento.getCategoria());
        recorrente.setDescricao(lancamento.getDescricao());
        recorrente.setSituacao(SituacaoRecorrente.ATIVO);
        recorrente.setTipo(TipoRecorrente.valueOf(lancamento.getTipo().name()));
        recorrente.setProximoVencimento(frequencia.proximaOcorrencia(lancamento.getData()));
        recorrente.setProximoVencimento(vencimentoAPartirDe(recorrente, hoje));

        recorrenteRepository.save(recorrente);
        lancamento.setRecorrente(recorrente);
        lancamentoRecorrenteService.lancar(recorrente, hoje);

        return recorrente;
    }

    private Recorrente buscar(UUID id) {
        return recorrenteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Recorrente não encontrada! - ID: [{}]", id);
                    return new RecorrenteNaoEncontradaException("Recorrente não encontrada!");
                });
    }

    private RecorrenteDTO toDTO(Recorrente recorrente, LocalDate hoje) {
        var vencimento = vencimentoAPartirDe(recorrente, hoje);
        var dto = recorrenteMapper.toDTO(recorrente);

        return new RecorrenteDTO(
                dto.id(),
                dto.tipo(),
                dto.descricao(),
                dto.valor(),
                dto.categoria(),
                dto.frequencia(),
                vencimento,
                dto.idOrigem(),
                dto.nomeOrigem(),
                dto.situacao(),
                dto.lancarAutomaticamente(),
                dto.observacoes());
    }

    private static LocalDate vencimentoAPartirDe(Recorrente recorrente, LocalDate hoje) {
        var vencimento = recorrente.getProximoVencimento();

        while (vencimento.isBefore(hoje)) {
            vencimento = recorrente.getFrequencia().proximaOcorrencia(vencimento);
        }

        return vencimento;
    }

    private void preencher(Recorrente recorrente, SalvarRecorrenteDTO salvarRecorrenteDTO, LocalDate hoje) {
        recorrente.setDescricao(salvarRecorrenteDTO.descricao().strip());
        recorrente.setObservacoes(textoOuNulo(salvarRecorrenteDTO.observacoes()));

        recorrente.setConta(null);
        recorrente.setCartao(null);

        vincularOrigem(recorrente, salvarRecorrenteDTO.idOrigem());
        recorrente.setCategoria(buscarCategoria(salvarRecorrenteDTO.idCategoria(), recorrente.getTipo()));

        if (Boolean.TRUE.equals(recorrente.getLancarAutomaticamente()) && recorrente.getCategoria() == null) {
            log.error("Escolha a categoria para a recorrente ser lançada automaticamente!");
            throw new CategoriaObrigatoriaException("Escolha a categoria para a recorrente ser lançada automaticamente!");
        }

        if (LancamentoRecorrenteService.lancaSozinha(recorrente)) {
            recorrente.setProximoVencimento(vencimentoAPartirDe(recorrente, hoje));
        }
    }

    private void vincularOrigem(Recorrente recorrente, UUID idOrigem) {
        var conta = contaRepository.findById(idOrigem);

        if (conta.isPresent()) {
            recorrente.setConta(conta.get());
            return;
        }

        var cartao = cartaoRepository.findById(idOrigem)
                .orElseThrow(() -> {
                    log.error("Escolha a conta ou o cartão da recorrente!");
                    return new OrigemInexistenteException("Escolha a conta ou o cartão da recorrente!");
                });

        if (recorrente.getTipo() == TipoRecorrente.RECEITA && (cartao.getTipo() == TipoCartao.CREDITO || cartao.getTipo() == TipoCartao.DEBITO)) {
            log.error("Uma receita só entra em conta ou em cartão de vale!");
            throw new OrigemDeReceitaInvalidaException("Uma receita só entra em conta ou em cartão de vale!");
        }

        recorrente.setCartao(cartao);
    }

    private Categoria buscarCategoria(UUID idCategoria, TipoRecorrente tipo) {
        if (idCategoria == null) return null;

        var categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> {
                    log.error("A categoria informada não existe!");
                    return new CategoriaInexistenteException("A categoria informada não existe!");
                });

        if (tipo == TipoRecorrente.DESPESA && categoria.getTipo() != TipoCategoria.DESPESA) {
            log.error("Escolha uma categoria de despesa!");
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        if (tipo == TipoRecorrente.RECEITA && categoria.getTipo() != TipoCategoria.RECEITA) {
            log.error("Escolha uma categoria de receita!");
            throw new CategoriaDeDespesaException("Escolha uma categoria de receita!");
        }

        return categoria;
    }

    private static boolean ativa(RecorrenteDTO recorrente, TipoRecorrente tipo) {
        return recorrente.tipo() == tipo && recorrente.situacao() == SituacaoRecorrente.ATIVO;
    }

    private static BigDecimal somarPorMes(List<RecorrenteDTO> recorrentes, TipoRecorrente tipo) {
        return recorrentes.stream()
                .filter(recorrente -> ativa(recorrente, tipo))
                .map(RecorrenteService::valorPorMes)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal valorPorMes(RecorrenteDTO recorrente) {
        var valor = recorrente.valor();

        return switch (recorrente.frequencia()) {
            case SEMANAL -> valor.multiply(new BigDecimal("4.3452"));
            case QUINZENAL -> valor.multiply(new BigDecimal("2.1726"));
            case MENSAL -> valor;
            case BIMESTRAL -> porMes(valor, 2);
            case TRIMESTRAL -> porMes(valor, 3);
            case SEMESTRAL -> porMes(valor, 6);
            case ANUAL -> porMes(valor, 12);
        };
    }

    private static BigDecimal porMes(BigDecimal valor, int meses) {
        return valor.divide(BigDecimal.valueOf(meses), 10, RoundingMode.HALF_UP);
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}