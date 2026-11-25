package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** Datos del usuario autenticado (GET /api/usuarios/me). */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerfilResponse {
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private String rolNombre;
    private LocalDateTime fechaCreacion;

    public static PerfilResponse fromEntity(Usuario usuario) {
        return PerfilResponse.builder()
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getCodigo())
                .rolNombre(usuario.getRol().getNombre())
                .fechaCreacion(usuario.getFechaCreacion())
                .build();
    }
}
