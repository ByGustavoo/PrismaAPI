package br.com.prismaapi.model.entity.meta;

import br.com.prismaapi.enums.SituacaoMeta;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "metas", schema = "prismaapi")
public class Meta {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(length = 2048)
    private String url;

    @Column(name = "url_imagem", length = 2048)
    private String urlImagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 12)
    private SituacaoMeta situacao;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @CreationTimestamp
    @Column(name = "data_criacao", nullable = false, updatable = false)
    private OffsetDateTime dataCriacao;

    @UpdateTimestamp
    @Column(name = "data_atualizacao", nullable = false)
    private OffsetDateTime dataAtualizacao;

}
