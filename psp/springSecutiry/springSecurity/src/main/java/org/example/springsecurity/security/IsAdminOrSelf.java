package org.example.springsecurity.security;

import org.springframework.security.access.prepost.PreAuthorize;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación personalizada que permite acceso si:
 * - El usuario es ADMIN, o
 * - El parámetro 'username' coincide con el usuario autenticado
 *
 * Uso: @IsAdminOrSelf en métodos con parámetro 'username'
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
public @interface IsAdminOrSelf {
}

