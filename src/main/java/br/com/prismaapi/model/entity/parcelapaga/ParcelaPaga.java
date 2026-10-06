package br.com.prismaapi.model.entity.parcelapaga;

import br.com.prismaapi.model.entity.compraparcelada.CompraParcelada;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "parcelas_pagas", schema = "prisma")
public class ParcelaPaga {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @JoinColumn(name = "id_compra_parcelada", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private CompraParcelada compraParcelada;

    @Column(nullable = false)
    private Short numero;

    @Column(name = "data_pagamento", nullable = false)
    private LocalDate dataPagamento;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

}