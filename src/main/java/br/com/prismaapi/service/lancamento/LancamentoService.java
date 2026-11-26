package br.com.prismaapi.service.lancamento;

import br.com.prismaapi.config.cache.LimparCachesDeLancamento;
import br.com.prismaapi.enums.DirecaoOrdenacao;
import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.OrdenacaoLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.*;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.PaginaLancamentosDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.lancamento.LancamentoMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoSpecification;
import br.com.prismaapi.service.categoria.CategoriaService;
import br.com.prismaapi.service.recorrente.RecorrenteService;
import br.com.prismaapi.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final LancamentoMapper lancamentoMapper;
    private final CategoriaService categoriaService;
    private final RecorrenteService recorrenteService;
    private final LancamentoRepository lancamentoRepository;

    @Cacheable("lancamentos")
    @Transactional(readOnly = true)
    public PaginaLancamentosDTO listar(FiltroLancamentoDTO filtro) {
        log.info("Listando os lançamentos... - Filtro: {}", filtro);
        validar(filtro);

        var especificacao = LancamentoSpecification.filtrar(filtro);
        var pagina = lancamentoRepository.findAll(especificacao, PageRequest.of(filtro.pagina(), filtro.tamanho(), ordenar(filtro)));

        return new PaginaLancamentosDTO(
                pagina.getContent().stream().map(lancamentoMapper::toDTO).toList(),
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages(),
                lancamentoRepository.somarResultado(especificacao));
    }

    @Transactional(readOnly = true)
    public LancamentoDTO buscarPorId(UUID id) {
        log.info("Buscando o lançamento... - ID: [{}]", id);
        return lancamentoMapper.toDTO(buscar(id));
    }

    @Transactional
    @LimparCachesDeLancamento
    public LancamentoDTO salvar(SalvarLancamentoDTO salvarLancamentoDTO) {
        log.info("Salvando o lançamento... - Descrição: {} - Tipo: {}", salvarLancamentoDTO.descricao(), salvarLancamentoDTO.tipo());
        var lancamento = lancamentoMapper.toEntity(salvarLancamentoDTO);
        preencher(lancamento, salvarLancamentoDTO);
        movimentarSaldos(lancamento, BigDecimal.ONE);

        lancamentoRepository.save(lancamento);
        repetir(lancamento, salvarLancamentoDTO.repeticao());

        return lancamentoMapper.toDTO(lancamento);
    }

    @Transactional
    @LimparCachesDeLancamento
    public LancamentoDTO atualizar(UUID id, SalvarLancamentoDTO salvarLancamentoDTO) {
        log.info("Atualizando o lançamento... - ID: [{}]", id);
        var lancamento = buscar(id);

        recusarLancamentoGerado(lancamento);
        movimentarSaldos(lancamento, BigDecimal.ONE.negate());

        lancamentoMapper.updateEntity(salvarLancamentoDTO, lancamento);
        preencher(lancamento, salvarLancamentoDTO);
        movimentarSaldos(lancamento, BigDecimal.ONE);
        repetir(lancamento, salvarLancamentoDTO.repeticao());

        return lancamentoMapper.toDTO(lancamento);
    }

    @Transactional
    @LimparCachesDeLancamento
    public void deletar(UUID id) {
        log.info("Deletando o lançamento... - ID: [{}]", id);
        var lancamento = buscar(id);

        recusarLancamentoGerado(lancamento);
        movimentarSaldos(lancamento, BigDecimal.ONE.negate());
        lancamentoRepository.delete(lancamento);
    }

    private void repetir(Lancamento lancamento, Frequencia repeticao) {
        if (repeticao == null || lancamento.getRecorrente() != null) return;

        if (lancamento.getTipo() == TipoLancamento.TRANSFERENCIA) {
            log.error("Uma transferência não pode se repetir!");
            throw new RequisicaoInvalidaException("Uma transferência não pode se repetir!");
        }

        recorrenteService.repetir(lancamento, repeticao);
    }

    private Lancamento buscar(UUID id) {
        return lancamentoRepository.buscarCompleto(id)
                .orElseThrow(() -> {
                    log.warn("Lançamento não encontrado! - ID: [{}]", id);
                    return new LancamentoNaoEncontradoException("Lançamento não encontrado!");
                });
    }

    private void preencher(Lancamento lancamento, SalvarLancamentoDTO salvarLancamentoDTO) {
        validarSituacao(salvarLancamentoDTO);

        lancamento.setDescricao(salvarLancamentoDTO.descricao());
        lancamento.setObservacoes(TextoUtil.textoOuNulo(salvarLancamentoDTO.observacoes()));

        lancamento.setConta(null);
        lancamento.setCartao(null);
        lancamento.setCategoria(null);
        lancamento.setContaDestino(null);

        vincularOrigem(lancamento, salvarLancamentoDTO.idOrigem());

        if (salvarLancamentoDTO.tipo() == TipoLancamento.TRANSFERENCIA) {
            vincularContaDestino(lancamento, salvarLancamentoDTO.idContaDestino());
        } else {
            lancamento.setCategoria(categoriaService.buscarDoTipo(salvarLancamentoDTO.idCategoria(), TipoCategoria.valueOf(salvarLancamentoDTO.tipo().name())));
        }

        validarForma(lancamento, salvarLancamentoDTO.forma());

        if (lancamento.getCartao() == null || lancamento.getTipo() != TipoLancamento.DESPESA) {
            lancamento.setDataPagamentoFatura(null);
            lancamento.setContaPagamentoFatura(null);
        }
    }

    private void vincularOrigem(Lancamento lancamento, UUID idOrigem) {
        var conta = contaRepository.findById(idOrigem);

        if (conta.isPresent()) {
            lancamento.setConta(conta.get());
            return;
        }

        var cartao = cartaoRepository.findById(idOrigem)
                .orElseThrow(() -> {
                    log.error("A conta informada não existe!");
                    return new OrigemInexistenteException("A conta informada não existe!");
                });

        if (cartao.getTipo() == TipoCartao.DEBITO) {
            log.error("O cartão de débito não é origem de lançamento: escolha a conta que ele movimenta!");
            throw new OrigemCartaoDeDebitoException("O cartão de débito não é origem de lançamento: escolha a conta que ele movimenta!");
        }

        if (lancamento.getTipo() == TipoLancamento.RECEITA && cartao.getTipo() == TipoCartao.CREDITO) {
            log.error("Uma receita só entra em conta ou em cartão de vale!");
            throw new OrigemDeReceitaInvalidaException("Uma receita só entra em conta ou em cartão de vale!");
        }

        lancamento.setCartao(cartao);
    }

    private void vincularContaDestino(Lancamento lancamento, UUID idContaDestino) {
        var contaDestino = Optional.ofNullable(idContaDestino)
                .flatMap(contaRepository::findById)
                .orElseThrow(() -> {
                    log.error("A conta de destino informada não existe!");
                    return new ContaDestinoInexistenteException("A conta de destino informada não existe!");
                });

        if (lancamento.getConta() == null) {
            log.error("A transferência precisa sair de uma conta!");
            throw new OrigemTransferenciaInvalidaException("A transferência precisa sair de uma conta!");
        }

        if (contaDestino.getId().equals(lancamento.getConta().getId())) {
            log.error("A conta de destino precisa ser diferente da origem!");
            throw new ContaDestinoIgualOrigemException("A conta de destino precisa ser diferente da origem!");
        }

        lancamento.setContaDestino(contaDestino);
    }

    private static void movimentarSaldos(Lancamento lancamento, BigDecimal sentido) {
        var valor = lancamento.getValor().multiply(sentido);

        movimentar(lancamento.getContaPagamentoFatura(), valor.negate());

        if (lancamento.getSituacao() != SituacaoLancamento.PAGO) return;

        switch (lancamento.getTipo()) {
            case RECEITA -> {
                movimentar(lancamento.getConta(), valor);
                movimentarVale(lancamento.getCartao(), valor);
            }
            case DESPESA -> {
                movimentar(lancamento.getConta(), valor.negate());
                movimentarVale(lancamento.getCartao(), valor.negate());
            }
            case TRANSFERENCIA -> {
                movimentar(lancamento.getConta(), valor.negate());
                movimentar(lancamento.getContaDestino(), valor);
            }
        }
    }

    private static void movimentar(Conta conta, BigDecimal valor) {
        if (conta == null) return;

        conta.setSaldo(conta.getSaldo().add(valor));
    }

    private static void movimentarVale(Cartao cartao, BigDecimal valor) {
        if (cartao == null || cartao.getSaldo() == null) return;

        cartao.setSaldo(cartao.getSaldo().add(valor));
    }

    private static void recusarLancamentoGerado(Lancamento lancamento) {
        if (lancamento.getParcelaPaga() != null) {
            log.error("Este lançamento foi gerado pelo pagamento de uma parcela: desfaça o pagamento na compra parcelada para alterá-lo!");
            throw new LancamentoDePagamentoDeParcelaException("Este lançamento foi gerado pelo pagamento de uma parcela: desfaça o pagamento na compra parcelada para alterá-lo!");
        }

        if (lancamento.getMovimentacaoInvestimento() != null) {
            log.error("Este lançamento foi gerado por um aporte ou resgate de investimento e não pode ser alterado aqui!");
            throw new LancamentoDeMovimentacaoDeInvestimentoException("Este lançamento foi gerado por um aporte ou resgate de investimento e não pode ser alterado aqui!");
        }
    }

    private static void validarForma(Lancamento lancamento, FormaLancamento forma) {
        var noCartao = lancamento.getCartao() != null;

        if (noCartao && forma != FormaLancamento.CARTAO_CREDITO) {
            log.error("Um lançamento no cartão precisa ter a forma de pagamento cartão!");
            throw new FormaIncompativelComOrigemException("Um lançamento no cartão precisa ter a forma de pagamento cartão!");
        }

        if (!noCartao && forma == FormaLancamento.CARTAO_CREDITO) {
            log.error("Um lançamento na conta não pode ter a forma de pagamento cartão de crédito!");
            throw new FormaIncompativelComOrigemException("Um lançamento na conta não pode ter a forma de pagamento cartão de crédito!");
        }
    }

    private static void validarSituacao(SalvarLancamentoDTO salvarLancamentoDTO) {
        if (salvarLancamentoDTO.situacao() == SituacaoLancamento.PAGO && salvarLancamentoDTO.data().isAfter(LocalDate.now())) {
            log.error("Um lançamento com data futura não pode estar concluído: marque como agendado ou pendente!");
            throw new LancamentoFuturoConcluidoException("Um lançamento com data futura não pode estar concluído: marque como agendado ou pendente!");
        }
    }

    private static void validar(FiltroLancamentoDTO filtro) {
        if (filtro.dataInicial() != null && filtro.dataFinal() != null && filtro.dataInicial().isAfter(filtro.dataFinal())) {
            log.warn("O período informado é inválido!");
            throw new RequisicaoInvalidaException("O período informado é inválido!");
        }

        if (filtro.pagina() < 0) {
            log.warn("A página informada é inválida! - Página: {}", filtro.pagina());
            throw new RequisicaoInvalidaException("A página precisa ser zero ou maior!");
        }

        if (filtro.tamanho() < 1 || filtro.tamanho() > 100) {
            log.warn("O tamanho de página informado é inválido! - Tamanho: {}", filtro.tamanho());
            throw new RequisicaoInvalidaException("O tamanho da página precisa ficar entre 1 e 100!");
        }
    }

    private static Sort ordenar(FiltroLancamentoDTO filtro) {
        var direcao = filtro.direcao() == DirecaoOrdenacao.ASC ? Sort.Direction.ASC : Sort.Direction.DESC;
        var maisRecentes = Sort.by(Sort.Order.desc("data"), Sort.Order.desc("dataCriacao"));

        var principal = switch (filtro.ordenacao() == null ? OrdenacaoLancamento.DATA : filtro.ordenacao()) {
            case DATA -> Sort.by(new Sort.Order(direcao, "data"), new Sort.Order(direcao, "dataCriacao"));
            case DESCRICAO -> Sort.by(new Sort.Order(direcao, "descricao").ignoreCase()).and(maisRecentes);
            case VALOR -> Sort.by(new Sort.Order(direcao, "valor")).and(maisRecentes);
        };

        return principal.and(Sort.by(Sort.Order.asc("id")));
    }
}