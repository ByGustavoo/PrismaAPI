package br.com.prismaapi.repository.telaoculta;

import br.com.prismaapi.model.entity.telaoculta.TelaOculta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TelaOcultaRepository extends JpaRepository<TelaOculta, UUID> {
}