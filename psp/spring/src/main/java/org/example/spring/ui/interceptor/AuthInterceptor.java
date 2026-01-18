package org.example.spring.ui.interceptor;


import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.spring.ui.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private final AuthService authService;
    private final ObjectMapper objectMapper;

    public AuthInterceptor(AuthService authService, ObjectMapper objectMapper) {
        this.authService = authService;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        request.getHeader("Authorization");


        request.setAttribute("rol","admin");

        // Verificar si el endpoint está en /api/
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            return true;
        }

        // Excluir endpoints públicos
        if (path.startsWith("/api/auth/login") ||
            path.startsWith("/api/auth/register") ||
            path.startsWith("/api/auth/activar") ||
            path.startsWith("/api/auth/session")) {
            return true;
        }

        RequiresAuth requiresAuth = handlerMethod.getMethodAnnotation(RequiresAuth.class);
        String httpMethod = request.getMethod();

        // Si tiene anotación @RequiresAuth, usarla
        if (requiresAuth != null) {
            if (!authService.isAuthenticated(request.getSession())) {
                sendJsonError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
                return false;
            }

            if (requiresAuth.admin() && !authService.isAdmin(request.getSession())) {
                sendJsonError(response, HttpStatus.FORBIDDEN, "Acceso denegado");
                return false;
            }

            return true;
        }

        // Reglas automáticas por método HTTP
        switch (httpMethod) {
            case "GET":
                // GET requiere USER (cualquier usuario autenticado)
                if (!authService.isAuthenticated(request.getSession())) {
                    sendJsonError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder a este recurso");
                    return false;
                }
                break;

            case "POST", "PUT", "PATCH", "DELETE":
                // POST, PUT, PATCH, DELETE requieren ADMIN
                if (!authService.isAuthenticated(request.getSession())) {
                    sendJsonError(response, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión");
                    return false;
                }

                if (!authService.isAdmin(request.getSession())) {
                    sendJsonError(response, HttpStatus.FORBIDDEN, "Solo los administradores pueden realizar esta acción");
                    return false;
                }
                break;

            default:
                // Otros métodos HTTP (OPTIONS, HEAD, etc.) pasan sin verificación
                break;
        }

        return true;
    }

    private void sendJsonError(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", message)));
    }
}
