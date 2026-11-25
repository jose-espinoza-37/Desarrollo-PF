package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.auditoria.AuditoriaService;
import com.poseidon.nauticaapi.dto.request.SolicitudRequest;
import com.poseidon.nauticaapi.dto.request.VerificacionRequest;
import com.poseidon.nauticaapi.dto.response.ConteoDniResponse;
import com.poseidon.nauticaapi.dto.response.SeguimientoResponse;
import com.poseidon.nauticaapi.dto.response.SolicitudCreadaResponse;
import com.poseidon.nauticaapi.dto.response.SolicitudResponse;
import com.poseidon.nauticaapi.entity.ClasificacionPago;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.SolicitudSocio;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.RecursoNoEncontradoException;
import com.poseidon.nauticaapi.repository.ClasificacionPagoRepository;
import com.poseidon.nauticaapi.repository.RolRepository;
import com.poseidon.nauticaapi.repository.SolicitudSocioRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/** Flujo de inscripción: HU08 (registrar), HU09 (verificar) y HU10 (aprobar / desaprobar). */
@Service
public class SolicitudService {

    private static final String ESQUEMA = "public";
    private static final String TABLA = "solicitudes_socio";
    private static final String MODULO_REGISTRO = "Hazte socio";
    private static final String MODULO_VERIFICAR = "Verificar solicitudes";
    private static final String MODULO_APROBAR = "Aprobar solicitudes";

    /** Estados que impiden volver a postular con el mismo DNI o correo. */
    private static final List<String> ESTADOS_VIGENTES = List.of(
            SolicitudSocio.PENDIENTE, SolicitudSocio.VERIFICADA, SolicitudSocio.APROBADA);

    @Autowired
    private SolicitudSocioRepository solicitudRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RolRepository rolRepository;
    @Autowired
    private ClasificacionPagoRepository clasificacionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private AuditoriaService auditoriaService;

    // ----------------------------------------------------------------------
    //  HU08: registrar la solicitud de inscripción
    // ----------------------------------------------------------------------
    @Transactional
    public SolicitudCreadaResponse registrar(SolicitudRequest request) {
        SolicitudSocio s = SolicitudRequest.toEntity(request);

        if (Period.between(s.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El postulante debe ser mayor de edad.");
        }
        if (s.getPoseeEmbarcacion() && s.getCantidadEmbarcaciones() < 1) {
            throw new IllegalArgumentException("Indica cuántas embarcaciones posees (mínimo 1).");
        }
        // Evita la doble inscripción
        if (solicitudRepository.existsByDniAndEstadoIn(s.getDni(), ESTADOS_VIGENTES)) {
            throw new IllegalArgumentException(
                    "Ya existe una solicitud en trámite o un socio registrado con el DNI " + s.getDni() + ".");
        }
        if (usuarioRepository.existsByCorreo(s.getCorreo())
                || solicitudRepository.existsByCorreoAndEstadoIn(s.getCorreo(), ESTADOS_VIGENTES)) {
            throw new IllegalArgumentException("El correo " + s.getCorreo() + " ya está registrado en el club.");
        }

        s.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        s = solicitudRepository.save(s);

        // Auditoría: INSERT de la solicitud
        auditoriaService.registrarCreacion(ESQUEMA, TABLA, s.getId().toString(), MODULO_REGISTRO);
        return SolicitudCreadaResponse.fromEntity(s);
    }

    @Transactional(readOnly = true)
    public SeguimientoResponse seguimiento(UUID codigo) {
        return solicitudRepository.findByCodigo(codigo)
                .map(SeguimientoResponse::fromEntity)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe una solicitud con el código ingresado."));
    }

    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(String estado) {
        List<SolicitudSocio> lista = (estado == null || estado.isBlank())
                ? solicitudRepository.findAllByOrderByFechaSolicitudDesc()
                : solicitudRepository.findByEstadoOrderByFechaSolicitudAsc(estado.trim().toUpperCase(Locale.ROOT));
        // Un solo conteo por DNI para toda la bandeja
        Map<String, Long> conteo = solicitudRepository.contarPorDni().stream()
                .collect(Collectors.toMap(ConteoDniResponse::getDni, ConteoDniResponse::getCantidad));
        return lista.stream()
                .map(s -> SolicitudResponse.fromEntity(s, conteo.getOrDefault(s.getDni(), 1L)))
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        return toResponse(buscar(id));
    }

    // ----------------------------------------------------------------------
    //  HU09: verificar con otros clubes y clasificar al postulante
    // ----------------------------------------------------------------------
    @Transactional
    public SolicitudResponse verificar(Long id, VerificacionRequest request, String verificadoPor) {
        SolicitudSocio s = buscar(id);
        if (!SolicitudSocio.PENDIENTE.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden verificar solicitudes en estado PENDIENTE.");
        }
        ClasificacionPago clasificacion = clasificacionRepository.findById(request.getClasificacion())
                .orElseThrow(() -> new RecursoNoEncontradoException("Clasificación no válida."));

        SolicitudSocio anterior = s.toBuilder().build(); // foto antes del cambio (auditoría)

        s.setClasificacion(clasificacion.getCodigo());
        s.setResultadoVerificacion(request.getResultadoVerificacion().trim());
        s.setFechaVerificacion(LocalDateTime.now());
        s.setVerificadoPor(verificadoPor);
        // Política del club (catálogo): solo la clasificación "apta" pasa al Jefe
        s.setEstado(clasificacion.getApto() ? SolicitudSocio.VERIFICADA : SolicitudSocio.NO_APTA);

        // Auditoría: UPDATE con los campos que cambiaron
        auditoriaService.registrarActualizacion(ESQUEMA, TABLA, s.getId().toString(), MODULO_VERIFICAR, anterior, s);
        return toResponse(s);
    }

    // ----------------------------------------------------------------------
    //  HU10: aprobar (otorga el rango de Socio) o desaprobar con motivo
    // ----------------------------------------------------------------------
    @Transactional
    public SolicitudResponse aprobar(Long id, String resueltoPor) {
        SolicitudSocio s = buscar(id);
        if (!SolicitudSocio.VERIFICADA.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes verificadas como 'pagador'.");
        }
        if (usuarioRepository.existsByCorreo(s.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo " + s.getCorreo() + ".");
        }

        Usuario usuario = usuarioRepository.save(Usuario.builder()
                .nombres(s.getNombres())
                .apellidos(s.getApellidos())
                .correo(s.getCorreo())
                .password(s.getPasswordHash())
                .rol(rolRepository.findByCodigo(Rol.SOCIO)
                        .orElseThrow(() -> new IllegalStateException("No existe el rol SOCIO.")))
                .build());

        SolicitudSocio anterior = s.toBuilder().build();
        s.setUsuarioId(usuario.getId());
        s.setEstado(SolicitudSocio.APROBADA);
        s.setFechaResolucion(LocalDateTime.now());
        s.setResueltoPor(resueltoPor);

        // Auditoría: INSERT del nuevo socio y UPDATE de la solicitud
        auditoriaService.registrarCreacion(ESQUEMA, "usuarios", usuario.getId().toString(), MODULO_APROBAR);
        auditoriaService.registrarActualizacion(ESQUEMA, TABLA, s.getId().toString(), MODULO_APROBAR, anterior, s);
        return toResponse(s);
    }

    @Transactional
    public SolicitudResponse desaprobar(Long id, String motivo, String resueltoPor) {
        SolicitudSocio s = buscar(id);
        if (!SolicitudSocio.VERIFICADA.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden desaprobar solicitudes verificadas.");
        }

        SolicitudSocio anterior = s.toBuilder().build();
        s.setEstado(SolicitudSocio.DESAPROBADA);
        s.setMotivoDesaprobacion(motivo.trim());
        s.setFechaResolucion(LocalDateTime.now());
        s.setResueltoPor(resueltoPor);

        // Auditoría: UPDATE de la solicitud con el motivo
        auditoriaService.registrarActualizacion(ESQUEMA, TABLA, s.getId().toString(), MODULO_APROBAR, anterior, s);
        return toResponse(s);
    }

    private SolicitudSocio buscar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud " + id + "."));
    }

    private SolicitudResponse toResponse(SolicitudSocio s) {
        return SolicitudResponse.fromEntity(s, solicitudRepository.countByDni(s.getDni()));
    }
}
