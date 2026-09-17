package br.com.prismaapi.service.lancamento;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.exceptions.*;
import br.com.prismaapi.model.dto.lancamento.FiltroLancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.LancamentoDTO;
import br.com.prismaapi.model.dto.lancamento.SalvarLancamentoDTO;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.lancamento.LancamentoMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LancamentoService {

    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final LancamentoMapper lancamentoMapper;
    private final CategoriaRepository categoriaRepository;
    private final LancamentoRepository lancamentoRepository;

    private static final Sort MAIS_RECENTES_PRIMEIRO = Sort.by(Sort.Order.desc("data"), Sort.Order.desc("dataCriacao"));

    @Transactional(readOnly = true)
    public List<LancamentoDTO> listar(FiltroLancamentoDTO filtro) {
        log.info("Listando os lançamentos... - Filtro: {}", filtro);
        validar(filtro);

        return lancamentoRepository.findAll(LancamentoSpecification.filtrar(filtro), MAIS_RECENTES_PRIMEIRO)
                .stream()
                .map(lancamentoMapper::toDTO)
                .toList();
    }

    @Transactional
    public LancamentoDTO salvar(SalvarLancamentoDTO salvarLancamentoDTO) {
        log.info("Salvando o lançamento... - Descrição: {} - Tipo: {}", salvarLancamentoDTO.descricao(), salvarLancamentoDTO.tipo());
        var lancamento = lancamentoMapper.toEntity(salvarLancamentoDTO);
        preencher(lancamento, salvarLancamentoDTO);
        movimentarSaldos(lancamento, BigDecimal.ONE);

        return lancamentoMapper.toDTO(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public LancamentoDTO atualizar(UUID id, SalvarLancamentoDTO salvarLancamentoDTO) {
        log.info("Atualizando o lançamento... - ID: [{}]", id);
        var lancamento = buscar(id);
        movimentarSaldos(lancamento, BigDecimal.ONE.negate());

        lancamentoMapper.updateEntity(salvarLancamentoDTO, lancamento);
        preencher(lancamento, salvarLancamentoDTO);
        movimentarSaldos(lancamento, BigDecimal.ONE);

        return lancamentoMapper.toDTO(lancamento);
    }

    @Transactional
    public void deletar(UUID id) {
        log.info("Deletando o lançamento... - ID: [{}]", id);
        var lancamento = buscar(id);

        movimentarSaldos(lancamento, BigDecimal.ONE.negate());
        lancamentoRepository.delete(lancamento);
    }

    private Lancamento buscar(UUID id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Lançamento não encontrado! - ID: [{}]", id);
                    return new LancamentoNaoEncontradoException("Lançamento não encontrado!");
                });
    }

    private void preencher(Lancamento lancamento, SalvarLancamentoDTO salvarLancamentoDTO) {
        validarSituacao(salvarLancamentoDTO);

        lancamento.setDescricao(salvarLancamentoDTO.descricao().strip());
        lancamento.setObservacoes(textoOuNulo(salvarLancamentoDTO.observacoes()));

        lancamento.setConta(null);
        lancamento.setCartao(null);
        lancamento.setCategoria(null);
        lancamento.setContaDestino(null);

        vincularOrigem(lancamento, salvarLancamentoDTO.idOrigem());

        if (salvarLancamentoDTO.tipo() == TipoLancamento.TRANSFERENCIA) {
            vincularContaDestino(lancamento, salvarLancamentoDTO.idContaDestino());
        } else {
            vincularCategoria(lancamento, salvarLancamentoDTO.idCategoria(), salvarLancamentoDTO.tipo());
        }

        validarForma(lancamento, salvarLancamentoDTO.forma());
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

    private void vincularCategoria(Lancamento lancamento, UUID idCategoria, TipoLancamento tipo) {
        var categoria = Optional.ofNullable(idCategoria)
                .flatMap(categoriaRepository::findById)
                .orElseThrow(() -> {
                    log.error("A categoria informada não existe!");
                    return new CategoriaInexistenteException("A categoria informada não existe!");
                });

        if (tipo == TipoLancamento.DESPESA && categoria.getTipo() != TipoCategoria.DESPESA) {
            log.error("Escolha uma categoria de despesa!");
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        if (tipo == TipoLancamento.RECEITA && categoria.getTipo() != TipoCategoria.RECEITA) {
            log.error("Escolha uma categoria de receita!");
            throw new CategoriaDeDespesaException("Escolha uma categoria de receita!");
        }

        lancamento.setCategoria(categoria);
    }

    private static void movimentarSaldos(Lancamento lancamento, BigDecimal sentido) {
        if (lancamento.getSituacao() != SituacaoLancamento.PAGO) return;

        var valor = lancamento.getValor().multiply(sentido);

        switch (lancamento.getTipo()) {
            case RECEITA -> movimentar(lancamento.getConta(), valor);
            case DESPESA -> movimentar(lancamento.getConta(), valor.negate());
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
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}