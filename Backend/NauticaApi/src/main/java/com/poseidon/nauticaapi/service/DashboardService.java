package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.response.DashboardResponse;
import com.poseidon.nauticaapi.dto.response.DashboardResponse.Conteo;
import com.poseidon.nauticaapi.dto.response.DashboardResponse.SerieDia;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.SolicitudSocio;
import com.poseidon.nauticaapi.repository.SolicitudSocioRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class DashboardService {

    private static final int DIAS = 14;


    private static final Map<String, String> ETIQUETAS_ESTADO = new LinkedHashMap<>();
    private static final Map<String, String> ETIQUETAS_CLASIFICACION = new LinkedHashMap<>();

    static {
        ETIQUETAS_ESTADO.put(SolicitudSocio.PENDIENTE, "Pendiente de verificación");
        ETIQUETAS_ESTADO.put(SolicitudSocio.VERIFICADA, "Pendiente de aprobación");
        ETIQUETAS_ESTADO.put(SolicitudSocio.NO_APTA, "No apta (no pagador)");
        ETIQUETAS_ESTADO.put(SolicitudSocio.APROBADA, "Aprobada");
        ETIQUETAS_ESTADO.put(SolicitudSocio.DESAPROBADA, "Desaprobada");

        ETIQUETAS_CLASIFICACION.put(SolicitudSocio.PAGADOR, "Pagador");
        ETIQUETAS_CLASIFICACION.put(SolicitudSocio.PAGADOR_ESPORADICO, "Pagador esporádico");
        ETIQUETAS_CLASIFICACION.put(SolicitudSocio.RENUENTE, "Renuente a pago");
    }

    private final SolicitudSocioRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;

    public DashboardService(SolicitudSocioRepository solicitudRepository, UsuarioRepository usuarioRepository) {
        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public DashboardResponse resumen() {

        long total = solicitudRepository.count();


        Map<String, Long> estados = aMapa(solicitudRepository.contarPorEstado());
        List<Conteo> porEstado = new ArrayList<>();
        ETIQUETAS_ESTADO.forEach((codigo, etiqueta) -> {
            long n = estados.getOrDefault(codigo, 0L);
            porEstado.add(new Conteo(codigo, etiqueta, n, porcentaje(n, total)));
        });


        Map<String, Long> clasif = aMapa(solicitudRepository.contarPorClasificacion());
        long totalClasificados = clasif.values().stream().mapToLong(Long::longValue).sum();
        List<Conteo> porClasificacion = new ArrayList<>();
        ETIQUETAS_CLASIFICACION.forEach((codigo, etiqueta) -> {
            long n = clasif.getOrDefault(codigo, 0L);
            porClasificacion.add(new Conteo(codigo, etiqueta, n, porcentaje(n, totalClasificados)));
        });


        LocalDate hoy = LocalDate.now();
        LocalDate desde = hoy.minusDays(DIAS - 1);
        Map<LocalDate, Long> porDia = new TreeMap<>();
        for (int i = 0; i < DIAS; i++) {
            porDia.put(desde.plusDays(i), 0L);
        }
        solicitudRepository.findByFechaSolicitudGreaterThanEqual(desde.atStartOfDay())
                .forEach(s -> porDia.merge(s.getFechaSolicitud().toLocalDate(), 1L, Long::sum));
        List<SerieDia> solicitudesPorDia = porDia.entrySet().stream()
                .map(e -> new SerieDia(e.getKey().toString(), e.getValue()))
                .toList();


        long aprobadas = estados.getOrDefault(SolicitudSocio.APROBADA, 0L);
        long desaprobadas = estados.getOrDefault(SolicitudSocio.DESAPROBADA, 0L);

        return new DashboardResponse(
                total,
                estados.getOrDefault(SolicitudSocio.PENDIENTE, 0L),
                estados.getOrDefault(SolicitudSocio.VERIFICADA, 0L),
                usuarioRepository.countByRol_CodigoAndActivoTrue(Rol.SOCIO),
                porcentaje(aprobadas, aprobadas + desaprobadas),
                solicitudesPorDia,
                porClasificacion,
                porEstado);
    }

    private static Map<String, Long> aMapa(List<Object[]> filas) {
        Map<String, Long> mapa = new LinkedHashMap<>();
        for (Object[] fila : filas) {
            mapa.put((String) fila[0], ((Number) fila[1]).longValue());
        }
        return mapa;
    }

    private static double porcentaje(long parte, long total) {
        if (total == 0) return 0.0;
        return Math.round(parte * 1000.0 / total) / 10.0; // un decimal
    }
}
