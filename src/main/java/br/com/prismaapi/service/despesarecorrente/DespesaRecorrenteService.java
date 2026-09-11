package br.com.prismaapi.service.despesarecorrente;

import br.com.prismaapi.enums.SituacaoDespesaRecorrente;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.exceptions.CategoriaDeReceitaException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.exceptions.DespesaRecorrenteNaoEncontradaException;
import br.com.prismaapi.exceptions.OrigemInexistenteException;
import br.com.prismaapi.model.dto.despesarecorrente.DespesaRecorrenteDTO;
import br.com.prismaapi.model.dto.despesarecorrente.ResumoDespesasRecorrentesDTO;
import br.com.prismaapi.model.dto.despesarecorrente.SalvarDespesaRecorrenteDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.despesarecorrente.DespesaRecorrente;
import br.com.prismaapi.model.mapper.despesarecorrente.DespesaRecorrenteMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DespesaRecorrenteService {

    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DespesaRecorrenteMapper despesaRecorrenteMapper;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Transactional(readOnly = true)
    public ResumoDespesasRecorrentesDTO resumir() {
        log.info("Resumindo as despesas recorrentes...");
        var hoje = LocalDate.now();

        var despesas = despesaRecorrenteRepository.buscarComOrigemECategoria()
                .stream()
                .map(despesa -> toDTO(despesa, hoje))
                .sorted(Comparator.comparing((DespesaRecorrenteDTO despesa) -> despesa.situacao() != SituacaoDespesaRecorrente.ATIVO)
                        .thenComparing(DespesaRecorrenteDTO::proximoVencimento)
                        .thenComparing(DespesaRecorrenteDTO::descricao, ORDEM_ALFABETICA))
                .toList();

        var ativas = despesas.stream()
                .filter(despesa -> despesa.situacao() == SituacaoDespesaRecorrente.ATIVO)
                .toList();

        var custoMensal = ativas.stream()
                .map(DespesaRecorrenteService::custoMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var vencendoEmBreve = ativas.stream()
                .filter(despesa -> !despesa.proximoVencimento().isAfter(hoje.plusDays(7)))
                .toList();

        return new ResumoDespesasRecorrentesDTO(
                despesas,
                custoMensal,
                custoMensal.multiply(BigDecimal.valueOf(12)),
                vencendoEmBreve);
    }

    @Transactional
    public DespesaRecorrenteDTO salvar(SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        log.info("Salvando a despesa recorrente... - Descrição: {}", salvarDespesaRecorrenteDTO.descricao());
        var despesa = despesaRecorrenteMapper.toEntity(salvarDespesaRecorrenteDTO);
        preencher(despesa, salvarDespesaRecorrenteDTO);

        return toDTO(despesaRecorrenteRepository.save(despesa), LocalDate.now());
    }

    @Transactional
    public DespesaRecorrenteDTO atualizar(UUID id, SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        log.info("Atualizando a despesa recorrente... - ID: [{}]", id);
        var despesa = buscar(id);

        despesaRecorrenteMapper.updateEntity(salvarDespesaRecorrenteDTO, despesa);
        preencher(despesa, salvarDespesaRecorrenteDTO);

        return toDTO(despesa, LocalDate.now());
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando a despesa recorrente... - ID: [{}]", id);
        despesaRecorrenteRepository.delete(buscar(id));
    }

    private DespesaRecorrente buscar(UUID id) {
        return despesaRecorrenteRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Despesa recorrente não encontrada! - ID: [{}]", id);
                    return new DespesaRecorrenteNaoEncontradaException("Despesa recorrente não encontrada!");
                });
    }

    private DespesaRecorrenteDTO toDTO(DespesaRecorrente despesa, LocalDate hoje) {
        var vencimento = despesa.getProximoVencimento();

        while (vencimento.isBefore(hoje)) {
            vencimento = despesa.getFrequencia().proximaOcorrencia(vencimento);
        }

        var dto = despesaRecorrenteMapper.toDTO(despesa);

        return new DespesaRecorrenteDTO(
                dto.id(),
                dto.descricao(),
                dto.valor(),
                dto.categoria(),
                dto.frequencia(),
                vencimento,
                dto.idOrigem(),
                dto.nomeOrigem(),
                dto.situacao(),
                dto.observacoes());
    }

    private void preencher(DespesaRecorrente despesa, SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        despesa.setDescricao(salvarDespesaRecorrenteDTO.descricao().strip());
        despesa.setObservacoes(textoOuNulo(salvarDespesaRecorrenteDTO.observacoes()));

        despesa.setConta(null);
        despesa.setCartao(null);

        vincularOrigem(despesa, salvarDespesaRecorrenteDTO.idOrigem());
        despesa.setCategoria(buscarCategoriaDeDespesa(salvarDespesaRecorrenteDTO.idCategoria()));
    }

    private void vincularOrigem(DespesaRecorrente despesa, UUID idOrigem) {
        var conta = contaRepository.findById(idOrigem);

        if (conta.isPresent()) {
            despesa.setConta(conta.get());
            return;
        }

        despesa.setCartao(cartaoRepository.findById(idOrigem)
                .orElseThrow(() -> {
                    log.error("Escolha a conta ou o cartão que paga esta despesa!");
                    return new OrigemInexistenteException("Escolha a conta ou o cartão que paga esta despesa!");
                }));
    }

    private Categoria buscarCategoriaDeDespesa(UUID idCategoria) {
        if (idCategoria == null) return null;

        var categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> {
                    log.error("A categoria informada não existe!");
                    return new CategoriaInexistenteException("A categoria informada não existe!");
                });

        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            log.error("Escolha uma categoria de despesa!");
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        return categoria;
    }

    private static BigDecimal custoMensal(DespesaRecorrenteDTO despesa) {
        var valor = despesa.valor();

        return switch (despesa.frequencia()) {
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