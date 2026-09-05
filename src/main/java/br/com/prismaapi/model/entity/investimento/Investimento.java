package br.com.prismaapi.model.entity.investimento;

import br.com.prismaapi.enums.ClasseAtivo;
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
@Table(name = "investimentos", schema = "prismaapi")
public class Investimento {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "classe_ativo", nullable = false, length = 16)
    private ClasseAtivo classeAtivo;

    @Column(nullable = false, length = 80)
    private String instituicao;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal aportado;

    @Column(name = "valor_atual", nullable = false, precision = 14, scale = 2)
    private BigDecimal valorAtual;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
