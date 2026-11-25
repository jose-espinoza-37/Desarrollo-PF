package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.entity.ClasificacionPago;
import com.poseidon.nauticaapi.entity.EstadoEmbarcacion;
import com.poseidon.nauticaapi.entity.EstadoSolicitud;
import com.poseidon.nauticaapi.entity.TipoEmbarcacion;
import com.poseidon.nauticaapi.repository.ClasificacionPagoRepository;
import com.poseidon.nauticaapi.repository.EstadoEmbarcacionRepository;
import com.poseidon.nauticaapi.repository.EstadoSolicitudRepository;
import com.poseidon.nauticaapi.repository.TipoEmbarcacionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/** Catálogos: estados de la solicitud, clasificación de pago, tipos y estados de embarcación. */
@Service
public class CatalogoService {

    @Autowired
    private EstadoSolicitudRepository estadoRepository;
    @Autowired
    private ClasificacionPagoRepository clasificacionRepository;
    @Autowired
    private TipoEmbarcacionRepository tipoEmbarcacionRepository;
    @Autowired
    private EstadoEmbarcacionRepository estadoEmbarcacionRepository;

    public List<EstadoSolicitud> listarEstados() {
        return estadoRepository.findAllByOrderByOrden();
    }

    public List<ClasificacionPago> listarClasificaciones() {
        return clasificacionRepository.findAllByOrderByOrden();
    }

    public List<TipoEmbarcacion> listarTiposEmbarcacion() {
        return tipoEmbarcacionRepository.findAllByOrderByOrden();
    }

    public List<EstadoEmbarcacion> listarEstadosEmbarcacion() {
        return estadoEmbarcacionRepository.findAllByOrderByOrden();
    }
}
