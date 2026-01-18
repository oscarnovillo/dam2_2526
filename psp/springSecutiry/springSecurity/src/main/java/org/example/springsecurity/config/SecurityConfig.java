package org.example.springsecurity.config;

import lombok.RequiredArgsConstructor;
import org.example.springsecurity.security.jwt.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,      // Habilita @Secured
        jsr250Enabled = true,       // Habilita @RolesAllowed, @PermitAll, @DenyAll (JSR-250)
        proxyTargetClass = true     // Usa CGLIB proxy (clases concretas, no solo interfaces)
)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    // ============================================
    // OPCIÓN 1: SecurityFilterChain para API REST (JWT + Stateless)
    // ============================================
    @Bean
    @Order(1)  // Prioridad alta - se evalúa primero
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            // Solo aplica a rutas /api/**
            .securityMatcher("/api/**")
            .csrf(csrf -> csrf.disable())  // Deshabilitar CSRF para API REST
            .authorizeHttpRequests(authorize -> authorize
                // Endpoints públicos de la API
                .requestMatchers("/api/public", "/api/auth/**", "/api/jsr250-public").permitAll()
                // API solo para ADMIN
                .requestMatchers("/api/admin").hasRole("ADMIN")
                // El resto de la API requiere autenticación
                .anyRequest().authenticated()
            )
            // Sin sesiones (stateless) - cada petición debe tener el token
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            // Añadir el filtro JWT antes del filtro de autenticación
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            // También permitir Basic Auth para la API (opcional)
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    // ============================================
    // OPCIÓN 2: SecurityFilterChain para Web (FormLogin + Sesiones)
    // ============================================
    @Bean
    @Order(2)  // Prioridad menor - se evalúa después
    public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // URLs públicas (sin autenticación)
                .requestMatchers("/", "/public", "/css/**", "/js/**").permitAll()
                // Consola H2 (solo desarrollo)
                .requestMatchers("/h2-console/**").permitAll()
                // URLs protegidas (requieren autenticación)
                .requestMatchers("/private", "/protected/**").authenticated()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            // Permitir frames para H2 Console (usa iframes)
            .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
            // Deshabilitar CSRF para H2 Console
            .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
            .formLogin(form -> form
                // Página de login personalizada
                .loginPage("/login")
                .defaultSuccessUrl("/private", true)
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

        return http.build();
    }


}
