package com.controlpagina.repository;

import com.controlpagina.entity.Noticia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface NoticiaRepository extends JpaRepository<Noticia, Long> {
    List<Noticia> findAllByOrderByFechaPublicacionDesc();
    List<Noticia> findByActivoTrueOrderByFechaPublicacionDesc();
    Optional<Noticia> findBySlug(String slug);
    boolean existsBySlug(String slug);
}
