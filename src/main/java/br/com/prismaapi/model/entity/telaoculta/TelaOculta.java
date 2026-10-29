package br.com.prismaapi.model.entity.telaoculta;

import br.com.prismaapi.enums.Tela;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "telas_ocultas", schema = "prisma")
public class TelaOculta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Tela tela;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;
}