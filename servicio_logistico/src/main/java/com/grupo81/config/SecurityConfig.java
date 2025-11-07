package com.grupo81.servicio_logistico.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos
                .requestMatchers(
                    "/swagger-ui/**",
                    "/api-docs/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/actuator/health"
                ).permitAll()
                
                // Endpoints de solicitudes
                .requestMatchers("POST", "/api/solicitudes").hasAnyRole("CLIENTE", "OPERADOR")
                .requestMatchers("GET", "/api/solicitudes/**").hasAnyRole("CLIENTE", "OPERADOR", "TRANSPORTISTA")
                .requestMatchers("GET", "/api/solicitudes/pendientes").hasRole("OPERADOR")
                
                // Endpoints de rutas
                .requestMatchers("/api/rutas/**").hasRole("OPERADOR")
                
                // Endpoints de tramos
                .requestMatchers("GET", "/api/tramos/**").hasAnyRole("OPERADOR", "TRANSPORTISTA")
                .requestMatchers("POST", "/api/tramos/*/iniciar").hasRole("TRANSPORTISTA")
                .requestMatchers("POST", "/api/tramos/*/finalizar").hasRole("TRANSPORTISTA")
                .requestMatchers("PUT", "/api/tramos/*/asignar-camion").hasRole("OPERADOR")
                
                // Endpoints de depósitos
                .requestMatchers("/api/depositos/**").hasRole("OPERADOR")
                
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );
        
        return http.build();
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return JwtDecoders.fromIssuerLocation(issuerUri);
    }
    
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        return converter;
    }
    
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        return new Converter<Jwt, Collection<GrantedAuthority>>() {
            @SuppressWarnings("unchecked")
            @Override
            public Collection<GrantedAuthority> convert(Jwt jwt) {
                // Extraer roles de Keycloak
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                Collection<String> roles;
                
                if (realmAccess != null && realmAccess.get("roles") instanceof Collection) {
                    roles = (Collection<String>) realmAccess.get("roles");
                } else {
                    roles = List.of();
                }
                
                // Convertir roles a GrantedAuthorities con prefijo ROLE_
                Stream<GrantedAuthority> rolesStream = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
                
                // También extraer authorities estándar
                JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
                Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);
                
                return Stream.concat(rolesStream, authorities != null ? authorities.stream() : Stream.empty())
                    .collect(Collectors.toSet());
            }
        };
    }
}