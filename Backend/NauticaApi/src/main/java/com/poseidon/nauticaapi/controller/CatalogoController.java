package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.entity.ClasificacionPago;
import com.poseidon.nauticaapi.entity.EstadoEmbarcacion;
import com.poseidon.nauticaapi.entity.EstadoSolicitud;
import com.poseidon.nauticaapi.entity.TipoEmbarcacion;
import com.poseidon.nauticaapi.service.CatalogoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogos públicos que el frontend usa para mostrar etiquetas y opciones. */
@RestController
@RequestMapping("/api/catalogos")
public class CatalogoController {

    @Autowired
    private CatalogoService catalogoService;

    @GetMapping("/estados")
    public ResponseEntity<List<EstadoSolicitud>> estados() {
        return ResponseEntity.ok(catalogoService.listarEstados());
    }

    @GetMapping("/clasificaciones")
    public ResponseEntity<List<ClasificacionPago>> clasificaciones() {
        return ResponseEntity.ok(catalogoService.listarClasificaciones());
    }

    @GetMapping("/tipos-embarcacion")
    public ResponseEntity<List<TipoEmbarcacion>> tiposEmbarcacion() {
        return ResponseEntity.ok(catalogoService.listarTiposEmbarcacion());
    }

    @GetMapping("/estados-embarcacion")
    public ResponseEntity<List<EstadoEmbarcacion>> estadosEmbarcacion() {
        return ResponseEntity.ok(catalogoService.listarEstadosEmbarcacion());
    }
}
