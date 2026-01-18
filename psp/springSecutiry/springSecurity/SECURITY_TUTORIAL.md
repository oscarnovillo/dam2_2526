# Spring Security - Tutorial de las 5 formas de control de acceso

Este proyecto es un tutorial completo de Spring Security que demuestra las diferentes formas de controlar el acceso a recursos.

## Credenciales de prueba

| Usuario | Contraseña | Roles |
|---------|------------|-------|
| `usuario` | `password123` | USER |
| `admin` | `admin123` | ADMIN, USER |

## Configuración básica

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(
    securedEnabled = true,      // Habilita @Secured
    jsr250Enabled = true,       // Habilita @RolesAllowed, @PermitAll, @DenyAll
    proxyTargetClass = true     // Usa CGLIB proxy (clases concretas)
)
public class SecurityConfig { }
```

---

## Las 5 formas de controlar la seguridad

### 1️⃣ SecurityConfig (URL-based)

Control centralizado por URLs en la clase de configuración.

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/", "/public").permitAll()           // Público
            .requestMatchers("/api/admin").hasRole("ADMIN")        // Solo ADMIN
            .requestMatchers("/api/**").authenticated()            // Autenticado
            .anyRequest().authenticated()
        );
    return http.build();
}
```

**Métodos disponibles:**
| Método | Descripción |
|--------|-------------|
| `.permitAll()` | Acceso público |
| `.authenticated()` | Requiere autenticación |
| `.hasRole("ADMIN")` | Requiere rol específico |
| `.hasAnyRole("ADMIN", "USER")` | Requiere cualquiera de los roles |
| `.hasAuthority("READ")` | Requiere authority específica |
| `.denyAll()` | Deniega todo acceso |

---

### 2️⃣ @Secured (Spring Security)

Anotación simple de Spring Security. **Requiere prefijo `ROLE_`**.

```java
@Secured("ROLE_ADMIN")
@GetMapping("/secured-admin")
public String adminOnly() { }

@Secured({"ROLE_ADMIN", "ROLE_USER"})
@GetMapping("/secured-multiple")
public String multipleRoles() { }
```

**Características:**
- ✅ Simple y directo
- ❌ Requiere prefijo `ROLE_`
- ❌ No soporta expresiones SpEL

---

### 3️⃣ @PreAuthorize / @PostAuthorize (SpEL)

Anotaciones más flexibles usando Spring Expression Language (SpEL).

#### @PreAuthorize - Se evalúa ANTES de ejecutar el método

```java
// Simple
@PreAuthorize("hasRole('ADMIN')")
public String adminOnly() { }

// Múltiples condiciones
@PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
public String multipleRoles() { }

// Usando parámetros del método
@PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
public User getUser(@PathVariable String username) { }

// Verificando authorities
@PreAuthorize("hasAuthority('READ')")
public List<Data> readData() { }
```

#### @PostAuthorize - Se evalúa DESPUÉS de ejecutar el método

```java
// Valida el resultado antes de devolverlo
@PostAuthorize("returnObject.owner == authentication.name or hasRole('ADMIN')")
public Resource getResource(Long id) { }
```

**Expresiones SpEL disponibles:**
| Expresión | Descripción |
|-----------|-------------|
| `hasRole('ADMIN')` | Tiene el rol (añade ROLE_ automáticamente) |
| `hasAnyRole('ADMIN', 'USER')` | Tiene alguno de los roles |
| `hasAuthority('READ')` | Tiene la authority exacta |
| `isAuthenticated()` | Está autenticado |
| `isAnonymous()` | Es anónimo |
| `#param` | Accede a parámetros del método |
| `authentication` | Objeto Authentication actual |
| `principal` | Usuario principal |
| `returnObject` | Objeto retornado (solo @PostAuthorize) |

---

### 4️⃣ Anotaciones personalizadas

Crear tus propias anotaciones encapsulando la lógica de seguridad.

#### Definición de la anotación

```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("hasRole('ADMIN')")
public @interface IsAdmin {
}
```

#### Uso

```java
@IsAdmin
@GetMapping("/custom-admin")
public String adminOnly() { }
```

#### Ejemplos incluidos en el proyecto

| Anotación | Lógica |
|-----------|--------|
| `@IsAdmin` | `hasRole('ADMIN')` |
| `@IsUser` | `hasRole('USER')` |
| `@IsAdminOrSelf` | `hasRole('ADMIN') or #username == authentication.name` |

**Ventajas:**
- ✅ Código más legible
- ✅ Reutilizable en toda la aplicación
- ✅ Cambios centralizados
- ✅ Documenta la intención

---

### 5️⃣ JSR-250 (Estándar Java EE)

Anotaciones del estándar Java EE, portables a otros frameworks.

```java
// Solo ADMIN (NO requiere prefijo ROLE_)
@RolesAllowed("ADMIN")
public String adminOnly() { }

// Múltiples roles
@RolesAllowed({"ADMIN", "USER"})
public String multipleRoles() { }

// Acceso público
@PermitAll
public String publicEndpoint() { }

// Denegado para todos (útil para deshabilitar temporalmente)
@DenyAll
public String disabledEndpoint() { }
```

**Anotaciones JSR-250:**
| Anotación | Descripción |
|-----------|-------------|
| `@RolesAllowed("ADMIN")` | Requiere rol(es) específico(s) |
| `@PermitAll` | Permite acceso a todos |
| `@DenyAll` | Deniega acceso a todos |

**Ventajas:**
- ✅ Estándar Java EE
- ✅ Portable (funciona en Jakarta EE, etc.)
- ✅ No requiere prefijo `ROLE_`

---

## Comparativa

| # | Método | Dónde | Prefijo ROLE_ | SpEL | Portable |
|---|--------|-------|---------------|------|----------|
| 1 | SecurityConfig | Configuración | No | Limitado | No |
| 2 | @Secured | Método/Clase | Sí | No | No |
| 3 | @PreAuthorize | Método/Clase | No | Sí | No |
| 4 | Custom Annotation | Método/Clase | Depende | Sí | No |
| 5 | JSR-250 | Método/Clase | No | No | Sí |

---

## Autenticación

### FormLogin (con sesión)
```java
.formLogin(form -> form
    .loginPage("/login")
    .defaultSuccessUrl("/private", true)
    .permitAll()
)
```

### Basic Authentication (para APIs)
```java
.httpBasic(Customizer.withDefaults())
```

### JWT Authentication (Stateless)

JWT (JSON Web Token) es ideal para APIs REST porque es stateless - no requiere sesiones en el servidor.

#### Flujo de autenticación JWT:
```
1. Cliente envía credenciales → POST /api/auth/login
2. Servidor valida y devuelve token JWT
3. Cliente guarda el token
4. Cliente envía token en cada petición → Authorization: Bearer <token>
5. Servidor valida el token y permite/deniega acceso
```

#### Configuración:

**1. Dependencias (pom.xml):**
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

**2. Propiedades (application.properties):**
```properties
jwt.secret=TuClaveSecretaBase64Encoded
jwt.expiration=86400000  # 24 horas en milisegundos
```

**3. Clases necesarias:**

| Clase | Descripción |
|-------|-------------|
| `JwtService` | Genera y valida tokens JWT |
| `JwtAuthenticationFilter` | Filtro que intercepta peticiones y valida tokens |
| `AuthController` | Endpoint `/api/auth/login` para obtener token |

**4. SecurityConfig para API (Stateless):**
```java
@Bean
@Order(1)
public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
    http
        .securityMatcher("/api/**")
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(authorize -> authorize
            .requestMatchers("/api/auth/**").permitAll()
            .anyRequest().authenticated()
        )
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
    
    return http.build();
}
```

#### Uso:

**1. Obtener token:**
```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "usuario",
  "password": "password123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tipo": "Bearer",
  "usuario": "usuario",
  "roles": ["ROLE_USER"]
}
```

**2. Usar token en peticiones:**
```http
GET /api/private
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Estructura del token JWT:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.  ← Header (algoritmo)
eyJzdWIiOiJ1c3VhcmlvIiwiaWF0IjoxNjE2..  ← Payload (datos)
SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c  ← Signature (firma)
```

### Stateless (sin sesión - para JWT/APIs REST)
```java
.sessionManagement(session -> session
    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
)
.formLogin(form -> form.disable())
```

---

## Filter vs Interceptor

### Diferencia conceptual

| Aspecto | Filter | Interceptor |
|---------|--------|-------------|
| **Nivel** | Servlet (más bajo) | Spring MVC (más alto) |
| **Cuándo se ejecuta** | Antes de que llegue a Spring | Después de DispatcherServlet |
| **Acceso a** | Request/Response HTTP raw | Handler, ModelAndView, contexto Spring |
| **Configuración** | `FilterRegistrationBean` o `@Component` | `WebMvcConfigurer` |
| **Orden de ejecución** | 1️⃣ Primero | 2️⃣ Después |

### Flujo de una petición

```
                    ┌─────────────────────────────────────────────────┐
                    │                    SERVIDOR                      │
                    │                                                  │
  Request ─────────▶│  Filter ──▶ DispatcherServlet ──▶ Interceptor ──▶ Controller
                    │    ▲              │                    │              │
                    │    │              ▼                    ▼              │
  Response ◀────────│  Filter ◀── DispatcherServlet ◀── Interceptor ◀──────┘
                    │                                                  │
                    └─────────────────────────────────────────────────┘
```

### ¿Por qué JWT usa Filter y no Interceptor?

1. **Seguridad debe ser lo primero** - El filtro se ejecuta antes que cualquier cosa de Spring
2. **Spring Security usa Filters** - Toda la cadena de seguridad son filtros
3. **Puede bloquear antes** - Si el token es inválido, ni siquiera llega al DispatcherServlet

### Ejemplo de Filter (JWT)

```java
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // Obtener el header Authorization
        final String authHeader = request.getHeader("Authorization");

        // Si no hay header o no empieza con "Bearer ", continuar con el siguiente filtro
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extraer el token (quitar "Bearer ")
        final String jwt = authHeader.substring(7);

        // Extraer el username del token
        final String username = jwtService.extractUsername(jwt);

        // Si hay username y no hay autenticación previa en el contexto
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Cargar el usuario desde el UserDetailsService
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // Validar el token
            if (jwtService.isTokenValid(jwt, userDetails)) {

                // Crear el objeto de autenticación
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,  // No necesitamos las credenciales
                        userDetails.getAuthorities()
                );

                // Añadir detalles de la request
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // Establecer la autenticación en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // Continuar con el siguiente filtro
        filterChain.doFilter(request, response);
    }
}
```

### Ejemplo de Interceptor

```java
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) {
        // Se ejecuta ANTES del controller (después de los filtros)
        System.out.println("Antes del controller: " + request.getRequestURI());
        return true;  // true = continuar, false = bloquear
    }
    
    @Override
    public void postHandle(HttpServletRequest request,
                          HttpServletResponse response,
                          Object handler,
                          ModelAndView modelAndView) {
        // Se ejecuta DESPUÉS del controller, ANTES de la vista
        System.out.println("Después del controller");
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request,
                               HttpServletResponse response,
                               Object handler,
                               Exception ex) {
        // Se ejecuta DESPUÉS de todo (para limpiar recursos)
        System.out.println("Petición completada");
    }
}

// Registrar el interceptor
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Autowired
    private LoggingInterceptor loggingInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")           // Aplica a todas las URLs
                .excludePathPatterns("/static/**"); // Excepto estáticos
    }
}
```

### ¿Cuándo usar cada uno?

| Caso de uso | Usar |
|-------------|------|
| Autenticación/Seguridad (JWT, OAuth) | **Filter** |
| Logging de requests HTTP raw | **Filter** |
| CORS | **Filter** |
| Validar headers específicos | **Filter** |
| Logging de tiempos de controller | **Interceptor** |
| Modificar ModelAndView | **Interceptor** |
| Auditoría de acciones de usuario | **Interceptor** |
| Validaciones de negocio pre-controller | **Interceptor** |

### Resumen

- **Filter** = Portero del edificio (controla quién entra)
- **Interceptor** = Recepcionista (controla qué hace una vez dentro)

Para JWT usamos **Filter** porque la seguridad debe validarse antes de que la petición llegue a cualquier parte de Spring MVC.

---

## Probar la API

### Con IntelliJ HTTP Client
Usar el archivo `src/test/resources/api-tests.http`

### Con curl

```bash
# Endpoint público
curl http://localhost:8080/api/public

# Endpoint privado (Basic Auth)
curl -u usuario:password123 http://localhost:8080/api/private

# Endpoint admin
curl -u admin:admin123 http://localhost:8080/api/admin
```

### Credenciales Base64

| Credenciales | Base64 |
|--------------|--------|
| `usuario:password123` | `dXN1YXJpbzpwYXNzd29yZDEyMw==` |
| `admin:admin123` | `YWRtaW46YWRtaW4xMjM=` |

---

## Estructura del proyecto

```
src/main/java/org/example/springsecurity/
├── config/
│   └── SecurityConfig.java          # Configuración de seguridad
├── controller/
│   ├── HomeController.java          # Controlador web (HTML)
│   └── ApiController.java           # Controlador REST (JSON)
└── security/
    ├── IsAdmin.java                  # Anotación personalizada
    ├── IsUser.java                   # Anotación personalizada
    └── IsAdminOrSelf.java            # Anotación personalizada

src/test/resources/
└── api-tests.http                    # Tests HTTP para IntelliJ
```

---

## Endpoints disponibles

### Públicos (sin autenticación)
| Método | URL | Descripción |
|--------|-----|-------------|
| GET | `/api/public` | API pública |
| GET | `/api/jsr250-public` | @PermitAll |

### Requieren autenticación
| Método | URL | Anotación |
|--------|-----|-----------|
| GET | `/api/private` | SecurityConfig |
| GET | `/api/preauthorize-complex` | @PreAuthorize (ADMIN or USER) |
| GET | `/api/jsr250-multiple` | @RolesAllowed (ADMIN, USER) |
| GET | `/api/custom-user` | @IsUser |

### Solo ADMIN
| Método | URL | Anotación |
|--------|-----|-----------|
| GET | `/api/admin` | SecurityConfig |
| GET | `/api/secured-admin` | @Secured |
| GET | `/api/preauthorize-admin` | @PreAuthorize |
| GET | `/api/custom-admin` | @IsAdmin |
| GET | `/api/jsr250-admin` | @RolesAllowed |

### Condicionales
| Método | URL | Condición |
|--------|-----|-----------|
| GET | `/api/user/{username}` | Usuario propio o ADMIN |
| GET | `/api/profile/{username}` | @IsAdminOrSelf |
| GET | `/api/postauthorize/{id}` | Owner o ADMIN |

### Siempre denegado
| Método | URL | Anotación |
|--------|-----|-----------|
| GET | `/api/jsr250-denied` | @DenyAll |

