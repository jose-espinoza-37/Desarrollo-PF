package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.dashboard.KpiDashboardResponse;
import com.poseidon.nauticaapi.dto.dashboard.MapaCalorResponse;
import com.poseidon.nauticaapi.dto.dashboard.PerfilEdadResponse;
import com.poseidon.nauticaapi.dto.dashboard.SerieTemporalResponse;
import com.poseidon.nauticaapi.dto.dashboard.TiempoAtencionResponse;
import com.poseidon.nauticaapi.repository.SolicitudSocioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Panel gerencial: todo el cálculo estadístico se hace en las consultas SQL del repositorio. */
@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired
    private SolicitudSocioRepository solicitudRepository;

    public KpiDashboardResponse getKpis(Integer dias) {
        return solicitudRepository.obtenerKpis(dias);
    }

    public List<SerieTemporalResponse> getSerieTemporal(Integer dias) {
        return solicitudRepository.obtenerSerieTemporal(dias);
    }

    public List<TiempoAtencionResponse> getTiemposAtencion(Integer dias) {
        return solicitudRepository.obtenerTiemposAtencion(dias);
    }

    public List<MapaCalorResponse> getMapaCalor(Integer dias) {
        return solicitudRepository.obtenerMapaCalor(dias);
    }

    public List<PerfilEdadResponse> getPerfilEdad(Integer dias) {
        return solicitudRepository.obtenerPerfilEdad(dias);
    }
}
