package com.controlpagina.repository;

import com.controlpagina.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    List<Trabajador> findAllByOrderByPrimerApellidoAscPrimerNombreAsc();
    List<Trabajador> findByActivoTrueOrderByPrimerApellidoAscPrimerNombreAsc();
    Optional<Trabajador> findByCodigoPublico(String codigoPublico);
    boolean existsByCodigoPublico(String codigoPublico);
    boolean existsByCodigoPublicoAndIdNot(String codigoPublico, Long id);
}
