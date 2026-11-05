package br.com.prismaapi.model.entity.recorrente;

import br.com.prismaapi.enums.Frequencia;
import br.com.prismaapi.enums.SituacaoRecorrente;
import br.com.prismaapi.enums.TipoRecorrente;
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
@Table(name = "recorrentes", schema = "prisma")
public class Recorrente {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoRecorrente tipo;

    @Column(nullable = false, length = 160)
    private String descricao;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private Frequencia frequencia;

    @Column(name = "proximo_vencimento", nullable = false)
    private LocalDate proximoVencimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SituacaoRecorrente situacao;

    @Column(name = "lancar_automaticamente", nullable = false)
    private Boolean lancarAutomaticamente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_categoria")
    private Categoria categoria;

    @JoinColumn(name = "id_conta")
    @ManyToOne(fetch = FetchType.LAZY)
    private Conta conta;

    @JoinColumn(name = "id_cartao")
    @ManyToOne(fetch = FetchType.LAZY)
    private Cartao cartao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
