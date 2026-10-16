package com.techinventory.inventario.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.security.tenant-id}")
    private String tenantId;

    @Value("${app.security.issuer-uri}")
    private String issuerUri;

    @Value("${app.security.audiences}")
    private String[] audiences;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/inventario/**", "/api/inventario/**")
                    .hasAnyAuthority("SCOPE_api.read", "SCOPE_api.write")
                .requestMatchers("/api/v1/inventario/**", "/api/inventario/**")
                    .hasAuthority("SCOPE_api.write")
                .anyRequest().authenticated())
            .oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthConverter())));

        return http.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = "https://login.microsoftonline.com/" + tenantId + "/discovery/v2.0/keys";
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();

        List<String> issuersPermitidos = List.of(
                issuerUri,
                "https://sts.windows.net/" + tenantId + "/");
        List<String> audiencesPermitidas = Arrays.asList(audiences);

        OAuth2TokenValidator<Jwt> timestamp = new JwtTimestampValidator();
        OAuth2TokenValidator<Jwt> issuer = new JwtClaimValidator<Object>(
                "iss", iss -> iss != null && issuersPermitidos.contains(String.valueOf(iss)));
        OAuth2TokenValidator<Jwt> audience = jwt -> {
            boolean valida = jwt.getAudience() != null
                    && jwt.getAudience().stream().anyMatch(audiencesPermitidas::contains);
            return valida
                    ? OAuth2TokenValidatorResult.success()
                    : OAuth2TokenValidatorResult.failure(new OAuth2Error("invalid_token", "Audience no permitida", null));
        };

        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(timestamp, issuer, audience));
        return decoder;
    }

    private JwtAuthenticationConverter jwtAuthConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<GrantedAuthority> authorities = new ArrayList<>();

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roles.forEach(r -> authorities.add(new SimpleGrantedAuthority("ROLE_" + r.toUpperCase())));
            }

            String scp = jwt.getClaimAsString("scp");
            if (scp != null && !scp.isBlank()) {
                for (String scope : scp.split(" ")) {
                    authorities.add(new SimpleGrantedAuthority("SCOPE_" + scope));
                }
            }
            return authorities;
        });
        return converter;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedOrigins(List.of(
                "http://localhost:4200",
                "http://127.0.0.1:4200",
                "https://lk7j2r33td.execute-api.us-east-1.amazonaws.com"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*", "Authorization", "Content-Type"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
