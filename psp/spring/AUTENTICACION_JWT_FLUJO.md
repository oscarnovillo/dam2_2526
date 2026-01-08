# Flujo de Autenticación con JWT (JSON Web Tokens)

## 📋 Índice
1. [Introducción](#introducción)
2. [¿Qué es JWT?](#qué-es-jwt)
3. [Access Token vs Refresh Token](#access-token-vs-refresh-token)
4. [Flujo Completo de Autenticación](#flujo-completo-de-autenticación)
5. [Implementación en Spring Boot](#implementación-en-spring-boot)
6. [Comparación: Sesiones vs JWT](#comparación-sesiones-vs-jwt)

---

## Introducción

Este documento describe cómo funciona la autenticación basada en **JWT (JSON Web Tokens)** en lugar del tradicional sistema de sesiones del servidor.

## ¿Qué es JWT?

JWT es un estándar abierto (RFC 7519) que define una forma compacta y autónoma de transmitir información de forma segura entre partes como un objeto JSON.

### Estructura de un JWT

Un JWT consta de tres partes separadas por puntos (`.`):

```
xxxxx.yyyyy.zzzzz
```

1. **Header (Cabecera)**: Tipo de token y algoritmo de firma
   ```json
   {
     "alg": "HS256",
     "typ": "JWT"
   }
   ```

2. **Payload (Carga útil)**: Claims (reclamaciones) - datos del usuario
   ```json
   {
     "sub": "oscar",
     "auth": "ADMIN",
     "iat": 1516239022,
     "exp": 1516242622
   }
   ```

3. **Signature (Firma)**: Verificación de integridad
   ```
   HMACSHA256(
     base64UrlEncode(header) + "." + base64UrlEncode(payload),
     secret
   )
   ```

### Ejemplo de JWT completo:
```
eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvc2NhciIsImF1dGgiOiJBRE1JTiIsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjoxNTE2MjQyNjIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
```

---

## Access Token vs Refresh Token

### 🔑 Access Token
- **Duración**: Corta (15 minutos - 1 hora)
- **Propósito**: Acceder a recursos protegidos
- **Uso**: Se envía en cada petición HTTP
- **Seguridad**: Si se compromete, solo es válido por poco tiempo

### 🔄 Refresh Token
- **Duración**: Larga (7 días - 30 días)
- **Propósito**: Obtener nuevos access tokens
- **Uso**: Solo para renovar tokens
- **Seguridad**: Se almacena de forma más segura
- **Almacenamiento**: HttpOnly Cookie o localStorage protegido

---

## Flujo Completo de Autenticación

### 1️⃣ Registro de Usuario

```
┌─────────┐                              ┌─────────┐
│ Cliente │                              │ Servidor│
└────┬────┘                              └────┬────┘
     │                                        │
     │  POST /api/auth/register               │
     │  { username, password, email }         │
     │───────────────────────────────────────>│
     │                                        │
     │                                        │ 1. Validar datos
     │                                        │ 2. Hash password (BCrypt)
     │                                        │ 3. Guardar en BD
     │                                        │
     │  { success: true, usuario: {...} }     │
     │<───────────────────────────────────────│
     │                                        │
```

**Código:**
```java
@PostMapping("/register")
public ResponseEntity<?> register(@RequestBody RegistroRequest request) {
    // 1. Verificar si el usuario ya existe
    if (usuarioRepository.existsByUsername(request.username())) {
        return ResponseEntity.badRequest()
            .body(Map.of("error", "Usuario ya existe"));
    }
    
    // 2. Hashear la contraseña
    String hashedPassword = passwordEncoder.encode(request.password());
    
    // 3. Guardar usuario
    Usuario usuario = usuarioRepository.save(new Usuario(
        null, request.username(), hashedPassword, 
        request.email(), request.nombre(), "USER"
    ));
    
    return ResponseEntity.ok(Map.of("success", true));
}
```

---

### 2️⃣ Login - Obtención de Tokens

```
┌─────────┐                              ┌─────────┐
│ Cliente │                              │ Servidor│
└────┬────┘                              └────┬────┘
     │                                        │
     │  POST /api/auth/login                  │
     │  { username: "oscar", password: "..." }│
     │───────────────────────────────────────>│
     │                                        │
     │                                        │ 1. Buscar usuario en BD
     │                                        │ 2. Verificar password
     │                                        │ 3. Generar Access Token (15min)
     │                                        │ 4. Generar Refresh Token (7días)
     │                                        │
     │  {                                     │
     │    accessToken: "eyJhbG...",           │
     │    refreshToken: "eyJhbG...",          │
     │    expiresIn: 900                      │
     │  }                                     │
     │<───────────────────────────────────────│
     │                                        │
     │  Almacenar tokens en:                  │
     │  - localStorage o                      │
     │  - sessionStorage o                    │
     │  - Cookie HttpOnly (más seguro)        │
     │                                        │
```

**Código:**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request) {
    // 1. Buscar usuario
    Optional<Usuario> usuarioOpt = usuarioRepository
        .findByUsername(request.username());
    
    if (usuarioOpt.isEmpty()) {
        return ResponseEntity.status(401)
            .body(Map.of("error", "Credenciales inválidas"));
    }
    
    Usuario usuario = usuarioOpt.get();
    
    // 2. Verificar password
    if (!passwordEncoder.matches(request.password(), usuario.password())) {
        return ResponseEntity.status(401)
            .body(Map.of("error", "Credenciales inválidas"));
    }
    
    // 3. Generar tokens
    String accessToken = jwtService.generateAccessToken(usuario.username());
    String refreshToken = jwtService.generateRefreshToken(usuario.username());
    
    return ResponseEntity.ok(Map.of(
        "accessToken", accessToken,
        "refreshToken", refreshToken,
        "expiresIn", 900, // 15 minutos en segundos
        "usuario", Map.of(
            "username", usuario.username(),
            "rol", usuario.rol()
        )
    ));
}
```

---

### 3️⃣ Acceso a Recursos Protegidos

```
┌─────────┐                              ┌─────────┐
│ Cliente │                              │ Servidor│
└────┬────┘                              └────┬────┘
     │                                        │
     │  GET /api/cosas                        │
     │  Header: Authorization: Bearer eyJhbG..│
     │───────────────────────────────────────>│
     │                                        │
     │                                        │ 1. Extraer token del Header
     │                                        │ 2. Validar firma del token
     │                                        │ 3. Verificar expiración
     │                                        │ 4. Extraer username del token
     │                                        │ 5. Procesar petición
     │                                        │
     │  { datos: [...] }                      │
     │<───────────────────────────────────────│
     │                                        │
```

**Interceptor/Filter:**
```java
@Component
public class JwtAuthenticationFilter implements Filter {
    
    private final JwtService jwtService;
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, 
                         FilterChain chain) {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        
        // 1. Extraer token del header
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                // 2. Validar token
                String username = jwtService.extractUsername(token);
                
                if (jwtService.isTokenValid(token, username)) {
                    // 3. Token válido - continuar
                    request.setAttribute("username", username);
                    request.setAttribute("rol", jwtService.extractRol(token));
                    chain.doFilter(request, response);
                    return;
                }
            } catch (Exception e) {
                // Token inválido
            }
        }
        
        // Sin token o inválido
        ((HttpServletResponse) response).setStatus(401);
    }
}
```

**Controlador protegido:**
```java
@RestController
@RequestMapping("/api/cosas")
public class CosasController {
    
    @GetMapping
    public ResponseEntity<?> listarCosas(HttpServletRequest request) {
        // El username ya fue validado por el filtro
        String username = (String) request.getAttribute("username");
        String rol = (String) request.getAttribute("rol");
        
        // Lógica del negocio
        List<Cosa> cosas = cosaRepository.findAll();
        return ResponseEntity.ok(cosas);
    }
}
```

---

### 4️⃣ Renovación de Access Token (con Refresh Token)

```
┌─────────┐                              ┌─────────┐
│ Cliente │                              │ Servidor│
└────┬────┘                              └────┬────┘
     │                                        │
     │  GET /api/cosas                        │
     │  Authorization: Bearer <expired_token> │
     │───────────────────────────────────────>│
     │                                        │
     │  401 Unauthorized                      │
     │  { error: "Token expirado" }           │
     │<───────────────────────────────────────│
     │                                        │
     │  POST /api/auth/refresh                │
     │  { refreshToken: "eyJhbG..." }         │
     │───────────────────────────────────────>│
     │                                        │
     │                                        │ 1. Validar refresh token
     │                                        │ 2. Verificar expiración
     │                                        │ 3. Generar nuevo access token
     │                                        │ 4. (Opcional) Rotar refresh token
     │                                        │
     │  {                                     │
     │    accessToken: "eyJnew...",           │
     │    refreshToken: "eyJnew..." (opcional)│
     │    expiresIn: 900                      │
     │  }                                     │
     │<───────────────────────────────────────│
     │                                        │
     │  Actualizar tokens almacenados         │
     │                                        │
     │  Reintentar petición original          │
     │  GET /api/cosas                        │
     │  Authorization: Bearer <new_token>     │
     │───────────────────────────────────────>│
     │                                        │
     │  200 OK { datos: [...] }               │
     │<───────────────────────────────────────│
     │                                        │
```

**Código:**
```java
@PostMapping("/refresh")
public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
    try {
        String refreshToken = request.refreshToken();
        
        // 1. Validar refresh token
        String username = jwtService.extractUsername(refreshToken);
        
        if (!jwtService.isTokenValid(refreshToken, username)) {
            return ResponseEntity.status(401)
                .body(Map.of("error", "Refresh token inválido"));
        }
        
        // 2. Generar nuevo access token
        String newAccessToken = jwtService.generateAccessToken(username);
        
        // 3. (Opcional) Rotar refresh token para mayor seguridad
        String newRefreshToken = jwtService.generateRefreshToken(username);
        
        return ResponseEntity.ok(Map.of(
            "accessToken", newAccessToken,
            "refreshToken", newRefreshToken,
            "expiresIn", 900
        ));
        
    } catch (Exception e) {
        return ResponseEntity.status(401)
            .body(Map.of("error", "Token inválido"));
    }
}
```

**Lógica en el Cliente (JavaScript):**
```javascript
// Interceptor de Axios para manejar tokens expirados
axios.interceptors.response.use(
    response => response,
    async error => {
        const originalRequest = error.config;
        
        // Si el token expiró y no hemos reintentado
        if (error.response.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            
            try {
                // Intentar renovar el token
                const refreshToken = localStorage.getItem('refreshToken');
                const response = await axios.post('/api/auth/refresh', {
                    refreshToken
                });
                
                // Guardar nuevos tokens
                const { accessToken, refreshToken: newRefreshToken } = response.data;
                localStorage.setItem('accessToken', accessToken);
                localStorage.setItem('refreshToken', newRefreshToken);
                
                // Reintentar petición original con nuevo token
                originalRequest.headers['Authorization'] = `Bearer ${accessToken}`;
                return axios(originalRequest);
                
            } catch (refreshError) {
                // Refresh token también expiró - redirigir a login
                localStorage.clear();
                window.location.href = '/login';
                return Promise.reject(refreshError);
            }
        }
        
        return Promise.reject(error);
    }
);
```

---

### 5️⃣ Logout

```
┌─────────┐                              ┌─────────┐
│ Cliente │                              │ Servidor│
└────┬────┘                              └────┬────┘
     │                                        │
     │  POST /api/auth/logout                 │
     │  { refreshToken: "eyJhbG..." }         │
     │───────────────────────────────────────>│
     │                                        │
     │                                        │ 1. (Opcional) Agregar refresh
     │                                        │    token a blacklist
     │                                        │ 2. Eliminar de BD si se guardó
     │                                        │
     │  { success: true }                     │
     │<───────────────────────────────────────│
     │                                        │
     │  Eliminar tokens del cliente:          │
     │  - localStorage.clear()                │
     │  - sessionStorage.clear()              │
     │  - Eliminar cookies                    │
     │                                        │
```

**Código (Logout Simple):**
```java
@PostMapping("/logout")
public ResponseEntity<?> logout(@RequestBody LogoutRequest request) {
    // Opción 1: No hacer nada en el servidor (más común con JWT)
    // El cliente simplemente elimina los tokens
    
    // Opción 2: Blacklist del refresh token (más seguro)
    // blacklistService.addToBlacklist(request.refreshToken());
    
    return ResponseEntity.ok(Map.of("success", true));
}
```

**Cliente:**
```javascript
async function logout() {
    try {
        const refreshToken = localStorage.getItem('refreshToken');
        
        await axios.post('/api/auth/logout', { refreshToken });
        
        // Eliminar tokens del almacenamiento
        localStorage.removeItem('accessToken');
        localStorage.removeItem('refreshToken');
        
        // Redirigir a login
        window.location.href = '/login';
    } catch (error) {
        console.error('Error en logout:', error);
    }
}
```

---

## Implementación en Spring Boot

### JwtService completo

```java
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token-expiration:900000}") // 15 min
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 días
    private long refreshTokenExpiration;

    // Generar Access Token
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "access");
        return generateToken(claims, username, accessTokenExpiration);
    }

    // Generar Refresh Token
    public String generateRefreshToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");
        return generateToken(claims, username, refreshTokenExpiration);
    }

    // Generar token genérico
    private String generateToken(Map<String, Object> claims, 
                                 String username, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Validar token
    public boolean isTokenValid(String token, String username) {
        String tokenUsername = extractUsername(token);
        return tokenUsername.equals(username) && !isTokenExpired(token);
    }

    // Extraer username
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extraer claims
    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSignInKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration)
                .before(new Date());
    }

    private Key getSignInKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
```

### application.properties

```properties
# JWT Configuration
jwt.secret=mi-clave-secreta-super-segura-con-al-menos-256-bits-de-longitud-para-hs256
jwt.access-token-expiration=900000
jwt.refresh-token-expiration=604800000
```

---

## Comparación: Sesiones vs JWT

### 🔄 Sistema de Sesiones (Tradicional)

| Aspecto | Descripción |
|---------|-------------|
| **Almacenamiento** | Servidor guarda estado en memoria/BD |
| **Cookie** | Solo contiene Session ID |
| **Escalabilidad** | Difícil (necesita sesiones compartidas) |
| **Memoria** | Consume memoria del servidor |
| **Invalidación** | Fácil (eliminar sesión del servidor) |
| **Seguridad** | Vulnerable a CSRF |

**Flujo:**
```
Cliente → Cookie(JSESSIONID=abc123) → Servidor
                                    ↓
                                Busca en memoria/BD
                                Session(abc123) → {user: "oscar"}
```

### 🎫 Sistema JWT (Moderno)

| Aspecto | Descripción |
|---------|-------------|
| **Almacenamiento** | Cliente guarda token (stateless) |
| **Token** | Contiene toda la información firmada |
| **Escalabilidad** | Excelente (no necesita estado compartido) |
| **Memoria** | No consume memoria del servidor |
| **Invalidación** | Difícil (necesita blacklist o corta duración) |
| **Seguridad** | Vulnerable a XSS (si se guarda en localStorage) |

**Flujo:**
```
Cliente → Header(Authorization: Bearer eyJhbG...)
                                              ↓
                                    Verifica firma y extrae datos
                                    {sub: "oscar", exp: 1234567890}
```

### ✅ Ventajas de JWT

1. **Stateless**: El servidor no necesita guardar estado
2. **Escalable**: Funciona bien en arquitecturas distribuidas
3. **Mobile-friendly**: Ideal para apps móviles
4. **Cross-domain**: Funciona entre diferentes dominios
5. **Performance**: No requiere consultas a BD para cada petición
6. **Microservicios**: Ideal para arquitecturas de microservicios

### ❌ Desventajas de JWT

1. **Tamaño**: Los tokens son más grandes que un Session ID
2. **Invalidación**: No se puede revocar fácilmente sin blacklist
3. **Seguridad**: Si se roba, es válido hasta que expire
4. **No se puede modificar**: Una vez emitido, no se puede cambiar

### 🔒 Mejores Prácticas de Seguridad

1. **Access Token corto**: 15-30 minutos
2. **Refresh Token largo**: 7-30 días
3. **HTTPS obligatorio**: Siempre usar HTTPS
4. **HttpOnly Cookies**: Para almacenar tokens (evita XSS)
5. **Firma fuerte**: Usar HS256 o RS256 con clave robusta
6. **Validar siempre**: Verificar firma y expiración
7. **Blacklist**: Para refresh tokens en logout
8. **Rotación**: Rotar refresh tokens regularmente

---

## 📊 Diagrama Completo del Flujo

```
┌──────────────────────────────────────────────────────────────────┐
│                    FLUJO COMPLETO JWT                            │
└──────────────────────────────────────────────────────────────────┘

1. REGISTRO
   Cliente ──[username, password]──> Servidor
   Servidor ──[hash password]──> BD ──[guardar]──> ✓

2. LOGIN
   Cliente ──[username, password]──> Servidor
   Servidor ──[verificar]──> BD
   Servidor ──[generar JWT]──> Cliente
   Cliente ──[guardar accessToken + refreshToken]──> localStorage

3. PETICIÓN AUTENTICADA
   Cliente ──[Header: Bearer token]──> Servidor
   Servidor ──[validar firma]──> ✓
   Servidor ──[verificar expiración]──> ✓
   Servidor ──[extraer username]──> Procesar petición
   Servidor ──[response]──> Cliente

4. TOKEN EXPIRADO
   Cliente ──[expired token]──> Servidor ──[401]──> Cliente
   Cliente ──[refreshToken]──> Servidor
   Servidor ──[validar refresh]──> ✓
   Servidor ──[nuevo accessToken]──> Cliente
   Cliente ──[reintentar con nuevo token]──> Servidor ──[200 OK]──> Cliente

5. LOGOUT
   Cliente ──[logout request]──> Servidor
   Servidor ──[blacklist refresh token]──> BD
   Cliente ──[eliminar tokens]──> localStorage.clear()
```

---

## 🎯 Resumen

**JWT es ideal cuando:**
- Necesitas escalabilidad horizontal
- Trabajas con microservicios
- Desarrollas APIs RESTful
- Tienes aplicaciones móviles
- Necesitas autenticación entre dominios

**Sesiones son mejores cuando:**
- Necesitas invalidación inmediata
- La seguridad es crítica (aplicaciones bancarias)
- Tienes una aplicación monolítica simple
- No necesitas escalar mucho

**La solución híbrida (JWT + Refresh Token) es lo más recomendado** porque combina lo mejor de ambos mundos: la eficiencia de JWT con la seguridad de poder invalidar sesiones.

