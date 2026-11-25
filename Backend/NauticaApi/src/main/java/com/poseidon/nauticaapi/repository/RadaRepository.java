package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.dto.response.RadaCompatibleResponse;
import com.poseidon.nauticaapi.entity.Rada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RadaRepository extends JpaRepository<Rada, Long> {

    List<Rada> findAllByOrderByCodigo();

    /**
     * HU13: radas LIBRES donde cabe la nave (eslora, manga y calado), ordenadas por la
     * menor holgura (se aprovecha mejor el amarre) y luego por tarifa.
     * aprovechamiento = % del área de la rada (eslora x manga) que ocupa la nave.
     */
    @Query(value = """
            WITH nave AS (
                SELECT e.eslora, e.manga, e.calado
                FROM embarcaciones e
                WHERE e.id = :embarcacionId
            )
            SELECT r.id AS "id",
                   CAST(r.codigo AS text) AS "codigo",
                   CAST(r.muelle AS text) AS "muelle",
                   CAST(r.eslora_max AS double precision) AS "esloraMax",
                   CAST(r.manga_max AS double precision) AS "mangaMax",
                   CAST(r.calado_max AS double precision) AS "caladoMax",
                   CAST(r.tarifa_mensual AS double precision) AS "tarifaMensual",
                   CAST(r.eslora_max - n.eslora AS double precision) AS "holguraEslora",
                   CAST(r.manga_max - n.manga AS double precision) AS "holguraManga",
                   CAST(ROUND(100 * (n.eslora * n.manga) / (r.eslora_max * r.manga_max), 1) AS double precision) AS "aprovechamiento",
                   CAST(ROW_NUMBER() OVER (ORDER BY (r.eslora_max - n.eslora) + (r.manga_max - n.manga),
                                                    r.tarifa_mensual, r.codigo) AS bigint) AS "ranking"
            FROM radas r
            CROSS JOIN nave n
            WHERE r.estado = 'LIBRE'
              AND r.eslora_max >= n.eslora
              AND r.manga_max >= n.manga
              AND r.calado_max >= n.calado
            ORDER BY 11
            LIMIT 8
            """, nativeQuery = true)
    List<RadaCompatibleResponse> buscarCompatibles(@Param("embarcacionId") Long embarcacionId);
}
