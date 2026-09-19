package br.com.prismaapi.service.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.exceptions.CartaoComHistoricoException;
import br.com.prismaapi.exceptions.CartaoNaoEncontradoException;
import br.com.prismaapi.exceptions.ContaVinculadaInexistenteException;
import br.com.prismaapi.exceptions.RequisicaoInvalidaException;
import br.com.prismaapi.model.dto.cartao.CartaoDTO;
import br.com.prismaapi.model.dto.cartao.SalvarCartaoDTO;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.conta.Conta;
import br.com.prismaapi.model.mapper.cartao.CartaoMapper;
import br.com.prismaapi.repository.cartao.CartaoRepository;
import br.com.prismaapi.repository.compraparcelada.CompraParceladaRepository;
import br.com.prismaapi.repository.conta.ContaRepository;
import br.com.prismaapi.repository.despesarecorrente.DespesaRecorrenteRepository;
import br.com.prismaapi.repository.lancamento.LancamentoRepository;
import br.com.prismaapi.service.fatura.FaturaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartaoService {

    private final CartaoMapper cartaoMapper;
    private final FaturaService faturaService;
    private final ContaRepository contaRepository;
    private final CartaoRepository cartaoRepository;
    private final LancamentoRepository lancamentoRepository;
    private final CompraParceladaRepository compraParceladaRepository;
    private final DespesaRecorrenteRepository despesaRecorrenteRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));
    private static final List<TipoCartao> ORDEM_DOS_TIPOS = List.of(TipoCartao.CREDITO, TipoCartao.DEBITO, TipoCartao.VALE_ALIMENTACAO, TipoCartao.VALE_REFEICAO);

    @Cacheable("cartoes")
    @Transactional(readOnly = true)
    public List<CartaoDTO> listar() {
        log.info("Listando os cartões...");
        var cartoes = cartaoRepository.buscarComConta();
        var limitesComprometidos = faturaService.limitesComprometidos(cartoes, LocalDate.now());

        return cartoes.stream()
                .map(cartao -> cartaoMapper.toDTO(cartao, limitesComprometidos.get(cartao.getId())))
                .sorted(Comparator.comparing((CartaoDTO cartao) -> ORDEM_DOS_TIPOS.indexOf(cartao.tipo()))
                        .thenComparing(cartao -> cartao.situacao() != Situacao.ATIVO)
                        .thenComparing(CartaoDTO::nome, ORDEM_ALFABETICA)
                        .thenComparing(CartaoDTO::instituicao, ORDEM_ALFABETICA))
                .toList();
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "compras-parceladas", "contas", "dashboard", "despesas-recorrentes", "faturas", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public CartaoDTO salvar(SalvarCartaoDTO salvarCartaoDTO) {
        log.info("Salvando o cartão... - Nome: {} - Tipo: {}", salvarCartaoDTO.nome(), salvarCartaoDTO.tipo());
        var cartao = cartaoMapper.toEntity(salvarCartaoDTO);
        preencher(cartao, salvarCartaoDTO);

        return toDTO(cartaoRepository.save(cartao));
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "compras-parceladas", "contas", "dashboard", "despesas-recorrentes", "faturas", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public CartaoDTO atualizar(UUID id, SalvarCartaoDTO salvarCartaoDTO) {
        log.info("Atualizando o cartão... - ID: [{}]", id);
        var cartao = buscar(id);

        cartaoMapper.updateEntity(salvarCartaoDTO, cartao);
        preencher(cartao, salvarCartaoDTO);

        return toDTO(cartao);
    }

    @Transactional
    @CacheEvict(value = {"avisos", "cartoes", "compras-parceladas", "contas", "dashboard", "despesas-recorrentes", "faturas", "lancamentos", "previsao", "relatorios"}, allEntries = true)
    public void deletar(UUID id) {
        log.info("Deletando o cartão... - ID: [{}]", id);
        var cartao = buscar(id);

        validarHistorico(id);

        cartaoRepository.delete(cartao);
    }

    private Cartao buscar(UUID id) {
        return cartaoRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Cartão não encontrado! - ID: [{}]", id);
                    return new CartaoNaoEncontradoException("Cartão não encontrado!");
                });
    }

    private void validarHistorico(UUID id) {
        var quantidade = lancamentoRepository.countByCartaoId(id) + compraParceladaRepository.countByCartaoId(id) + despesaRecorrenteRepository.countByCartaoId(id);

        if (quantidade > 0) {
            var registros = quantidade == 1 ? "registro" : "registros";
            log.error("Este cartão tem {} {} no histórico. Marque-o como inativo para tirá-lo dos lançamentos sem apagar o passado!", quantidade, registros);
            throw new CartaoComHistoricoException("Este cartão tem %d %s no histórico. Marque-o como inativo para tirá-lo dos lançamentos sem apagar o passado!".formatted(quantidade, registros));
        }
    }

    private CartaoDTO toDTO(Cartao cartao) {
        var limitesComprometidos = faturaService.limitesComprometidos(List.of(cartao), LocalDate.now());
        return cartaoMapper.toDTO(cartao, limitesComprometidos.get(cartao.getId()));
    }

    private void preencher(Cartao cartao, SalvarCartaoDTO salvarCartaoDTO) {
        cartao.setNome(salvarCartaoDTO.nome().strip());
        cartao.setInstituicao(salvarCartaoDTO.instituicao().strip());
        cartao.setBandeira(textoOuNulo(salvarCartaoDTO.bandeira()));
        cartao.setUltimosDigitos(textoOuNulo(salvarCartaoDTO.ultimosDigitos()));

        cartao.setConta(null);
        cartao.setSaldo(null);
        cartao.setLimiteCredito(null);
        cartao.setDiaFechamento(null);
        cartao.setDiaVencimento(null);

        switch (salvarCartaoDTO.tipo()) {
            case CREDITO -> {
                cartao.setLimiteCredito(exigir(salvarCartaoDTO.limiteCredito(), "Informe o limite do cartão!"));
                cartao.setDiaFechamento(exigir(salvarCartaoDTO.diaFechamento(), "Informe um dia de fechamento entre 1 e 31!"));
                cartao.setDiaVencimento(exigir(salvarCartaoDTO.diaVencimento(), "Informe um dia de vencimento entre 1 e 31!"));
            }
            case DEBITO -> cartao.setConta(buscarContaVinculada(salvarCartaoDTO.idConta()));
            case VALE_ALIMENTACAO, VALE_REFEICAO -> cartao.setSaldo(exigir(salvarCartaoDTO.saldo(), "Informe um saldo válido para o cartão!"));
        }
    }

    private Conta buscarContaVinculada(UUID idConta) {
        return Optional.ofNullable(idConta)
                .flatMap(contaRepository::findById)
                .orElseThrow(() -> {
                    log.error("Escolha a conta vinculada ao cartão de débito!");
                    return new ContaVinculadaInexistenteException("Escolha a conta vinculada ao cartão de débito!");
                });
    }

    private static <T> T exigir(T valor, String mensagem) {
        if (valor == null) {
            log.warn(mensagem);
            throw new RequisicaoInvalidaException(mensagem);
        }

        return valor;
    }

    private static String textoOuNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.strip();
    }
}