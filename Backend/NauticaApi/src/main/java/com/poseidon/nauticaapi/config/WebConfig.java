package com.poseidon.nauticaapi.config;

import com.poseidon.nauticaapi.auditoria.AuditoriaInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Registra el AuditoriaInterceptor en todos los endpoints de la API. */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuditoriaInterceptor auditoriaInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(auditoriaInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/actuator/**");
    }
}
