package br.com.prismaapi.model.entity.preferenciasavisos;

import br.com.prismaapi.enums.TipoAviso;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "preferencias_avisos", schema = "prisma")
public class PreferenciasAvisos {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "dias_antecedencia", nullable = false)
    private Short diasAntecedencia;

    @Column(name = "percentual_limite_cartao", nullable = false)
    private Short percentualLimiteCartao;

    @Column(name = "dias_lembrete_investimento", nullable = false)
    private Short diasLembreteInvestimento;

    @Column(name = "dias_lembrete_meta", nullable = false)
    private Short diasLembreteMeta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    @ElementCollection
    @CollectionTable(name = "tipos_aviso_desligados", schema = "prisma", joinColumns = @JoinColumn(name = "id_preferencias_avisos"))
    private Set<TipoAviso> tiposDesligados = new HashSet<>();

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;
}