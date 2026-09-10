package br.com.prismaapi.service.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.exceptions.ContaComLancamentosException;
import br.com.prismaapi.exceptions.ContaDuplicadaException;
import br.com.prismaapi.exceptions.ContaNaoEncontradaException;
import br.com.prismaapi.model.dto.conta.ContaDTO;
import br.com.prismaapi.model.dto.conta.OrigemDTO;
import br.com.prismaapi.model.dto.conta.SalvarContaDTO;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.mapper.cartao.CartaoMapper;
import br.com.prismaapi.model.mapper.conta.ContaMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaMapper contaMapper;
    private final CartaoMapper cartaoMapper;
    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Transactional(readOnly = true)
    public List<ContaDTO> listar() {
        return contaRepository.findAll()
                .stream()
                .map(contaMapper::toDTO)
                .sorted(Comparator.comparing((ContaDTO conta) -> conta.situacao() != Situacao.ATIVO)
                        .thenComparing(ContaDTO::nome, ORDEM_ALFABETICA)
                        .thenComparing(ContaDTO::instituicao, ORDEM_ALFABETICA))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrigemDTO> listarOrigens() {
        var contas = contaRepository.findBySituacao(Situacao.ATIVO)
                .stream()
                .map(contaMapper::toOrigemDTO)
                .sorted(Comparator.comparing(OrigemDTO::nome, ORDEM_ALFABETICA));

        var cartoes = cartaoRepository.findBySituacaoAndTipoNot(Situacao.ATIVO, TipoCartao.DEBITO)
                .stream()
                .map(cartaoMapper::toOrigemDTO)
                .sorted(Comparator.comparing(OrigemDTO::nome, ORDEM_ALFABETICA));

        return Stream.concat(contas, cartoes).toList();
    }

    @Transactional
    public ContaDTO salvar(SalvarContaDTO salvarContaDTO) {
        validarDuplicidade(salvarContaDTO, null);

        var conta = contaMapper.toEntity(salvarContaDTO);
        preencher(conta, salvarContaDTO);

        return contaMapper.toDTO(contaRepository.save(conta));
    }

    @Transactional
    public ContaDTO atualizar(UUID id, SalvarContaDTO salvarContaDTO) {
        var conta = buscar(id);

        validarDuplicidade(salvarContaDTO, id);

        contaMapper.updateEntity(salvarContaDTO, conta);
        preencher(conta, salvarContaDTO);

        return contaMapper.toDTO(conta);
    }

    @Transactional
    public void deletar(UUID id) {
        var conta = buscar(id);

        validarHistorico(id);

        contaRepository.delete(conta);
    }

    private Conta buscar(UUID id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> new ContaNaoEncontradaException("Conta não encontrada!"));
    }

    private void validarDuplicidade(SalvarContaDTO salvarContaDTO, UUID id) {
        var nome = salvarContaDTO.nome().strip();
        var instituicao = salvarContaDTO.instituicao().strip();

        var duplicada = id == null
                ? contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCase(nome, instituicao)
                : contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseAndIdNot(nome, instituicao, id);

        if (duplicada) {
            throw new ContaDuplicadaException("Já existe uma conta com esse nome nessa instituição!");
        }
    }

    private void validarHistorico(UUID id) {
        var quantidade = lancamentoRepository.countByContaIdOrContaDestinoId(id, id);

        if (quantidade > 0) {
            var lancamentos = quantidade == 1 ? "lançamento" : "lançamentos";
            throw new ContaComLancamentosException("Esta conta tem %d %s no histórico. Marque-a como inativa para tirá-la do saldo sem apagar o passado!".formatted(quantidade, lancamentos));
        }
    }

    private static void preencher(Conta conta, SalvarContaDTO salvarContaDTO) {
        conta.setNome(salvarContaDTO.nome().strip());
        conta.setInstituicao(salvarContaDTO.instituicao().strip());
        conta.setIncluirNoTotal(salvarContaDTO.situacao() == Situacao.ATIVO && salvarContaDTO.incluirNoTotal());
    }
}