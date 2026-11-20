package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.request.SolicitudRequest;
import com.poseidon.nauticaapi.dto.request.VerificacionRequest;
import com.poseidon.nauticaapi.dto.response.SeguimientoResponse;
import com.poseidon.nauticaapi.dto.response.SolicitudResponse;
import com.poseidon.nauticaapi.entity.Rol;
import com.poseidon.nauticaapi.entity.SolicitudSocio;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.RecursoNoEncontradoException;
import com.poseidon.nauticaapi.repository.RolRepository;
import com.poseidon.nauticaapi.repository.SolicitudSocioRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class SolicitudService {


    private static final List<String> ESTADOS_VIGENTES = List.of(
            SolicitudSocio.PENDIENTE, SolicitudSocio.VERIFICADA, SolicitudSocio.APROBADA);

    private final SolicitudSocioRepository solicitudRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public SolicitudService(
            SolicitudSocioRepository solicitudRepository,
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder) {

        this.solicitudRepository = solicitudRepository;
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public SolicitudSocio registrar(SolicitudRequest req) {

        String correo = req.correo().trim().toLowerCase(Locale.ROOT);
        String dni = req.dni().trim();


        if (Period.between(req.fechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("El postulante debe ser mayor de edad.");
        }


        if (req.poseeEmbarcacion() && req.cantidadEmbarcaciones() < 1) {
            throw new IllegalArgumentException("Indica cuántas embarcaciones posees (mínimo 1).");
        }


        if (solicitudRepository.existsByDniAndEstadoIn(dni, ESTADOS_VIGENTES)) {
            throw new IllegalArgumentException(
                    "Ya existe una solicitud en trámite o un socio registrado con el DNI " + dni + ".");
        }
        if (usuarioRepository.existsByCorreo(correo)
                || solicitudRepository.existsByCorreoAndEstadoIn(correo, ESTADOS_VIGENTES)) {
            throw new IllegalArgumentException("El correo " + correo + " ya está registrado en el club.");
        }

        SolicitudSocio s = new SolicitudSocio();
        s.setNombres(req.nombres().trim());
        s.setApellidos(req.apellidos().trim());
        s.setDni(dni);
        s.setFechaNacimiento(req.fechaNacimiento());
        s.setCorreo(correo);
        s.setCelular(req.celular().trim());
        s.setDireccion(req.direccion().trim());
        s.setPoseeEmbarcacion(req.poseeEmbarcacion());
        s.setCantidadEmbarcaciones(req.poseeEmbarcacion() ? req.cantidadEmbarcaciones() : 0);
        s.setPasswordHash(passwordEncoder.encode(req.password()));
        s.setEstado(SolicitudSocio.PENDIENTE);

        return solicitudRepository.save(s);
    }

    @Transactional(readOnly = true)
    public SeguimientoResponse seguimiento(UUID codigo) {
        SolicitudSocio s = solicitudRepository.findByCodigo(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una solicitud con el código ingresado."));

        return new SeguimientoResponse(
                s.getCodigo(), s.getNombres(), s.getEstado(),
                s.getFechaSolicitud(), s.getFechaResolucion(), s.getMotivoDesaprobacion());
    }


    @Transactional(readOnly = true)
    public List<SolicitudResponse> listar(String estado) {
        List<SolicitudSocio> lista = (estado == null || estado.isBlank())
                ? solicitudRepository.findAllByOrderByFechaSolicitudDesc()
                : solicitudRepository.findByEstadoOrderByFechaSolicitudAsc(estado.trim().toUpperCase(Locale.ROOT));

        return lista.stream()
                .map(s -> SolicitudResponse.desde(s, solicitudRepository.countByDni(s.getDni())))
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitudResponse obtener(Long id) {
        SolicitudSocio s = buscar(id);
        return SolicitudResponse.desde(s, solicitudRepository.countByDni(s.getDni()));
    }

    @Transactional
    public SolicitudResponse verificar(Long id, VerificacionRequest req, String verificadoPor) {
        SolicitudSocio s = buscar(id);

        if (!SolicitudSocio.PENDIENTE.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden verificar solicitudes en estado PENDIENTE.");
        }

        s.setClasificacion(req.clasificacion());
        s.setResultadoVerificacion(req.resultadoVerificacion().trim());
        s.setFechaVerificacion(LocalDateTime.now());
        s.setVerificadoPor(verificadoPor);


        s.setEstado(SolicitudSocio.PAGADOR.equals(req.clasificacion())
                ? SolicitudSocio.VERIFICADA
                : SolicitudSocio.NO_APTA);

        return SolicitudResponse.desde(s, solicitudRepository.countByDni(s.getDni()));
    }


    @Transactional
    public SolicitudResponse aprobar(Long id, String resueltoPor) {
        SolicitudSocio s = buscar(id);

        if (!SolicitudSocio.VERIFICADA.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden aprobar solicitudes verificadas como 'pagador'.");
        }
        if (usuarioRepository.existsByCorreo(s.getCorreo())) {
            throw new IllegalArgumentException("Ya existe un usuario con el correo " + s.getCorreo() + ".");
        }


        Usuario usuario = new Usuario();
        usuario.setNombres(s.getNombres());
        usuario.setApellidos(s.getApellidos());
        usuario.setCorreo(s.getCorreo());
        usuario.setPassword(s.getPasswordHash());
        usuario.setRol(rolRepository.findByCodigo(Rol.SOCIO)
                .orElseThrow(() -> new IllegalStateException("No existe el rol SOCIO.")));
        usuarioRepository.save(usuario);

        s.setUsuarioId(usuario.getId());
        s.setEstado(SolicitudSocio.APROBADA);
        s.setFechaResolucion(LocalDateTime.now());
        s.setResueltoPor(resueltoPor);

        return SolicitudResponse.desde(s, solicitudRepository.countByDni(s.getDni()));
    }

    @Transactional
    public SolicitudResponse desaprobar(Long id, String motivo, String resueltoPor) {
        SolicitudSocio s = buscar(id);

        if (!SolicitudSocio.VERIFICADA.equals(s.getEstado())) {
            throw new IllegalStateException("Solo se pueden desaprobar solicitudes verificadas.");
        }

        s.setEstado(SolicitudSocio.DESAPROBADA);
        s.setMotivoDesaprobacion(motivo.trim());
        s.setFechaResolucion(LocalDateTime.now());
        s.setResueltoPor(resueltoPor);

        return SolicitudResponse.desde(s, solicitudRepository.countByDni(s.getDni()));
    }


    private SolicitudSocio buscar(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la solicitud " + id + "."));
    }
}
