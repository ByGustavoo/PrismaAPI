package br.com.prismaapi.model.entity.conta;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoConta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "contas", schema = "prismaapi")
public class Conta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 80)
    private String instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoConta tipo;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal saldo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Situacao situacao;

    @Column(name = "incluir_no_total", nullable = false)
    private Boolean incluirNoTotal;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
