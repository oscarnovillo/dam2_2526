package org.example.springsecurity.controller;

import jakarta.annotation.security.DenyAll;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import org.example.springsecurity.security.IsAdmin;
import org.example.springsecurity.security.IsAdminOrSelf;
import org.example.springsecurity.security.IsUser;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    /**
     * Endpoint público - no requiere autenticación
     * GET /api/public
     */
    @GetMapping("/public")
    public Map<String, Object> publicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Este es un endpoint público");
        response.put("timestamp", LocalDateTime.now());
        response.put("autenticado", false);
        return response;
    }

    /**
     * Endpoint protegido - requiere autenticación
     * GET /api/private
     */
    @GetMapping("/private")
    public Map<String, Object> privateEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Acceso autorizado! Este es un endpoint protegido");
        response.put("timestamp", LocalDateTime.now());
        response.put("usuario", authentication.getName());

        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        response.put("roles", roles);

        return response;
    }

    /**
     * Endpoint solo para ADMIN (configurado en SecurityConfig)
     * GET /api/admin
     */
    @GetMapping("/admin")
    public Map<String, Object> adminEndpoint(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "¡Bienvenido Admin! Tienes acceso total");
        response.put("timestamp", LocalDateTime.now());
        response.put("usuario", authentication.getName());
        response.put("esAdmin", true);
        return response;
    }

    // ============================================
    // EJEMPLOS DE SEGURIDAD A NIVEL DE MÉTODO
    // ============================================

    /**
     * OPCIÓN 1: @Secured - Solo valida roles (más simple)
     * Requiere prefijo ROLE_
     * GET /api/secured-admin
     */
    @Secured("ROLE_ADMIN")
    @GetMapping("/secured-admin")
    public Map<String, Object> securedAdminEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @Secured - Solo ADMIN");
        response.put("metodo", "@Secured(\"ROLE_ADMIN\")");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * OPCIÓN 2: @PreAuthorize - Se evalúa ANTES de ejecutar el método
     * Usa SpEL (Spring Expression Language) - más flexible
     * GET /api/preauthorize-admin
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/preauthorize-admin")
    public Map<String, Object> preAuthorizeAdminEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @PreAuthorize - Solo ADMIN");
        response.put("metodo", "@PreAuthorize(\"hasRole('ADMIN')\")");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @PreAuthorize con múltiples condiciones
     * GET /api/preauthorize-complex
     */
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    @GetMapping("/preauthorize-complex")
    public Map<String, Object> preAuthorizeComplexEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @PreAuthorize - ADMIN o USER");
        response.put("metodo", "@PreAuthorize(\"hasRole('ADMIN') or hasRole('USER')\")");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @PreAuthorize validando parámetros del método
     * Solo permite acceso si el username coincide con el usuario autenticado
     * GET /api/user/{username}
     */
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @GetMapping("/user/{username}")
    public Map<String, Object> getUserByUsername(@PathVariable String username, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso a datos del usuario: " + username);
        response.put("metodo", "@PreAuthorize(\"#username == authentication.name or hasRole('ADMIN')\")");
        response.put("usuarioSolicitado", username);
        response.put("usuarioAutenticado", auth.getName());
        return response;
    }

    /**
     * OPCIÓN 3: @PostAuthorize - Se evalúa DESPUÉS de ejecutar el método
     * Útil para validar el resultado antes de devolverlo
     * GET /api/postauthorize/{id}
     */
    @PostAuthorize("returnObject.get('owner') == authentication.name or hasRole('ADMIN')")
    @GetMapping("/postauthorize/{id}")
    public Map<String, Object> postAuthorizeEndpoint(@PathVariable String id, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Recurso con ID: " + id);
        response.put("metodo", "@PostAuthorize(\"returnObject.get('owner') == authentication.name\")");
        // Simula que el recurso pertenece al usuario "usuario"
        response.put("owner", "usuario");
        response.put("id", id);
        return response;
    }

    // ============================================
    // OPCIÓN 4: ANOTACIONES PERSONALIZADAS
    // ============================================

    /**
     * @IsAdmin - Anotación personalizada equivalente a @PreAuthorize("hasRole('ADMIN')")
     * GET /api/custom-admin
     */
    @IsAdmin
    @GetMapping("/custom-admin")
    public Map<String, Object> customAdminEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @IsAdmin - Anotación personalizada");
        response.put("metodo", "@IsAdmin");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @IsUser - Anotación personalizada para usuarios con rol USER
     * GET /api/custom-user
     */
    @IsUser
    @GetMapping("/custom-user")
    public Map<String, Object> customUserEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @IsUser - Anotación personalizada");
        response.put("metodo", "@IsUser");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @IsAdminOrSelf - Solo el propio usuario o un admin puede acceder
     * GET /api/profile/{username}
     */
    @IsAdminOrSelf
    @GetMapping("/profile/{username}")
    public Map<String, Object> getProfile(@PathVariable String username, Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Perfil del usuario: " + username);
        response.put("metodo", "@IsAdminOrSelf");
        response.put("usuarioSolicitado", username);
        response.put("usuarioAutenticado", auth.getName());
        return response;
    }

    // ============================================
    // OPCIÓN 5: JSR-250 (Estándar Java EE)
    // ============================================

    /**
     * @RolesAllowed - Equivalente a @Secured pero estándar Java EE
     * NO requiere prefijo ROLE_ (Spring lo añade automáticamente)
     * GET /api/jsr250-admin
     */
    @RolesAllowed("ADMIN")
    @GetMapping("/jsr250-admin")
    public Map<String, Object> jsr250AdminEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @RolesAllowed - Estándar JSR-250");
        response.put("metodo", "@RolesAllowed(\"ADMIN\")");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @RolesAllowed con múltiples roles
     * GET /api/jsr250-multiple
     */
    @RolesAllowed({"ADMIN", "USER"})
    @GetMapping("/jsr250-multiple")
    public Map<String, Object> jsr250MultipleRolesEndpoint(Authentication auth) {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @RolesAllowed - Múltiples roles");
        response.put("metodo", "@RolesAllowed({\"ADMIN\", \"USER\"})");
        response.put("usuario", auth.getName());
        return response;
    }

    /**
     * @PermitAll - Permite acceso a todos (incluso sin autenticar)
     * GET /api/jsr250-public
     */
    @PermitAll
    @GetMapping("/jsr250-public")
    public Map<String, Object> jsr250PublicEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Acceso con @PermitAll - Público para todos");
        response.put("metodo", "@PermitAll");
        response.put("timestamp", LocalDateTime.now());
        return response;
    }

    /**
     * @DenyAll - Deniega acceso a todos (útil para deshabilitar temporalmente)
     * GET /api/jsr250-denied
     */
    @DenyAll
    @GetMapping("/jsr250-denied")
    public Map<String, Object> jsr250DeniedEndpoint() {
        Map<String, Object> response = new HashMap<>();
        response.put("mensaje", "Este mensaje nunca se verá");
        response.put("metodo", "@DenyAll");
        return response;
    }
}

