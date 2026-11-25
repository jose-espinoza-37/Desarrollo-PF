package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.dto.response.DocumentoResponse;
import com.poseidon.nauticaapi.entity.DocumentoEmbarcacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DocumentoEmbarcacionRepository extends JpaRepository<DocumentoEmbarcacion, Long> {

    /** Datos de los documentos de varias naves (sin cargar los archivos), en una sola consulta. */
    @Query("SELECT new com.poseidon.nauticaapi.dto.response.DocumentoResponse("
            + "d.id, d.tipoDocumento, d.nombreArchivo, d.contentType, d.tamano, d.fechaCarga, d.embarcacion.id) "
            + "FROM DocumentoEmbarcacion d WHERE d.embarcacion.id IN :ids ORDER BY d.fechaCarga")
    List<DocumentoResponse> listarPorEmbarcaciones(@Param("ids") Collection<Long> ids);

    Long countByEmbarcacion_Id(Long embarcacionId);
}
