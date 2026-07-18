package com.controlpagina.repository;

import com.controlpagina.entity.PDFDocumento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.Optional;

public interface PDFDocumentoRepository extends JpaRepository<PDFDocumento, Long> {
    List<PDFDocumento> findAllByOrderByOrdenAsc();
    List<PDFDocumento> findByActivoTrueOrderByOrdenAsc();

    @Query("SELECT COALESCE(MAX(p.orden), 0) FROM PDFDocumento p")
    int findMaxOrden();
}
