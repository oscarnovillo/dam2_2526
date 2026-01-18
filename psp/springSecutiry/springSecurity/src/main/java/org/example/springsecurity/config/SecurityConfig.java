package org.example.springsecurity.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
        securedEnabled = true,      // Habilita @Secured
        jsr250Enabled = true,       // Habilita @RolesAllowed, @PermitAll, @DenyAll (JSR-250)
        proxyTargetClass = true     // Usa CGLIB proxy (clases concretas, no solo interfaces)
)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authorize -> authorize
                // URLs públicas (sin autenticación)
                .requestMatchers("/", "/public", "/css/**", "/js/**").permitAll()
                // API pública
                .requestMatchers("/api/public").permitAll()
                // API solo para ADMIN
                .requestMatchers("/api/admin").hasRole("ADMIN")
                // URLs protegidas (requieren autenticación)
                .requestMatchers("/private", "/protected/**", "/api/**").authenticated()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                // Página de login personalizada (opcional, Spring genera una por defecto)
                .loginPage("/login")
                .defaultSuccessUrl("/private", true)
                .permitAll()
            )
            // Basic Authentication - el navegador muestra popup de login
            // Útil para APIs REST o pruebas con curl/Postman
            .httpBasic(Customizer.withDefaults())
            .logout(logout -> logout
                .logoutSuccessUrl("/")
                .permitAll()
            );

            // ⚠️ STATELESS - Descomentar para deshabilitar sesiones
            // Si activas esto, FormLogin NO funcionará correctamente
            // porque cada petición debe enviar credenciales (ideal para APIs REST + JWT)
            //
            // .sessionManagement(session -> session
            //     .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            // )
            // .formLogin(form -> form.disable())  // Desactivar formLogin
            // .httpBasic(Customizer.withDefaults())  // Solo Basic Auth

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Usuario en memoria para pruebas
        UserDetails user = User.builder()
            .username("usuario")
            .password(passwordEncoder().encode("password123"))
            .roles("USER")
            .build();

        UserDetails admin = User.builder()
            .username("admin")
            .password(passwordEncoder().encode("admin123"))
            .roles("ADMIN", "USER")
            .build();

        return new InMemoryUserDetailsManager(user, admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
