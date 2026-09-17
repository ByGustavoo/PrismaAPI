package br.com.prismaapi.model.entity.movimentacaoinvestimento;

import br.com.prismaapi.enums.TipoMovimentacaoInvestimento;
import br.com.prismaapi.model.entity.investimento.Investimento;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "movimentacoes_investimento", schema = "prisma")
public class MovimentacaoInvestimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_investimento", nullable = false)
    private Investimento investimento;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoMovimentacaoInvestimento tipo;

    @Column(nullable = false)
    private LocalDate data;

    @Column(precision = 14, scale = 2)
    private BigDecimal valor;

    @Column(name = "saldo_informado", precision = 14, scale = 2)
    private BigDecimal saldoInformado;

    @Column(length = 160)
    private String descricao;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}