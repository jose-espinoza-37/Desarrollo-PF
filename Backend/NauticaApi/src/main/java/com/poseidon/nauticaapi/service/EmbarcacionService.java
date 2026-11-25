package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.auditoria.AuditoriaService;
import com.poseidon.nauticaapi.dto.request.EmbarcacionRequest;
import com.poseidon.nauticaapi.dto.request.VerificacionEmbarcacionRequest;
import com.poseidon.nauticaapi.dto.response.DocumentoResponse;
import com.poseidon.nauticaapi.dto.response.EmbarcacionResponse;
import com.poseidon.nauticaapi.entity.AsignacionRada;
import com.poseidon.nauticaapi.entity.DocumentoEmbarcacion;
import com.poseidon.nauticaapi.entity.Embarcacion;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.exception.CredencialesInvalidasException;
import com.poseidon.nauticaapi.exception.RecursoNoEncontradoException;
import com.poseidon.nauticaapi.repository.AsignacionRadaRepository;
import com.poseidon.nauticaapi.repository.DocumentoEmbarcacionRepository;
import com.poseidon.nauticaapi.repository.EmbarcacionRepository;
import com.poseidon.nauticaapi.repository.TipoEmbarcacionRepository;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** HU11 (registrar embarcación y documentos) y HU12 (verificar en Capitanías). */
@Service
public class EmbarcacionService {

    private static final String ESQUEMA = "public";
    private static final String TABLA = "embarcaciones";
    private static final String MODULO_SOCIO = "Mis embarcaciones";
    private static final String MODULO_VERIFICAR = "Verificar embarcaciones";

    private static final Set<String> TIPOS_DOCUMENTO = Set.of("MATRICULA", "CERTIFICADO", "OTRO");
    private static final Set<String> FORMATOS = Set.of("application/pdf", "image/jpeg", "image/png");
    private static final long TAMANO_MAXIMO = 5L * 1024 * 1024;
    private static final int MAX_DOCUMENTOS = 6;

    @Autowired
    private EmbarcacionRepository embarcacionRepository;
    @Autowired
    private DocumentoEmbarcacionRepository documentoRepository;
    @Autowired
    private AsignacionRadaRepository asignacionRepository;
    @Autowired
    private TipoEmbarcacionRepository tipoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private AuditoriaService auditoriaService;

    // ----------------------------------------------------------------------
    //  HU11: el socio registra y consulta sus embarcaciones
    // ----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<EmbarcacionResponse> misEmbarcaciones(String correo) {
        return toResponses(embarcacionRepository.findBySocio_IdOrderByFechaRegistroDesc(usuario(correo).getId()));
    }

    @Transactional
    public EmbarcacionResponse registrar(EmbarcacionRequest request, String correo) {
        String matricula = request.getMatricula().trim().toUpperCase(Locale.ROOT);
        if (embarcacionRepository.existsByMatricula(matricula)) {
            throw new IllegalArgumentException("Ya existe una embarcación registrada con la matrícula " + matricula + ".");
        }
        validarTipo(request.getTipo());
        validarAnio(request.getAnioFabricacion());

        Embarcacion e = embarcacionRepository.save(Embarcacion.builder()
                .socio(usuario(correo))
                .nombre(request.getNombre().trim())
                .matricula(matricula)
                .tipo(request.getTipo())
                .eslora(request.getEslora())
                .manga(request.getManga())
                .calado(request.getCalado())
                .anioFabricacion(request.getAnioFabricacion())
                .estado(Embarcacion.PENDIENTE)
                .build());

        // Auditoría: INSERT de la ficha
        auditoriaService.registrarCreacion(ESQUEMA, TABLA, e.getId().toString(), MODULO_SOCIO);
        return toResponse(e);
    }

    /** El socio corrige una ficha pendiente u observada; vuelve a la bandeja de verificación. */
    @Transactional
    public EmbarcacionResponse subsanar(Long id, EmbarcacionRequest request, String correo) {
        Embarcacion e = buscarPropia(id, correo);
        if (Embarcacion.VERIFICADA.equals(e.getEstado())) {
            throw new IllegalStateException("La ficha ya fue verificada y no se puede modificar.");
        }
        String matricula = request.getMatricula().trim().toUpperCase(Locale.ROOT);
        if (embarcacionRepository.existsByMatriculaAndIdNot(matricula, id)) {
            throw new IllegalArgumentException("Ya existe otra embarcación con la matrícula " + matricula + ".");
        }
        validarTipo(request.getTipo());
        validarAnio(request.getAnioFabricacion());

        Embarcacion anterior = e.toBuilder().build();
        e.setNombre(request.getNombre().trim());
        e.setMatricula(matricula);
        e.setTipo(request.getTipo());
        e.setEslora(request.getEslora());
        e.setManga(request.getManga());
        e.setCalado(request.getCalado());
        e.setAnioFabricacion(request.getAnioFabricacion());
        e.setEstado(Embarcacion.PENDIENTE);

        auditoriaService.registrarActualizacion(ESQUEMA, TABLA, e.getId().toString(), MODULO_SOCIO, anterior, e);
        return toResponse(e);
    }

    /** Carga de documentos de la nave: PDF, JPG o PNG de hasta 5 MB. */
    @Transactional
    public DocumentoResponse subirDocumento(Long id, String tipoDocumento, MultipartFile archivo, String correo)
            throws IOException {
        Embarcacion e = buscarPropia(id, correo);
        if (tipoDocumento == null || !TIPOS_DOCUMENTO.contains(tipoDocumento)) {
            throw new IllegalArgumentException("Tipo de documento no válido (MATRICULA, CERTIFICADO u OTRO).");
        }
        if (archivo == null || archivo.isEmpty()) {
            throw new IllegalArgumentException("Selecciona un archivo.");
        }
        if (!FORMATOS.contains(archivo.getContentType())) {
            throw new IllegalArgumentException("Solo se aceptan archivos PDF, JPG o PNG.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new IllegalArgumentException("El archivo no puede superar los 5 MB.");
        }
        if (documentoRepository.countByEmbarcacion_Id(id) >= MAX_DOCUMENTOS) {
            throw new IllegalStateException("Cada embarcación admite como máximo " + MAX_DOCUMENTOS + " documentos.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "documento"
                : archivo.getOriginalFilename().replaceAll("[^\\p{L}0-9._ -]", "_");

        DocumentoEmbarcacion d = documentoRepository.save(DocumentoEmbarcacion.builder()
                .embarcacion(e)
                .tipoDocumento(tipoDocumento)
                .nombreArchivo(nombre.length() > 150 ? nombre.substring(0, 150) : nombre)
                .contentType(archivo.getContentType())
                .tamano(archivo.getSize())
                .contenido(archivo.getBytes())
                .build());

        auditoriaService.registrarCreacion(ESQUEMA, "documentos_embarcacion", d.getId().toString(), MODULO_SOCIO);
        return DocumentoResponse.builder()
                .id(d.getId())
                .tipoDocumento(d.getTipoDocumento())
                .nombreArchivo(d.getNombreArchivo())
                .contentType(d.getContentType())
                .tamano(d.getTamano())
                .fechaCarga(d.getFechaCarga())
                .embarcacionId(e.getId())
                .build();
    }

    /** El socio solo descarga documentos de sus naves; el personal descarga cualquiera. */
    @Transactional(readOnly = true)
    public DocumentoEmbarcacion descargarDocumento(Long documentoId, String correo, boolean esSocio) {
        DocumentoEmbarcacion d = documentoRepository.findById(documentoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el documento " + documentoId + "."));
        if (esSocio && !d.getEmbarcacion().getSocio().getCorreo().equals(correo)) {
            throw new AccessDeniedException("El documento no pertenece a tus embarcaciones.");
        }
        return d;
    }

    // ----------------------------------------------------------------------
    //  HU12: Servicios Navieros verifica la ficha en los registros de Capitanías
    // ----------------------------------------------------------------------
    @Transactional(readOnly = true)
    public List<EmbarcacionResponse> listar(String estado) {
        List<Embarcacion> lista = (estado == null || estado.isBlank())
                ? embarcacionRepository.findAllByOrderByFechaRegistroDesc()
                : embarcacionRepository.findByEstadoOrderByFechaRegistroAsc(estado.trim().toUpperCase(Locale.ROOT));
        return toResponses(lista);
    }

    @Transactional
    public EmbarcacionResponse verificar(Long id, VerificacionEmbarcacionRequest request, String correo) {
        Embarcacion e = buscar(id);
        if (!Embarcacion.PENDIENTE.equals(e.getEstado())) {
            throw new IllegalStateException("Solo se pueden verificar fichas en estado PENDIENTE.");
        }
        String observaciones = request.getObservaciones() == null ? "" : request.getObservaciones().trim();
        boolean observada = Embarcacion.OBSERVADA.equals(request.getResultado());
        if (observada && observaciones.length() < 10) {
            throw new IllegalArgumentException("Indica las observaciones (mínimo 10 caracteres) para que el socio las subsane.");
        }

        Embarcacion anterior = e.toBuilder().build();
        e.setEstado(request.getResultado());
        e.setObservaciones(observaciones.isEmpty() ? null : observaciones);
        e.setFechaVerificacion(LocalDateTime.now());
        e.setVerificadoPor(correo);

        // Auditoría: UPDATE con el resultado (el socio ve la observación en "Mis embarcaciones")
        auditoriaService.registrarActualizacion(ESQUEMA, TABLA, e.getId().toString(), MODULO_VERIFICAR, anterior, e);
        return toResponse(e);
    }

    // ----------------------------------------------------------------------
    //  Métodos internos
    // ----------------------------------------------------------------------
    EmbarcacionResponse toResponse(Embarcacion e) {
        return toResponses(List.of(e)).get(0);
    }

    /** Arma las fichas con 2 consultas en total (asignaciones y documentos), no 2 por nave. */
    List<EmbarcacionResponse> toResponses(List<Embarcacion> lista) {
        if (lista.isEmpty()) return List.of();
        List<Long> ids = lista.stream().map(Embarcacion::getId).toList();
        Map<Long, AsignacionRada> asignaciones = asignacionRepository.findByEmbarcacion_IdInAndActivaTrue(ids).stream()
                .collect(Collectors.toMap(a -> a.getEmbarcacion().getId(), a -> a, (a, b) -> a));
        Map<Long, List<DocumentoResponse>> documentos = documentoRepository.listarPorEmbarcaciones(ids).stream()
                .collect(Collectors.groupingBy(DocumentoResponse::getEmbarcacionId));
        return lista.stream()
                .map(e -> EmbarcacionResponse.fromEntity(e, asignaciones.get(e.getId()),
                        documentos.getOrDefault(e.getId(), List.of())))
                .toList();
    }

    private Embarcacion buscar(Long id) {
        return embarcacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la embarcación " + id + "."));
    }

    private Embarcacion buscarPropia(Long id, String correo) {
        Embarcacion e = buscar(id);
        if (!e.getSocio().getCorreo().equals(correo)) {
            throw new AccessDeniedException("La embarcación no pertenece a tu cuenta.");
        }
        return e;
    }

    private Usuario usuario(String correo) {
        return usuarioRepository.findByCorreo(correo).orElseThrow(CredencialesInvalidasException::new);
    }

    private void validarTipo(String tipo) {
        if (!tipoRepository.existsById(tipo)) {
            throw new IllegalArgumentException("Tipo de embarcación no válido.");
        }
    }

    private void validarAnio(Integer anio) {
        if (anio > LocalDateTime.now().getYear()) {
            throw new IllegalArgumentException("El año de fabricación no puede ser futuro.");
        }
    }
}
