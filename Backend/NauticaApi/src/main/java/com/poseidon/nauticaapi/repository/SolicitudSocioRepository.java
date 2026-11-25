package com.poseidon.nauticaapi.repository;

import com.poseidon.nauticaapi.dto.dashboard.KpiDashboardResponse;
import com.poseidon.nauticaapi.dto.response.ConteoDniResponse;
import com.poseidon.nauticaapi.dto.dashboard.MapaCalorResponse;
import com.poseidon.nauticaapi.dto.dashboard.PerfilEdadResponse;
import com.poseidon.nauticaapi.dto.dashboard.SerieTemporalResponse;
import com.poseidon.nauticaapi.dto.dashboard.TiempoAtencionResponse;
import com.poseidon.nauticaapi.entity.SolicitudSocio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SolicitudSocioRepository extends JpaRepository<SolicitudSocio, Long> {

    List<SolicitudSocio> findAllByOrderByFechaSolicitudDesc();

    List<SolicitudSocio> findByEstadoOrderByFechaSolicitudAsc(String estado);

    Optional<SolicitudSocio> findByCodigo(UUID codigo);

    boolean existsByDniAndEstadoIn(String dni, Collection<String> estados);

    boolean existsByCorreoAndEstadoIn(String correo, Collection<String> estados);

    Long countByDni(String dni);

    /** Cuántas solicitudes tiene cada DNI, en una sola consulta (evita una consulta por fila). */
    @Query("SELECT new com.poseidon.nauticaapi.dto.response.ConteoDniResponse(s.dni, COUNT(s)) "
            + "FROM SolicitudSocio s GROUP BY s.dni")
    List<ConteoDniResponse> contarPorDni();

    // ======================================================================
    //  Consultas estadísticas del dashboard (PostgreSQL). Parámetro: últimos N días.
    // ======================================================================

    /**
     * Indicadores del periodo (KPIs): tasas, media, mediana y desviación estándar del tiempo de atención,
     * edad promedio, reincidentes y correlación de Pearson entre edad y embarcaciones.
     */
    @Query(value = """
            WITH periodo AS (
                SELECT s.*,
                       EXTRACT(EPOCH FROM (COALESCE(s.fecha_resolucion, s.fecha_verificacion) - s.fecha_solicitud)) / 3600.0 AS horas_atencion,
                       DATE_PART('year', AGE(CAST(s.fecha_solicitud AS date), s.fecha_nacimiento)) AS edad
                FROM solicitudes_socio s
                WHERE s.fecha_solicitud >= CURRENT_DATE - CAST(:dias AS integer)
            ), atendidas AS (
                SELECT horas_atencion FROM periodo WHERE estado IN ('APROBADA', 'DESAPROBADA', 'NO_APTA')
            )
            SELECT
                CAST(COUNT(*) AS bigint) AS "totalSolicitudes",
                CAST(COUNT(*) FILTER (WHERE p.estado = 'PENDIENTE') AS bigint) AS "pendientesVerificacion",
                CAST(COUNT(*) FILTER (WHERE p.estado = 'VERIFICADA') AS bigint) AS "pendientesAprobacion",
                (SELECT CAST(COUNT(*) AS bigint) FROM usuarios u JOIN roles r ON r.id = u.rol_id
                  WHERE r.codigo = 'SOCIO' AND u.activo) AS "sociosActivos",
                CAST(COALESCE(ROUND(100.0 * COUNT(*) FILTER (WHERE p.estado = 'APROBADA')
                     / NULLIF(COUNT(*) FILTER (WHERE p.estado IN ('APROBADA', 'DESAPROBADA')), 0), 1), 0) AS double precision) AS "tasaAprobacion",
                CAST(COALESCE(ROUND(100.0 * COUNT(*) FILTER (WHERE p.estado = 'NO_APTA')
                     / NULLIF(COUNT(*) FILTER (WHERE p.clasificacion IS NOT NULL), 0), 1), 0) AS double precision) AS "tasaNoAptos",
                (SELECT CAST(ROUND(CAST(AVG(horas_atencion) AS numeric), 1) AS double precision) FROM atendidas) AS "promedioHorasAtencion",
                (SELECT CAST(ROUND(CAST(percentile_cont(0.5) WITHIN GROUP (ORDER BY horas_atencion) AS numeric), 1) AS double precision) FROM atendidas) AS "medianaHorasAtencion",
                (SELECT CAST(ROUND(CAST(stddev_samp(horas_atencion) AS numeric), 1) AS double precision) FROM atendidas) AS "desviacionHorasAtencion",
                CAST(ROUND(CAST(AVG(p.edad) AS numeric), 1) AS double precision) AS "edadPromedio",
                CAST(COUNT(*) FILTER (WHERE EXISTS (SELECT 1 FROM solicitudes_socio d
                      WHERE d.dni = p.dni AND d.id <> p.id AND d.fecha_solicitud < p.fecha_solicitud)) AS bigint) AS "reincidentes",
                CAST(ROUND(CAST(CORR(p.edad, p.cantidad_embarcaciones) AS numeric), 3) AS double precision) AS "correlacionEdadEmbarcaciones"
            FROM periodo p
            """, nativeQuery = true)
    KpiDashboardResponse obtenerKpis(@Param("dias") Integer dias);

    /**
     * Chart 1: serie diaria completa (generate_series rellena los días sin registros),
     * con media móvil de 7 días y total acumulado (funciones de ventana).
     */
    @Query(value = """
            WITH dias AS (
                SELECT CAST(d AS date) AS dia
                FROM generate_series(CURRENT_DATE - (CAST(:dias AS integer) - 1), CURRENT_DATE, INTERVAL '1 day') AS d
            ), conteo AS (
                SELECT CAST(fecha_solicitud AS date) AS dia,
                       COUNT(*) AS total,
                       COUNT(*) FILTER (WHERE estado = 'APROBADA') AS aprobadas
                FROM solicitudes_socio
                WHERE fecha_solicitud >= CURRENT_DATE - (CAST(:dias AS integer) - 1)
                GROUP BY CAST(fecha_solicitud AS date)
            )
            SELECT TO_CHAR(d.dia, 'YYYY-MM-DD') AS "fecha",
                   CAST(COALESCE(c.total, 0) AS bigint) AS "cantidad",
                   CAST(COALESCE(c.aprobadas, 0) AS bigint) AS "aprobadas",
                   CAST(ROUND(AVG(COALESCE(c.total, 0)) OVER (ORDER BY d.dia ROWS BETWEEN 6 PRECEDING AND CURRENT ROW), 2) AS double precision) AS "mediaMovil",
                   CAST(SUM(COALESCE(c.total, 0)) OVER (ORDER BY d.dia) AS bigint) AS "acumulado"
            FROM dias d
            LEFT JOIN conteo c ON c.dia = d.dia
            ORDER BY d.dia
            """, nativeQuery = true)
    List<SerieTemporalResponse> obtenerSerieTemporal(@Param("dias") Integer dias);

    /**
     * Chart 2: cuartiles (percentile_cont), mínimo, máximo, media y desviación
     * del tiempo de atención en horas, por clasificación de pago.
     */
    @Query(value = """
            WITH atendidas AS (
                SELECT s.clasificacion,
                       EXTRACT(EPOCH FROM (COALESCE(s.fecha_resolucion, s.fecha_verificacion) - s.fecha_solicitud)) / 3600.0 AS horas
                FROM solicitudes_socio s
                WHERE s.estado IN ('APROBADA', 'DESAPROBADA', 'NO_APTA')
                  AND s.fecha_solicitud >= CURRENT_DATE - CAST(:dias AS integer)
            )
            SELECT c.codigo AS "clasificacion",
                   c.descripcion AS "descripcion",
                   CAST(COUNT(a.horas) AS bigint) AS "cantidad",
                   CAST(ROUND(CAST(MIN(a.horas) AS numeric), 1) AS double precision) AS "minimo",
                   CAST(ROUND(CAST(percentile_cont(0.25) WITHIN GROUP (ORDER BY a.horas) AS numeric), 1) AS double precision) AS "q1",
                   CAST(ROUND(CAST(percentile_cont(0.50) WITHIN GROUP (ORDER BY a.horas) AS numeric), 1) AS double precision) AS "mediana",
                   CAST(ROUND(CAST(percentile_cont(0.75) WITHIN GROUP (ORDER BY a.horas) AS numeric), 1) AS double precision) AS "q3",
                   CAST(ROUND(CAST(MAX(a.horas) AS numeric), 1) AS double precision) AS "maximo",
                   CAST(ROUND(CAST(AVG(a.horas) AS numeric), 1) AS double precision) AS "promedio",
                   CAST(ROUND(CAST(stddev_samp(a.horas) AS numeric), 1) AS double precision) AS "desviacion"
            FROM clasificacion_pago c
            LEFT JOIN atendidas a ON a.clasificacion = c.codigo
            GROUP BY c.codigo, c.descripcion, c.orden
            ORDER BY c.orden
            """, nativeQuery = true)
    List<TiempoAtencionResponse> obtenerTiemposAtencion(@Param("dias") Integer dias);

    /**
     * Chart 3: matriz día de la semana x franja horaria (7 x 6) con el conteo y
     * el porcentaje sobre el total del periodo.
     */
    @Query(value = """
            WITH dias AS (
                SELECT generate_series(1, 7) AS dia
            ), franjas AS (
                SELECT generate_series(8, 18, 2) AS hora_inicio
            ), conteo AS (
                SELECT CAST(EXTRACT(ISODOW FROM fecha_solicitud) AS integer) AS dia,
                       CAST(8 + 2 * FLOOR((LEAST(GREATEST(EXTRACT(HOUR FROM fecha_solicitud), 8), 19) - 8) / 2) AS integer) AS hora_inicio,
                       COUNT(*) AS total
                FROM solicitudes_socio
                WHERE fecha_solicitud >= CURRENT_DATE - CAST(:dias AS integer)
                GROUP BY 1, 2
            )
            SELECT CAST(d.dia AS integer) AS "diaSemana",
                   CASE d.dia WHEN 1 THEN 'Lunes' WHEN 2 THEN 'Martes' WHEN 3 THEN 'Miércoles' WHEN 4 THEN 'Jueves'
                              WHEN 5 THEN 'Viernes' WHEN 6 THEN 'Sábado' ELSE 'Domingo' END AS "dia",
                   LPAD(CAST(f.hora_inicio AS text), 2, '0') || ':00-' || LPAD(CAST(f.hora_inicio + 2 AS text), 2, '0') || ':00' AS "franja",
                   CAST(COALESCE(c.total, 0) AS bigint) AS "cantidad",
                   CAST(COALESCE(ROUND(100.0 * COALESCE(c.total, 0) / NULLIF(SUM(COALESCE(c.total, 0)) OVER (), 0), 1), 0) AS double precision) AS "porcentaje"
            FROM dias d
            CROSS JOIN franjas f
            LEFT JOIN conteo c ON c.dia = d.dia AND c.hora_inicio = f.hora_inicio
            ORDER BY d.dia, f.hora_inicio
            """, nativeQuery = true)
    List<MapaCalorResponse> obtenerMapaCalor(@Param("dias") Integer dias);

    /**
     * Chart 4: por cada rango de edad, porcentaje de postulantes de cada clasificación.
     */
    @Query(value = """
            WITH rangos (orden, rango, desde, hasta) AS (
                VALUES (1, '18-29', 18, 29), (2, '30-39', 30, 39), (3, '40-49', 40, 49), (4, '50-59', 50, 59), (5, '60 a más', 60, 150)
            ), postulantes AS (
                SELECT s.clasificacion,
                       CAST(DATE_PART('year', AGE(CAST(s.fecha_solicitud AS date), s.fecha_nacimiento)) AS integer) AS edad
                FROM solicitudes_socio s
                WHERE s.clasificacion IS NOT NULL
                  AND s.fecha_solicitud >= CURRENT_DATE - CAST(:dias AS integer)
            )
            SELECT r.rango AS "rango",
                   c.codigo AS "clasificacion",
                   c.descripcion AS "descripcion",
                   CAST(COUNT(p.edad) AS bigint) AS "cantidad",
                   CAST(COALESCE(ROUND(100.0 * COUNT(p.edad) / NULLIF(SUM(COUNT(p.edad)) OVER (PARTITION BY r.rango), 0), 1), 0) AS double precision) AS "porcentaje"
            FROM rangos r
            CROSS JOIN clasificacion_pago c
            LEFT JOIN postulantes p ON p.clasificacion = c.codigo AND p.edad BETWEEN r.desde AND r.hasta
            GROUP BY r.orden, r.rango, c.orden, c.codigo, c.descripcion
            ORDER BY c.orden, r.orden
            """, nativeQuery = true)
    List<PerfilEdadResponse> obtenerPerfilEdad(@Param("dias") Integer dias);
}
