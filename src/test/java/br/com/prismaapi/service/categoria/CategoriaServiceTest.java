package br.com.prismaapi.service.categoria;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.TipoCategoria;
import br.com.prismaapi.repository.categoria.CategoriaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CategoriaServiceTest extends AbstractTest {

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Test
    void listarTest() {
        var categorias = Assertions.assertDoesNotThrow(() -> categoriaService.listar(TipoCategoria.DESPESA));
        Assertions.assertNotNull(categorias);
    }

    @Test
    void buscarDoTipoTest() {
        var idCategoria = buscar(categoriaRepository, categoria -> categoria.getNome().equals("Outras despesas")).getId();

        var categoria = Assertions.assertDoesNotThrow(() -> categoriaService.buscarDoTipo(idCategoria, TipoCategoria.DESPESA));
        Assertions.assertNotNull(categoria);
    }
}