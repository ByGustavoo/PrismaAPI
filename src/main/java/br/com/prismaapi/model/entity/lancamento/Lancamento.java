package br.com.prismaapi.model.entity.lancamento;

import br.com.prismaapi.enums.FormaLancamento;
import br.com.prismaapi.enums.SituacaoLancamento;
import br.com.prismaapi.enums.TipoLancamento;
import br.com.prismaapi.model.entity.cartao.Cartao;
import br.com.prismaapi.model.entity.categoria.Categoria;
import br.com.prismaapi.model.entity.conta.Conta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "lancamentos", schema = "prismaapi")
public class Lancamento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 14)
    private TipoLancamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoLancamento situacao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 14)
    private FormaLancamento forma;

    @Column(nullable = false)
    private LocalDate data;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta")
    private Conta conta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cartao")
    private Cartao cartao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta_destino")
    private Conta contaDestino;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
