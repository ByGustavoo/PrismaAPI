package br.com.prismaapi.service.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.exceptions.CategoriaDeDespesaException;
import br.com.prismaapi.exceptions.CategoriaDeReceitaException;
import br.com.prismaapi.exceptions.CategoriaInexistenteException;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import br.com.prismaapi.util.TextoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;

    @Cacheable("categorias")
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar(TipoCategoria tipo) {
        log.info("Listando as categorias... - Tipo: {}", tipo);
        var categorias = tipo == null ? categoriaRepository.findAll() : categoriaRepository.findByTipo(tipo);

        return categorias.stream()
                .map(categoriaMapper::toDTO)
                .sorted(Comparator.comparing(CategoriaDTO::nome, TextoUtil.ORDEM_ALFABETICA))
                .toList();
    }

    public Categoria buscarDoTipo(UUID idCategoria, TipoCategoria tipo) {
        var categoria = Optional.ofNullable(idCategoria)
                .flatMap(categoriaRepository::findById)
                .orElseThrow(() -> {
                    log.error("A categoria informada não existe!");
                    return new CategoriaInexistenteException("A categoria informada não existe!");
                });

        if (tipo == TipoCategoria.DESPESA && categoria.getTipo() != TipoCategoria.DESPESA) {
            log.error("Escolha uma categoria de despesa!");
            throw new CategoriaDeReceitaException("Escolha uma categoria de despesa!");
        }

        if (tipo == TipoCategoria.RECEITA && categoria.getTipo() != TipoCategoria.RECEITA) {
            log.error("Escolha uma categoria de receita!");
            throw new CategoriaDeDespesaException("Escolha uma categoria de receita!");
        }

        return categoria;
    }
}