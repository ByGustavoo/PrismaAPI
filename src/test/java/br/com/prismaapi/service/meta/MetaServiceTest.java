package br.com.prismaapi.service.meta;

import br.com.prismaapi.config.AbstractTest;
import br.com.prismaapi.enums.SituacaoMeta;
import br.com.prismaapi.model.dto.meta.AtualizarMetaDTO;
import br.com.prismaapi.model.dto.meta.SalvarMetaDTO;
import br.com.prismaapi.model.dto.metapreco.SalvarMetaPrecoDTO;
import br.com.prismaapi.repository.meta.MetaRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@SpringBootTest
class MetaServiceTest extends AbstractTest {

    @Autowired
    private MetaService metaService;

    @Autowired
    private MetaRepository metaRepository;

    @Test
    void listarTest() {
        var metas = Assertions.assertDoesNotThrow(() -> metaService.listar(SituacaoMeta.ACOMPANHANDO, null));
        Assertions.assertNotNull(metas);
    }

    @Test
    void salvarTest() {
        var salvarMetaDTO = new SalvarMetaDTO(
                "Tênis Nike Air Max",
                "https://www.nike.com/br/",
                "https://www.nike.com/br/images/tenis-air-max.jpg",
                new BigDecimal("799.90"),
                LocalDate.of(2026, 9, 25),
                SituacaoMeta.ACOMPANHANDO,
                "Comprar quando houver uma boa promoção.");

        var meta = Assertions.assertDoesNotThrow(() -> metaService.salvar(salvarMetaDTO));
        Assertions.assertNotNull(meta);
    }

    @Test
    void atualizarTest() {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Cadeira ergonômica")).getId();

        var atualizarMetaDTO = new AtualizarMetaDTO(
                "Cadeira ergonômica",
                "https://example.com/cadeira",
                null,
                SituacaoMeta.COMPRADA,
                "Comprada com cupom de desconto.");

        var meta = Assertions.assertDoesNotThrow(() -> metaService.atualizar(idMeta, atualizarMetaDTO));
        Assertions.assertNotNull(meta);
    }

    @Test
    void registrarPrecoTest() {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Notebook para edição")).getId();

        var salvarMetaPrecoDTO = new SalvarMetaPrecoDTO(
                new BigDecimal("7500.00"),
                LocalDate.now(),
                "Promoção relâmpago");

        var meta = Assertions.assertDoesNotThrow(() -> metaService.registrarPreco(idMeta, salvarMetaPrecoDTO));
        Assertions.assertNotNull(meta);
    }

    @Test
    void deletarTest() {
        var idMeta = buscar(metaRepository, meta -> meta.getNome().equals("Console de videogame")).getId();

        Assertions.assertDoesNotThrow(() -> metaService.deletar(idMeta));
    }
}