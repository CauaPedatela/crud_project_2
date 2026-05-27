package com.crudproject.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

// Libera o Angular (http://localhost:4200) para chamar os endpoints /api/** do Spring Boot.
// Usamos CorsFilter (em vez de WebMvcConfigurer) porque ele roda ANTES do filtro do Wicket,
// garantindo que requisições OPTIONS de preflight não sejam interceptadas pelo Wicket.

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Permite chamadas vindas do servidor de desenvolvimento do Angular
        config.addAllowedOrigin("http://localhost:4200");

        // Permite todos os métodos HTTP usados pela API REST
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");

        // Permite todos os cabeçalhos (Content-Type, Accept, etc.)
        config.addAllowedHeader("*");

        // Permite cookies de sessão se necessário no futuro
        config.setAllowCredentials(true);

        // Aplica a configuração apenas nas rotas da API REST, não nas rotas do Wicket
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }
}
