package com.poseidon.nauticaapi.auditoria;

import com.poseidon.nauticaapi.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.InetAddress;
import java.util.UUID;

/**
 * Interceptor HTTP que llena el {@link AuditoriaContext} antes de cada request
 * y lo limpia al terminar, para que no se mezclen datos entre hilos.
 *
 * <p>La IP se toma de {@code request.getRemoteAddr()}. Detrás del proxy de Render
 * la IP real llega gracias a {@code server.forward-headers-strategy=FRAMEWORK}.</p>
 */
@Component
public class AuditoriaInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuditoriaInterceptor.class);

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        // 1. Contexto nuevo para este hilo
        AuditoriaContext.inicializar();

        // 2. Usuario autenticado: el filtro JWT deja su id en la petición (0 = anónimo)
        Object id = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ID_USUARIO);
        Long idUsuario = id instanceof Long valor ? valor : 0L;
        AuditoriaContext.setIdUsuario(idUsuario);

        // 3. IP de origen (sin sufijos de zona IPv6 ni el prefijo ::ffff:)
        AuditoriaContext.setIpOrigen(normalizarIp(request.getRemoteAddr()));

        // 4. Método HTTP y endpoint
        AuditoriaContext.setMetodoHttp(request.getMethod());
        AuditoriaContext.setEndpoint(request.getRequestURI());

        // 5. UUID de correlación por request
        AuditoriaContext.setIdInteraccion(UUID.randomUUID());

        // 6. Versión de la aplicación
        AuditoriaContext.setVersionAplicacion(appVersion);

        log.debug("[Auditoria] usuario={} ip={} endpoint={}",
                idUsuario, AuditoriaContext.getIpOrigen(), request.getRequestURI());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        // Limpiar SIEMPRE para evitar fugas de datos entre requests del pool
        AuditoriaContext.limpiar();
    }

    /** Deja la IP en un formato que PostgreSQL acepte como tipo inet. */
    private String normalizarIp(String ip) {
        if (ip == null || ip.isBlank()) return null;
        int zona = ip.indexOf('%');
        if (zona != -1) ip = ip.substring(0, zona);
        if (ip.toLowerCase().startsWith("::ffff:")) return ip.substring(7);
        try {
            return InetAddress.getByName(ip).getHostAddress();
        } catch (java.net.UnknownHostException e) {
            return null;
        }
    }
}
