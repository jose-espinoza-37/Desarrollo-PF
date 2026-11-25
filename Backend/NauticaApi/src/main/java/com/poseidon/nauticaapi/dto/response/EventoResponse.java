package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.auditoria.Cambio;
import com.poseidon.nauticaapi.entity.auditoria.Evento;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

/** Evento de auditoría con el detalle de los campos que cambiaron. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventoResponse {
    private Long idEvento;
    private OffsetDateTime fechaEvento;
    private String modulo;
    private String operacion;
    private String nombreTabla;
    private String idRegistro;
    private Long idUsuario;
    private String usuario;
    private String ipOrigen;
    private String metodoHttp;
    private String endpoint;
    private String resultado;
    private String motivo;
    private List<CambioResponse> cambios;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CambioResponse {
        private String campo;
        private String valorAnterior;
        private String valorNuevo;

        public static CambioResponse fromEntity(Cambio c) {
            return CambioResponse.builder()
                    .campo(c.getNombreCampo())
                    .valorAnterior(c.getValorAnterior())
                    .valorNuevo(c.getValorNuevo())
                    .build();
        }
    }

    public static EventoResponse fromEntity(Evento e, String usuario, List<Cambio> cambios) {
        return EventoResponse.builder()
                .idEvento(e.getIdEvento())
                .fechaEvento(e.getFechaEvento())
                .modulo(e.getModulo())
                .operacion(e.getOperacion())
                .nombreTabla(e.getNombreTabla())
                .idRegistro(e.getIdRegistro())
                .idUsuario(e.getIdUsuario())
                .usuario(usuario)
                .ipOrigen(e.getIpOrigen())
                .metodoHttp(e.getMetodoHttp())
                .endpoint(e.getEndpoint())
                .resultado(e.getResultado())
                .motivo(e.getMotivo())
                .cambios(cambios.stream().map(CambioResponse::fromEntity).toList())
                .build();
    }
}
