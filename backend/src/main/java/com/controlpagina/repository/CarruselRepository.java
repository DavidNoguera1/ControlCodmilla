package com.controlpagina.repository;

import com.controlpagina.entity.Carrusel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface CarruselRepository extends JpaRepository<Carrusel, Long> {
    List<Carrusel> findAllByOrderByOrdenAsc();
    List<Carrusel> findByActivoTrueOrderByOrdenAsc();

    @Query("SELECT COALESCE(MAX(c.orden), 0) FROM Carrusel c")
    int findMaxOrden();
}
