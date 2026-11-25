package com.poseidon.nauticaapi.auditoria;

import com.poseidon.nauticaapi.entity.auditoria.Cambio;
import com.poseidon.nauticaapi.entity.auditoria.Evento;
import com.poseidon.nauticaapi.repository.auditoria.CambioRepository;
import com.poseidon.nauticaapi.repository.auditoria.EventoRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Servicio genérico de auditoría: guarda un Evento (y sus Cambio) por cada
 * operación INSERT, UPDATE o DELETE, y también los inicios de sesión.
 *
 * Los datos HTTP los toma de AuditoriaContext, que llena AuditoriaInterceptor.
 * REQUIRES_NEW: la auditoría se guarda en su propia transacción.
 */
@Service
public class AuditoriaService {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaService.class);

    /** Campos que nunca se copian a la auditoría. */
    private static final Set<String> CAMPOS_SENSIBLES = Set.of("password", "passwordHash");

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private CambioRepository cambioRepository;

    @Autowired
    private HttpServletResponse httpResponse;

    // ======================================================================
    //  API pública
    // ======================================================================

    /** Registra una operación INSERT. Solo guarda el Evento. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarCreacion(String esquema, String tabla, String idRegistro, String modulo) {
        guardar(construirEvento(esquema, tabla, idRegistro, modulo, "INSERT", "SUCCESS", null), List.of());
    }

    /** Registra una operación UPDATE comparando los campos con reflexión. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarActualizacion(String esquema, String tabla, String idRegistro, String modulo,
                                       Object estadoAnterior, Object estadoNuevo) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "UPDATE", "SUCCESS", null);
        guardar(evento, compararCampos(evento, estadoAnterior, estadoNuevo));
    }

    /** Registra una operación DELETE guardando una foto del registro. */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEliminacion(String esquema, String tabla, String idRegistro, String modulo,
                                     Object estadoAnterior) {
        Evento evento = construirEvento(esquema, tabla, idRegistro, modulo, "DELETE", "SUCCESS", null);
        guardar(evento, compararCampos(evento, estadoAnterior, null));
    }

    /** Registra un evento sin cambios de datos (ej. LOGIN correcto o fallido). */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registrarEvento(String tabla, String idRegistro, String modulo, String operacion,
                                boolean exitoso, String motivo) {
        guardar(construirEvento("public", tabla, idRegistro, modulo, operacion,
                exitoso ? "SUCCESS" : "FAILURE", motivo), List.of());
    }

    // ======================================================================
    //  Métodos internos
    // ======================================================================

    private void guardar(Evento evento, List<Cambio> cambios) {
        eventoRepository.save(evento);
        if (!cambios.isEmpty()) cambioRepository.saveAll(cambios);
        log.debug("[Auditoria] {} {}.{} id={} cambios={}", evento.getOperacion(),
                evento.getNombreEsquema(), evento.getNombreTabla(), evento.getIdRegistro(), cambios.size());
    }

    private Evento construirEvento(String esquema, String tabla, String idRegistro, String modulo,
                                   String operacion, String resultado, String motivo) {
        return Evento.builder()
                .nombreEsquema(esquema)
                .nombreTabla(tabla)
                .idRegistro(idRegistro)
                .modulo(modulo)
                .operacion(operacion)
                .fechaEvento(OffsetDateTime.now())
                .resultado(resultado)
                .motivo(motivo)
                .idUsuario(Objects.requireNonNullElse(AuditoriaContext.getIdUsuario(), 0L))
                .ipOrigen(AuditoriaContext.getIpOrigen())
                .metodoHttp(AuditoriaContext.getMetodoHttp())
                .endpoint(AuditoriaContext.getEndpoint())
                .codigoHttp(httpResponse != null ? httpResponse.getStatus() : null)
                .idInteraccion(AuditoriaContext.getIdInteraccion())
                .versionAplicacion(AuditoriaContext.getVersionAplicacion())
                .build();
    }

    /**
     * Compara dos objetos campo a campo. Si "nuevo" es null (DELETE),
     * guarda todos los valores anteriores con valorNuevo = null.
     */
    private List<Cambio> compararCampos(Evento evento, Object anterior, Object nuevo) {
        List<Cambio> cambios = new ArrayList<>();
        if (anterior == null) return cambios;
        for (Field field : anterior.getClass().getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) || CAMPOS_SENSIBLES.contains(field.getName())) continue;
            field.setAccessible(true);
            try {
                Object va = field.get(anterior);
                Object vn = nuevo != null ? field.get(nuevo) : null;
                if (nuevo == null || !Objects.equals(va, vn)) {
                    cambios.add(Cambio.builder()
                            .evento(evento)
                            .nombreCampo(field.getName())
                            .valorAnterior(va == null ? null : va.toString())
                            .valorNuevo(vn == null ? null : vn.toString())
                            .build());
                }
            } catch (IllegalAccessException e) {
                log.warn("[Auditoria] No se pudo leer el campo '{}': {}", field.getName(), e.getMessage());
            }
        }
        return cambios;
    }
}
