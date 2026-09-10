package br.com.prismaapi.service.categoria;

import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.model.dto.categoria.CategoriaDTO;
import br.com.prismaapi.model.mapper.categoria.CategoriaMapper;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Collator;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaMapper categoriaMapper;
    private final CategoriaRepository categoriaRepository;
    private static final Collator ORDEM_ALFABETICA = Collator.getInstance(Locale.forLanguageTag("pt-BR"));

    @Transactional(readOnly = true)
    public List<CategoriaDTO> listar(TipoCategoria tipo) {
        var categorias = tipo == null ? categoriaRepository.findAll() : categoriaRepository.findByTipo(tipo);

        return categorias.stream()
                .map(categoriaMapper::toDTO)
                .sorted(Comparator.comparing(CategoriaDTO::nome, ORDEM_ALFABETICA))
                .toList();
    }
}