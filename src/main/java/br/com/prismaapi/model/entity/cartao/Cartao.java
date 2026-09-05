package br.com.prismaapi.model.entity.cartao;

import br.com.prismaapi.enums.Situacao;
import br.com.prismaapi.enums.TipoCartao;
import br.com.prismaapi.model.entity.conta.Conta;
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
@Table(name = "cartoes", schema = "prismaapi")
public class Cartao {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 80)
    private String instituicao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoCartao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Situacao situacao;

    @Column(length = 40)
    private String bandeira;

    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "limite_credito", precision = 14, scale = 2)
    private BigDecimal limiteCredito;

    @Column(name = "dia_fechamento")
    private Short diaFechamento;

    @Column(name = "dia_vencimento")
    private Short diaVencimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_conta")
    private Conta conta;

    @Column(precision = 14, scale = 2)
    private BigDecimal saldo;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
