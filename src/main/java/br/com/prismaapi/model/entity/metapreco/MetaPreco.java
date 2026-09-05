package br.com.prismaapi.model.entity.metapreco;

import br.com.prismaapi.model.entity.meta.Meta;
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
@Table(name = "metas_precos", schema = "prismaapi")
public class MetaPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_meta", nullable = false)
    private Meta meta;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal preco;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

}
