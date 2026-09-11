package br.com.prismaapi.service.compraparcelada;

import br.com.prismaapi.enums.SituacaoParcela;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.exceptions.CartaoInexistenteException;
import br.com.prismaapi.exceptions.CartaoNaoAceitaParcelamentoException;
import br.com.prismaapi.exceptions.CategoriaDeReceitaException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.exceptions.CompraParceladaNaoEncontradaException;
import br.com.prismaapi.exceptions.PrimeiroMesAnteriorACompraException;
import br.com.prismaapi.model.dto.compraparcelada.CompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.ParcelaDTO;
import br.com.prismaapi.model.dto.compraparcelada.PlanoCompraParceladaDTO;
import br.com.prismaapi.model.dto.compraparcelada.SalvarCompraParceladaDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import br.com.prismaapi.model.mapper.compraparcelada.CompraParceladaMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraParceladaService {

    private final FaturaService faturaService;
    private final CartaoRepository cartaoRepository;
    private final CategoriaRepository categoriaRepository;
    private final CompraParceladaMapper compraParceladaMapper;
    private final CompraParceladaRepository compraParceladaRepository;

    @Transactional(readOnly = true)
    public List<PlanoCompraParceladaDTO> listar(UUID idCartao) {
        log.info("Listando as compras parceladas... - ID do Cartão: [{}]", idCartao);
        var hoje = LocalDate.now();

        return compraParceladaRepository.buscarComCartaoECategoria()
                .stream()
                .filter(compra -> idCartao == null || compra.getCartao().getId().equals(idCartao))
                .map(compra -> planejar(compra, hoje))
                .sorted(Comparator.comparing((PlanoCompraParceladaDTO plano) -> plano.parcelasRestantes() == 0)
                        .thenComparing(plano -> plano.compra().dataCompra(), Comparator.reverseOrder()))
                .toList();
    }

    @Transactional
    public CompraParceladaDTO salvar(SalvarCompraParceladaDTO salvarCompraParceladaDTO) {
        log.info("Salvando a compra parcelada... - Descrição: {}", salvarCompraParceladaDTO.descricao());
        var compra = compraParceladaMapper.toEntity(salvarCompraParceladaDTO);
        preencher(compra, salvarCompraParceladaDTO);

        return compraParceladaMapper.toDTO(compraParceladaRepository.save(compra));
    }

    @Transactional
    public CompraParceladaDTO atualizar(UUID id, SalvarCompraParceladaDTO salvarCompraParceladaDTO) {
        log.info("Atualizando a compra parcelada... - ID: [{}]", id);
        var compra = buscar(id);

        compraParceladaMapper.updateEntity(salvarCompraParceladaDTO, compra);
        preencher(compra, salvarCompraParceladaDTO);

        return compraParceladaMapper.toDTO(compra);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando a compra parcelada... - ID: [{}]", id);
        compraParceladaRepository.delete(buscar(id));
    }

    private CompraParcelada buscar(UUID id) {
        return compraParceladaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Compra parcelada não encontrada! - ID: [{}]", id);
                    return new CompraParceladaNaoEncontradaException("Compra parcelada não encontrada!");
                });
    }

    private PlanoCompraParceladaDTO planejar(CompraParcelada compra, LocalDate hoje) {
        var cronograma = faturaService.cronograma(compra, hoje);

        var pagas = cronograma.stream()
                .filter(parcela -> parcela.situacao() == SituacaoParcela.PAGA)
                .toList();

        var valorPago = pagas.stream()
                .map(ParcelaDTO::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        var parcelaAtual = cronograma.stream()
                .filter(parcela -> parcela.situacao() == SituacaoParcela.ATUAL)
                .findFirst()
                .orElse(null);

        return new PlanoCompraParceladaDTO(
                compraParceladaMapper.toDTO(compra),
                cronograma.getFirst().valor(),
                pagas.size(),
                cronograma.size() - pagas.size(),
                valorPago,
                compra.getValorTotal().subtract(valorPago),
                parcelaAtual,
                cronograma);
    }

    private void preencher(CompraParcelada compra, SalvarCompraParceladaDTO salvarCompraParceladaDTO) {
        if (salvarCompraParceladaDTO.primeiroMes().isBefore(YearMonth.from(salvarCompraParceladaDTO.dataCompra()))) {
            log.error("A primeira parcela não pode cair antes do mês da compra!");
            throw new PrimeiroMesAnteriorACompraException("A primeira parcela não pode cair antes do mês da compra!");
        }

        compra.setDescricao(salvarCompraParceladaDTO.descricao().strip());
        compra.setPrimeiroMes(salvarCompraParceladaDTO.primeiroMes().atDay(1));
        compra.setObservacoes(textoOuNulo(salvarCompraParceladaDTO.observacoes()));
        compra.setCartao(buscarCartaoDeCredito(salvarCompraParceladaDTO.idCartao()));
        compra.setCategoria(buscarCategoriaDeDespesa(salvarCompraParceladaDTO.idCategoria()));
    }

    private Cartao buscarCartaoDeCredito(UUID idCartao) {
        var cartao = cartaoRepository.findById(idCartao)
                .orElseThrow(() -> {
                    log.error("O cartão informado não existe!");
                    return new CartaoInexistenteException("O cartão informado não existe!");
                });

        if (cartao.getTipo() != TipoCartao.CREDITO) {
            log.error("Só cartões de crédito aceitam compras parceladas!");
            throw new CartaoNaoAceitaParcelamentoException("Só cartões de crédito aceitam compras parceladas!");
        }

        return cartao;
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

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}