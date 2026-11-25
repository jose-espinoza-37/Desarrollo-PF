package com.poseidon.nauticaapi.controller;

import com.poseidon.nauticaapi.dto.response.AppInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/** Ruta pública para comprobar que la API desplegada está en línea. */
@RestController
public class AppInfoController {

    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.version:1.0.0}")
    private String appVersion;

    @Value("${info.app.description:}")
    private String appDescription;

    @GetMapping("/")
    public ResponseEntity<AppInfoResponse> estado() {
        return ResponseEntity.ok(AppInfoResponse.builder()
                .aplicacion(appName)
                .descripcion(appDescription)
                .version(appVersion)
                .estado("UP")
                .build());
    }
}
