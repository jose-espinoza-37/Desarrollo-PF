package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.auditoria.AuditoriaService;
import com.poseidon.nauticaapi.dto.response.AsignacionRadaResponse;
import com.poseidon.nauticaapi.dto.response.EmbarcacionResponse;
import com.poseidon.nauticaapi.dto.response.RadaCompatibleResponse;
import com.poseidon.nauticaapi.dto.response.RadaResponse;
import com.poseidon.nauticaapi.entity.AsignacionRada;
import com.poseidon.nauticaapi.entity.Embarcacion;
import com.poseidon.nauticaapi.entity.Rada;
import com.poseidon.nauticaapi.exception.RecursoNoEncontradoException;
import com.poseidon.nauticaapi.repository.AsignacionRadaRepository;
import com.poseidon.nauticaapi.repository.EmbarcacionRepository;
import com.poseidon.nauticaapi.repository.RadaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** HU13: asignar y liberar radas según las medidas de la embarcación. */
@Service
public class RadaService {

    private static final String ESQUEMA = "public";
    private static final String MODULO = "Asignar radas";

    @Autowired
    private RadaRepository radaRepository;
    @Autowired
    private EmbarcacionRepository embarcacionRepository;
    @Autowired
    private AsignacionRadaRepository asignacionRepository;
    @Autowired
    private EmbarcacionService embarcacionService;
    @Autowired
    private AuditoriaService auditoriaService;

    @Transactional(readOnly = true)
    public List<RadaResponse> listar() {
        // Naves que ocupan radas: una sola consulta
        Map<Long, Embarcacion> ocupantes = embarcacionRepository.findByRadaIsNotNull().stream()
                .collect(Collectors.toMap(e -> e.getRada().getId(), e -> e, (a, b) -> a));
        return radaRepository.findAllByOrderByCodigo().stream()
                .map(r -> RadaResponse.fromEntity(r, ocupantes.get(r.getId())))
                .toList();
    }

    /** Embarcaciones verificadas sin rada (solo estas se pueden asignar). */
    @Transactional(readOnly = true)
    public List<EmbarcacionResponse> porAsignar() {
        return embarcacionService.toResponses(
                embarcacionRepository.findByEstadoAndRadaIsNullOrderByFechaVerificacionAsc(Embarcacion.VERIFICADA));
    }

    @Transactional(readOnly = true)
    public List<RadaCompatibleResponse> compatibles(Long embarcacionId) {
        Embarcacion e = buscarEmbarcacion(embarcacionId);
        if (!Embarcacion.VERIFICADA.equals(e.getEstado())) {
            throw new IllegalStateException("Solo las embarcaciones verificadas pueden asignarse a una rada.");
        }
        return radaRepository.buscarCompatibles(embarcacionId);
    }

    @Transactional
    public RadaResponse asignar(Long radaId, Long embarcacionId, String correo) {
        Rada rada = buscarRada(radaId);
        Embarcacion e = buscarEmbarcacion(embarcacionId);

        if (!Embarcacion.VERIFICADA.equals(e.getEstado())) {
            throw new IllegalStateException("Solo las embarcaciones verificadas pueden asignarse a una rada.");
        }
        if (e.getRada() != null) {
            throw new IllegalStateException("La embarcación ya tiene asignada la rada " + e.getRada().getCodigo() + ".");
        }
        if (!Rada.LIBRE.equals(rada.getEstado())) {
            throw new IllegalStateException("La rada " + rada.getCodigo() + " está ocupada.");
        }
        if (rada.getEsloraMax().compareTo(e.getEslora()) < 0
                || rada.getMangaMax().compareTo(e.getManga()) < 0
                || rada.getCaladoMax().compareTo(e.getCalado()) < 0) {
            throw new IllegalArgumentException("La embarcación no cabe en la rada " + rada.getCodigo() + ".");
        }

        Rada radaAnterior = rada.toBuilder().build();
        Embarcacion embarcacionAnterior = e.toBuilder().build();
        rada.setEstado(Rada.OCUPADA);
        e.setRada(rada);
        AsignacionRada asignacion = asignacionRepository.save(AsignacionRada.builder()
                .rada(rada)
                .embarcacion(e)
                .asignadoPor(correo)
                .build());

        // Auditoría: INSERT en el historial y UPDATE de la rada y de la embarcación
        auditoriaService.registrarCreacion(ESQUEMA, "asignaciones_rada", asignacion.getId().toString(), MODULO);
        auditoriaService.registrarActualizacion(ESQUEMA, "radas", rada.getId().toString(), MODULO, radaAnterior, rada);
        auditoriaService.registrarActualizacion(ESQUEMA, "embarcaciones", e.getId().toString(), MODULO,
                embarcacionAnterior, e);
        return RadaResponse.fromEntity(rada, e);
    }

    @Transactional
    public RadaResponse liberar(Long radaId, String motivo, String correo) {
        Rada rada = buscarRada(radaId);
        AsignacionRada asignacion = asignacionRepository.findByRada_IdAndActivaTrue(radaId)
                .orElseThrow(() -> new IllegalStateException("La rada " + rada.getCodigo() + " no está ocupada."));
        Embarcacion e = asignacion.getEmbarcacion();

        Rada radaAnterior = rada.toBuilder().build();
        asignacion.setActiva(false);
        asignacion.setFechaLiberacion(LocalDateTime.now());
        asignacion.setLiberadoPor(correo);
        asignacion.setMotivoLiberacion(motivo.trim());
        e.setRada(null);
        rada.setEstado(Rada.LIBRE);

        auditoriaService.registrarActualizacion(ESQUEMA, "radas", rada.getId().toString(), MODULO, radaAnterior, rada);
        auditoriaService.registrarEvento("asignaciones_rada", asignacion.getId().toString(), MODULO, "UPDATE", true,
                "Liberación: " + asignacion.getMotivoLiberacion());
        return RadaResponse.fromEntity(rada, null);
    }

    @Transactional(readOnly = true)
    public List<AsignacionRadaResponse> historial() {
        return asignacionRepository.findTop200ByOrderByIdDesc().stream()
                .map(AsignacionRadaResponse::fromEntity).toList();
    }

    private Rada buscarRada(Long id) {
        return radaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la rada " + id + "."));
    }

    private Embarcacion buscarEmbarcacion(Long id) {
        return embarcacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la embarcación " + id + "."));
    }
}
