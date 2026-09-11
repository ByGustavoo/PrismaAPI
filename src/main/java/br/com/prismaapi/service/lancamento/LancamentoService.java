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
import br.com.prismaapi.model.entity.lancamento.Lancamento;
import br.com.prismaapi.model.mapper.lancamento.LancamentoMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.repository.lancamento.LancamentoSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
        validar(filtro);

        return lancamentoRepository.findAll(LancamentoSpecification.filtrar(filtro), MAIS_RECENTES_PRIMEIRO)
                .stream()
                .map(lancamentoMapper::toDTO)
                .toList();
    }

    @Transactional
    public LancamentoDTO salvar(SalvarLancamentoDTO salvarLancamentoDTO) {
        var lancamento = lancamentoMapper.toEntity(salvarLancamentoDTO);
        preencher(lancamento, salvarLancamentoDTO);

        return lancamentoMapper.toDTO(lancamentoRepository.save(lancamento));
    }

    @Transactional
    public LancamentoDTO atualizar(UUID id, SalvarLancamentoDTO salvarLancamentoDTO) {
        var lancamento = buscar(id);

        lancamentoMapper.updateEntity(salvarLancamentoDTO, lancamento);
        preencher(lancamento, salvarLancamentoDTO);

        return lancamentoMapper.toDTO(lancamento);
    }

    @Transactional
    public void deletar(UUID id) {
        lancamentoRepository.delete(buscar(id));
    }

    private Lancamento buscar(UUID id) {
        return lancamentoRepository.findById(id)
                .orElseThrow(() -> new LancamentoNaoEncontradoException("Lançamento não encontrado!"));
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
                .orElseThrow(() -> new OrigemInexistenteException("A conta informada não existe!"));

        if (cartao.getTipo() == TipoCartao.DEBITO) {
            throw new OrigemCartaoDeDebitoException("O cartão de débito não é origem de lançamento: escolha a conta que ele movimenta!");
        }

        lancamento.setCartao(cartao);
    }

    private void vincularContaDestino(Lancamento lancamento, UUID idContaDestino) {
        var contaDestino = Optional.ofNullable(idContaDestino)
                .flatMap(contaRepository::findById)
                .orElseThrow(() -> new ContaDestinoInexistenteException("A conta de destino informada não existe!"));

        if (lancamento.getConta() == null) {
            throw new OrigemTransferenciaInvalidaException("A transferência precisa sair de uma conta!");
        }

        if (contaDestino.getId().equals(lancamento.getConta().getId())) {
            throw new ContaDestinoIgualOrigemException("A conta de destino precisa ser diferente da origem!");
        }

        lancamento.setContaDestino(contaDestino);
    }

    private void vincularCategoria(Lancamento lancamento, UUID idCategoria, TipoLancamento tipo) {
        var categoria = Optional.ofNullable(idCategoria)
                .flatMap(categoriaRepository::findById)
                .orElseThrow(() -> new CategoriaInexistenteException("A categoria informada não existe!"));

        if (tipo == TipoLancamento.DESPESA && categoria.getTipo() != TipoCategoria.DESPESA) {
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        if (tipo == TipoLancamento.RECEITA && categoria.getTipo() != TipoCategoria.RECEITA) {
            throw new CategoriaDeDespesaException("Escolha uma categoria de receita!");
        }

        lancamento.setCategoria(categoria);
    }

    private static void validarForma(Lancamento lancamento, FormaLancamento forma) {
        var noCartao = lancamento.getCartao() != null;

        if (noCartao && forma != FormaLancamento.CARTAO_CREDITO) {
            throw new FormaIncompativelComOrigemException("Um lançamento no cartão precisa ter a forma de pagamento cartão!");
        }

        if (!noCartao && forma == FormaLancamento.CARTAO_CREDITO) {
            throw new FormaIncompativelComOrigemException("Um lançamento na conta não pode ter a forma de pagamento cartão de crédito!");
        }
    }

    private static void validarSituacao(SalvarLancamentoDTO salvarLancamentoDTO) {
        if (salvarLancamentoDTO.situacao() == SituacaoLancamento.PAGO && salvarLancamentoDTO.data().isAfter(LocalDate.now())) {
            throw new LancamentoFuturoConcluidoException("Um lançamento com data futura não pode estar concluído: marque como agendado ou pendente!");
        }
    }

    private static void validar(FiltroLancamentoDTO filtro) {
        if (filtro.dataInicial() != null && filtro.dataFinal() != null && filtro.dataInicial().isAfter(filtro.dataFinal())) {
            throw new RequisicaoInvalidaException("O período informado é inválido!");
        }
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}