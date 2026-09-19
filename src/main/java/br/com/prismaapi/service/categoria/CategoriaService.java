package br.com.prismaapi.service.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Cacheable("categorias")
    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar(TipoCategoria tipo) {
        log.info("Listando as categorias... - Tipo: {}", tipo);
        var categorias = tipo == null ? categoriaRepository.findAll() : categoriaRepository.findByTipo(tipo);

        return categorias.stream()
                .map(categoriaMapper::toDTO)
                .sorted(Comparator.comparing(CategoriaDTO::nome, ORDEM_ALFABETICA))
                .toList();
    }
}