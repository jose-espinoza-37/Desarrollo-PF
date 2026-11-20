package com.poseidon.nauticaapi.security;

import com.poseidon.nauticaapi.entity.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secretBase64,
                      @Value("${jwt.expiration}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretBase64));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        Date vence = new Date(ahora.getTime() + expiracionMs);


        List<Map<String, Object>> modulos = usuario.getRol().getModulosActivosOrdenados()
                .stream()
                .map(m -> {
                    Map<String, Object> mapa = new LinkedHashMap<>();
                    mapa.put("codigo", m.getCodigo());
                    mapa.put("nombre", m.getNombre());
                    mapa.put("ruta", m.getRuta());
                    mapa.put("icono", m.getIcono());
                    mapa.put("orden", m.getOrden());
                    return mapa;
                })
                .toList();

        return Jwts.builder()
                .subject(usuario.getCorreo())
                .claim("id", usuario.getId())
                .claim("nombres", usuario.getNombres() + " " + usuario.getApellidos())
                .claim("rol", usuario.getRol().getCodigo())
                .claim("rolNombre", usuario.getRol().getNombre())
                .claim("modulos", modulos)
                .issuedAt(ahora)
                .expiration(vence)
                .signWith(clave)
                .compact();
    }

    public Claims extraerClaims(String token) {
        return Jwts.parser()
                .verifyWith(clave)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extraerCorreo(String token) {
        return extraerClaims(token).getSubject();
    }

    public boolean esTokenValido(String token) {
        try {
            extraerClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
