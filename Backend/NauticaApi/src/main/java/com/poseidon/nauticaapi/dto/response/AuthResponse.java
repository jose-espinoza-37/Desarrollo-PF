package com.poseidon.nauticaapi.dto.response;

import com.poseidon.nauticaapi.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/** Respuesta del login: token JWT, datos del usuario y su menú. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    @Builder.Default
    private String tokenType = "Bearer";
    private Long id;
    private String nombres;
    private String apellidos;
    private String correo;
    private String rol;
    private String rolNombre;
    private List<ModuloResponse> modulos;

    public static AuthResponse fromEntity(Usuario usuario, String token) {
        return AuthResponse.builder()
                .accessToken(token)
                .id(usuario.getId())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol().getCodigo())
                .rolNombre(usuario.getRol().getNombre())
                .modulos(ModuloResponse.fromEntities(usuario.getRol().getModulosActivosOrdenados()))
                .build();
    }
}
