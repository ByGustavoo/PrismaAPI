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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;

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
        var hoje = LocalDate.now();

        var despesas = despesaRecorrenteRepository.buscarComOrigemECategoria()
                .stream()
                .sorted(Comparator.comparing((DespesaRecorrente despesa) -> despesa.getSituacao() != SituacaoDespesaRecorrente.ATIVO)
                        .thenComparing(DespesaRecorrente::getProximoVencimento)
                        .thenComparing(DespesaRecorrente::getDescricao, ORDEM_ALFABETICA))
                .toList();

        var ativas = despesas.stream()
                .filter(despesa -> despesa.getSituacao() == SituacaoDespesaRecorrente.ATIVO)
                .toList();

        var custoMensal = ativas.stream()
                .map(DespesaRecorrenteService::custoMensal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var vencendoEmBreve = ativas.stream()
                .filter(despesa -> !despesa.getProximoVencimento().isBefore(hoje))
                .filter(despesa -> !despesa.getProximoVencimento().isAfter(hoje.plusDays(7)))
                .map(despesaRecorrenteMapper::toDTO)
                .toList();

        return new ResumoDespesasRecorrentesDTO(
                despesas.stream().map(despesaRecorrenteMapper::toDTO).toList(),
                custoMensal,
                custoMensal.multiply(BigDecimal.valueOf(12)),
                vencendoEmBreve);
    }

    @Transactional
    public DespesaRecorrenteDTO salvar(SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        var despesa = despesaRecorrenteMapper.toEntity(salvarDespesaRecorrenteDTO);
        preencher(despesa, salvarDespesaRecorrenteDTO);

        return despesaRecorrenteMapper.toDTO(despesaRecorrenteRepository.save(despesa));
    }

    @Transactional
    public DespesaRecorrenteDTO atualizar(UUID id, SalvarDespesaRecorrenteDTO salvarDespesaRecorrenteDTO) {
        var despesa = buscar(id);

        despesaRecorrenteMapper.updateEntity(salvarDespesaRecorrenteDTO, despesa);
        preencher(despesa, salvarDespesaRecorrenteDTO);

        return despesaRecorrenteMapper.toDTO(despesa);
    }

    @Transactional
    public void deletar(UUID id) {
        despesaRecorrenteRepository.delete(buscar(id));
    }

    private DespesaRecorrente buscar(UUID id) {
        return despesaRecorrenteRepository.findById(id)
                .orElseThrow(() -> new DespesaRecorrenteNaoEncontradaException("Despesa recorrente não encontrada!"));
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
                .orElseThrow(() -> new OrigemInexistenteException("Escolha a conta ou o cartão que paga esta despesa!")));
    }

    private Categoria buscarCategoriaDeDespesa(UUID idCategoria) {
        if (idCategoria == null) return null;

        var categoria = categoriaRepository.findById(idCategoria)
                .orElseThrow(() -> new CategoriaInexistenteException("A categoria informada não existe!"));

        if (categoria.getTipo() != TipoCategoria.DESPESA) {
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        return categoria;
    }

    private static BigDecimal custoMensal(DespesaRecorrente despesa) {
        var valor = despesa.getValor();

        return switch (despesa.getFrequencia()) {
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