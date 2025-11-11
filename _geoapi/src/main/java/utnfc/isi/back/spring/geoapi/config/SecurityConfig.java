package utnfc.isi.back.spring.geoapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitamos CSRF (común en APIs stateless)
            .csrf(csrf -> csrf.disable())
            
            // Permitimos todas las peticiones
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll() 
            )
            
            // Configuramos la gestión de sesiones como STATELESS
            // (Spring Security no creará ni usará sesiones)
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            );
            
        return http.build();
    }
}