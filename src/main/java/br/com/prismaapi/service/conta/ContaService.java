package br.com.prismaapi.service.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.exceptions.ContaComCartaoVinculadoException;
import br.com.prismaapi.exceptions.ContaComHistoricoException;
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
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContaService {

    private final ContaMapper contaMapper;
    private final CartaoMapper cartaoMapper;
    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Cacheable("contas")
    @Transactional(readOnly = true)
    public List<ContaDTO> listar() {
        log.info("Listando as contas...");
        return contaRepository.findAll()
                .stream()
                .map(contaMapper::toDTO)
                .sorted(Comparator.comparing((ContaDTO conta) -> conta.situacao() != Situacao.ATIVO)
                        .thenComparing(ContaDTO::nome, ORDEM_ALFABETICA)
                        .thenComparing(ContaDTO::instituicao, ORDEM_ALFABETICA))
                .toList();
    }

    @Cacheable("contas")
    @Transactional(readOnly = true)
    public List<OrigemDTO> listarOrigens() {
        log.info("Listando as origens...");
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
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "despesas-recorrentes", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public ContaDTO salvar(SalvarContaDTO salvarContaDTO) {
        log.info("Salvando a conta... - Nome: {} - Instituição: {}", salvarContaDTO.nome(), salvarContaDTO.instituicao());
        validarDuplicidade(salvarContaDTO, null);

        var conta = contaMapper.toEntity(salvarContaDTO);
        preencher(conta, salvarContaDTO);

        return contaMapper.toDTO(contaRepository.save(conta));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "despesas-recorrentes", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public ContaDTO atualizar(UUID id, SalvarContaDTO salvarContaDTO) {
        log.info("Atualizando a conta... - ID: [{}]", id);
        var conta = buscar(id);

        validarDuplicidade(salvarContaDTO, id);

        contaMapper.updateEntity(salvarContaDTO, conta);
        preencher(conta, salvarContaDTO);

        return contaMapper.toDTO(conta);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "contas", "dashboard", "despesas-recorrentes", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public void deletar(UUID id) {
        log.info("Deletando a conta... - ID: [{}]", id);
        var conta = buscar(id);

        validarHistorico(id);
        validarCartoesVinculados(id);

        contaRepository.delete(conta);
    }

    private Conta buscar(UUID id) {
        return contaRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Conta não encontrada! - ID: [{}]", id);
                    return new ContaNaoEncontradaException("Conta não encontrada!");
                });
    }

    private void validarDuplicidade(SalvarContaDTO salvarContaDTO, UUID id) {
        var nome = salvarContaDTO.nome().strip();
        var instituicao = salvarContaDTO.instituicao().strip();

        var duplicada = id == null
                ? contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCase(nome, instituicao)
                : contaRepository.existsByNomeIgnoreCaseAndInstituicaoIgnoreCaseAndIdNot(nome, instituicao, id);

        if (duplicada) {
            log.error("Já existe uma conta com esse nome nessa instituição!");
            throw new ContaDuplicadaException("Já existe uma conta com esse nome nessa instituição!");
        }
    }

    private void validarHistorico(UUID id) {
        var quantidade = lancamentoRepository.countByContaIdOrContaDestinoId(id, id) + despesaRecorrenteRepository.countByContaId(id);

        if (quantidade > 0) {
            var registros = quantidade == 1 ? "registro" : "registros";
            log.error("Esta conta tem {} {} no histórico. Marque-a como inativa para tirá-la do saldo sem apagar o passado!", quantidade, registros);
            throw new ContaComHistoricoException("Esta conta tem %d %s no histórico. Marque-a como inativa para tirá-la do saldo sem apagar o passado!".formatted(quantidade, registros));
        }
    }

    private void validarCartoesVinculados(UUID id) {
        var quantidade = cartaoRepository.countByContaId(id);

        if (quantidade == 1) {
            log.error("Esta conta está vinculada a um cartão de débito. Troque a conta desse cartão ou exclua-o antes de excluir a conta!");
            throw new ContaComCartaoVinculadoException("Esta conta está vinculada a um cartão de débito. Troque a conta desse cartão ou exclua-o antes de excluir a conta!");
        }

        if (quantidade > 1) {
            log.error("Esta conta está vinculada a {} cartões de débito. Troque a conta desses cartões ou exclua-os antes de excluir a conta!", quantidade);
            throw new ContaComCartaoVinculadoException("Esta conta está vinculada a %d cartões de débito. Troque a conta desses cartões ou exclua-os antes de excluir a conta!".formatted(quantidade));
        }
    }

    private static void preencher(Conta conta, SalvarContaDTO salvarContaDTO) {
        conta.setNome(salvarContaDTO.nome().strip());
        conta.setInstituicao(salvarContaDTO.instituicao().strip());
        conta.setIncluirNoTotal(salvarContaDTO.situacao() == Situacao.ATIVO && salvarContaDTO.incluirNoTotal());
    }
}