package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.request.EmbarcacionRequest;
import com.poseidon.nauticaapi.dto.request.VerificacionEmbarcacionRequest;
import com.poseidon.nauticaapi.dto.response.DocumentoResponse;
import com.poseidon.nauticaapi.dto.response.EmbarcacionResponse;
import com.poseidon.nauticaapi.entity.DocumentoEmbarcacion;
import com.poseidon.nauticaapi.service.EmbarcacionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api/embarcaciones")
public class EmbarcacionController {

    @Autowired
    private EmbarcacionService embarcacionService;

    // ---------- HU11: socio ----------
    @GetMapping("/mias")
    public ResponseEntity<List<EmbarcacionResponse>> misEmbarcaciones(Authentication autenticacion) {
        return ResponseEntity.ok(embarcacionService.misEmbarcaciones(autenticacion.getName()));
    }

    @PostMapping
    public ResponseEntity<EmbarcacionResponse> registrar(@Valid @RequestBody EmbarcacionRequest request,
                                                         Authentication autenticacion) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(embarcacionService.registrar(request, autenticacion.getName()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmbarcacionResponse> subsanar(@PathVariable("id") Long id,
                                                        @Valid @RequestBody EmbarcacionRequest request,
                                                        Authentication autenticacion) {
        return ResponseEntity.ok(embarcacionService.subsanar(id, request, autenticacion.getName()));
    }

    @PostMapping(value = "/{id}/documentos", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentoResponse> subirDocumento(@PathVariable("id") Long id,
                                                            @RequestParam("tipoDocumento") String tipoDocumento,
                                                            @RequestParam("archivo") MultipartFile archivo,
                                                            Authentication autenticacion) throws IOException {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(embarcacionService.subirDocumento(id, tipoDocumento, archivo, autenticacion.getName()));
    }

    /** Descarga del documento (socio dueño o personal de Servicios Navieros / Gerencia). */
    @GetMapping("/documentos/{documentoId}")
    public ResponseEntity<byte[]> descargarDocumento(@PathVariable("documentoId") Long documentoId,
                                                     Authentication autenticacion) {
        boolean esSocio = autenticacion.getAuthorities().stream()
                .anyMatch(a -> "ROLE_SOCIO".equals(a.getAuthority()));
        DocumentoEmbarcacion d = embarcacionService.descargarDocumento(documentoId, autenticacion.getName(), esSocio);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(d.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline()
                        .filename(d.getNombreArchivo(), StandardCharsets.UTF_8).build().toString())
                .body(d.getContenido());
    }

    // ---------- HU12: Servicios Navieros ----------
    @GetMapping
    public ResponseEntity<List<EmbarcacionResponse>> listar(
            @RequestParam(name = "estado", required = false) String estado) {
        return ResponseEntity.ok(embarcacionService.listar(estado));
    }

    @PutMapping("/{id}/verificar")
    public ResponseEntity<EmbarcacionResponse> verificar(@PathVariable("id") Long id,
                                                         @Valid @RequestBody VerificacionEmbarcacionRequest request,
                                                         Authentication autenticacion) {
        return ResponseEntity.ok(embarcacionService.verificar(id, request, autenticacion.getName()));
    }
}
