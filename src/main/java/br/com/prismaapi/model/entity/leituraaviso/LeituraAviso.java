package br.com.prismaapi.model.entity.leituraaviso;

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
@Table(name = "leituras_avisos", schema = "prisma")
public class LeituraAviso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String chave;

    @Column(nullable = false, length = 200)
    private String assinatura;

    @Column(name = "data_fim_adiamento")
    private LocalDate dataFimAdiamento;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}