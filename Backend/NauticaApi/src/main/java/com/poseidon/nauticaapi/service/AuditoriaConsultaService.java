package com.poseidon.nauticaapi.service;

import com.poseidon.nauticaapi.dto.response.EventoResponse;
import com.poseidon.nauticaapi.entity.Usuario;
import com.poseidon.nauticaapi.entity.auditoria.Cambio;
import com.poseidon.nauticaapi.entity.auditoria.Evento;
import com.poseidon.nauticaapi.repository.UsuarioRepository;
import com.poseidon.nauticaapi.repository.auditoria.CambioRepository;
import com.poseidon.nauticaapi.repository.auditoria.EventoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Consulta de la bitácora de auditoría para el módulo Auditoría del Gerente. */
@Service
@Transactional(readOnly = true)
public class AuditoriaConsultaService {

    @Autowired
    private EventoRepository eventoRepository;
    @Autowired
    private CambioRepository cambioRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;

    /** Últimos 300 eventos, con el correo del responsable y los campos modificados. */
    public List<EventoResponse> listarEventos() {
        Map<Long, String> correos = usuarioRepository.findAll().stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getCorreo, (a, b) -> a));
        List<Evento> eventos = eventoRepository.findTop300ByOrderByIdEventoDesc();
        // Todos los cambios en una sola consulta, agrupados por evento
        Map<Long, List<Cambio>> cambios = cambioRepository
                .findByEvento_IdEventoInOrderByIdCambio(eventos.stream().map(Evento::getIdEvento).toList())
                .stream()
                .collect(Collectors.groupingBy(c -> c.getEvento().getIdEvento()));
        return eventos.stream()
                .map(e -> EventoResponse.fromEntity(
                        e,
                        correos.getOrDefault(e.getIdUsuario(), "anónimo"),
                        cambios.getOrDefault(e.getIdEvento(), List.of())))
                .toList();
    }
}
