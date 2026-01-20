# Arquitectura de Caja Fuerte por Usuario (Secure Vault)

## El Problema

Cada usuario quiere guardar datos (mensajes, archivos, objetos) en el servidor de forma que:
- ✅ Solo el usuario con su clave pueda descifrarlos
- ✅ Ni siquiera el administrador del servidor puede ver los datos
- ✅ Si el servidor es comprometido, los datos siguen seguros (cifrados)

## Opciones de Arquitectura

### Opción 1: Cifrado en Cliente (Zero-Knowledge) ⭐ RECOMENDADO

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENTE (App Móvil / Web)                                  │
├─────────────────────────────────────────────────────────────┤
│  1. Usuario introduce password                              │
│     ↓                                                        │
│  2. Derivar clave AES con PBKDF2                            │
│     key = PBKDF2(password, salt, 100000)                    │
│     ↓                                                        │
│  3. Cifrar datos LOCALMENTE                                 │
│     encrypted = AES-GCM(data, key)                          │
│     ↓                                                        │
│  4. Enviar solo datos cifrados al servidor                  │
│     POST /vault/save { encryptedData, iv, salt }            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  SERVIDOR (Spring Boot)                                     │
├─────────────────────────────────────────────────────────────┤
│  ❌ NO tiene acceso a la clave                              │
│  ❌ NO puede descifrar los datos                            │
│  ✅ Solo almacena datos cifrados                            │
│                                                              │
│  Database:                                                   │
│  ┌──────────────────────────────────────┐                  │
│  │ user_id | encrypted_data | iv | salt │                  │
│  │ 1       | 0x3F4A...      | ... | ...  │                  │
│  └──────────────────────────────────────┘                  │
└─────────────────────────────────────────────────────────────┘
```

**Ventajas:**
- ✅ **Zero-knowledge**: El servidor nunca ve los datos en claro
- ✅ **Máxima seguridad**: Aunque el servidor sea hackeado, datos están seguros
- ✅ **Privacidad total**: Ni administradores pueden ver los datos

**Desventajas:**
- ❌ Si el usuario olvida la password, **datos perdidos para siempre**
- ❌ No hay recuperación de cuenta posible
- ❌ Búsquedas en servidor imposibles (datos cifrados)

**Uso típico:** Signal, ProtonMail, Bitwarden

---

### Opción 2: Cifrado en Servidor con Clave del Usuario

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENTE                                                     │
├─────────────────────────────────────────────────────────────┤
│  1. Usuario introduce password                              │
│     ↓                                                        │
│  2. Enviar password al servidor (HTTPS)                     │
│     POST /vault/save { data, password }                     │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  SERVIDOR (Spring Boot)                                     │
├─────────────────────────────────────────────────────────────┤
│  1. Recibir password por HTTPS                              │
│     ↓                                                        │
│  2. Derivar clave AES                                       │
│     key = PBKDF2(password, salt, 100000)                    │
│     ↓                                                        │
│  3. Cifrar datos                                            │
│     encrypted = AES-GCM(data, key)                          │
│     ↓                                                        │
│  4. Guardar datos cifrados                                  │
│     ⚠️ NO guardar la password ni la clave                   │
└─────────────────────────────────────────────────────────────┘
```

**Ventajas:**
- ✅ Cliente simple (solo envía password)
- ✅ Seguridad en reposo (datos cifrados en DB)

**Desventajas:**
- ⚠️ Password viaja al servidor (aunque por HTTPS)
- ⚠️ Servidor temporalmente tiene acceso a los datos en claro
- ⚠️ Vulnerable a compromiso del servidor en tiempo real
- ⚠️ Logs del servidor podrían capturar passwords

**Uso típico:** Aplicaciones empresariales internas

---

### Opción 3: Híbrida con Master Key del Usuario

```
┌─────────────────────────────────────────────────────────────┐
│  CLIENTE - REGISTRO                                         │
├─────────────────────────────────────────────────────────────┤
│  1. Usuario crea cuenta con password                        │
│     ↓                                                        │
│  2. Generar Master Key aleatoria                            │
│     masterKey = SecureRandom(256 bits)                      │
│     ↓                                                        │
│  3. Cifrar Master Key con password                          │
│     wrappedKey = AES(masterKey, derivedKey(password))       │
│     ↓                                                        │
│  4. Enviar wrappedKey al servidor                           │
│     POST /register { username, wrappedKey }                 │
└─────────────────────────────────────────────────────────────┘
                              │
                              ↓
┌─────────────────────────────────────────────────────────────┐
│  CLIENTE - USO DIARIO                                       │
├─────────────────────────────────────────────────────────────┤
│  1. Login con password                                      │
│     ↓                                                        │
│  2. Obtener wrappedKey del servidor                         │
│     GET /user/wrappedKey                                    │
│     ↓                                                        │
│  3. Descifrar Master Key LOCALMENTE                         │
│     masterKey = AES.decrypt(wrappedKey, derivedKey(password))│
│     ↓                                                        │
│  4. Usar masterKey para cifrar/descifrar datos              │
│     encrypted = AES-GCM(data, masterKey)                    │
│     POST /vault/save { encryptedData }                      │
└─────────────────────────────────────────────────────────────┘
```

**Ventajas:**
- ✅ Zero-knowledge (servidor no tiene la Master Key)
- ✅ Cambiar password sin re-cifrar todos los datos
- ✅ Posibilidad de compartir con wrapping adicional

**Desventajas:**
- ❌ Complejidad mayor
- ❌ Si pierdes password, datos perdidos

**Uso típico:** 1Password, LastPass

---

## Implementación Recomendada: Opción 1 + Backend Spring Boot

### Arquitectura de la Solución

```
┌──────────────────────────────────────────────────────────────┐
│  APP MÓVIL (Android/iOS) o WEB (JavaScript)                  │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  CryptoManager (Cliente)                                     │
│  ├─ deriveKey(password) → AES Key                           │
│  ├─ encrypt(data, key) → { ciphertext, iv, salt }           │
│  └─ decrypt(ciphertext, key, iv) → data                     │
│                                                               │
│  VaultService (Cliente)                                      │
│  ├─ saveSecret(data, password)                              │
│  │   └─ HTTP POST /api/vault/secrets                        │
│  └─ getSecret(id, password)                                 │
│      └─ HTTP GET /api/vault/secrets/{id}                    │
│                                                               │
└──────────────────────────────────────────────────────────────┘
                              │ HTTPS (TLS 1.3)
                              ↓
┌──────────────────────────────────────────────────────────────┐
│  SPRING BOOT SERVER                                          │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  @RestController VaultController                             │
│  ├─ POST /api/vault/secrets                                 │
│  │   → Guarda { userId, encryptedData, iv, salt, createdAt }│
│  ├─ GET /api/vault/secrets/{id}                             │
│  │   → Devuelve { encryptedData, iv, salt }                 │
│  └─ DELETE /api/vault/secrets/{id}                          │
│                                                               │
│  VaultService (Servidor)                                     │
│  ├─ ❌ NO descifra nada                                      │
│  └─ ✅ Solo CRUD de datos cifrados                           │
│                                                               │
│  Database (PostgreSQL/MySQL)                                 │
│  ┌────────────────────────────────────────────────┐         │
│  │ secrets                                        │         │
│  ├────────────────────────────────────────────────┤         │
│  │ id BIGINT PK                                   │         │
│  │ user_id BIGINT FK → users(id)                 │         │
│  │ encrypted_data BYTEA (datos cifrados)         │         │
│  │ iv BYTEA (12 bytes)                            │         │
│  │ salt BYTEA (16 bytes)                          │         │
│  │ metadata TEXT (título cifrado, tags...)        │         │
│  │ created_at TIMESTAMP                           │         │
│  │ updated_at TIMESTAMP                           │         │
│  └────────────────────────────────────────────────┘         │
└──────────────────────────────────────────────────────────────┘
```

---

## Derivación de Claves: PBKDF2

**¿Por qué PBKDF2?**
- Password del usuario suele ser débil ("password123")
- PBKDF2 hace computacionalmente caro probar passwords (fuerza bruta)
- Genera una clave AES-256 robusta

```java
// Derivar clave AES desde password
public static SecretKey deriveKey(String password, byte[] salt) throws Exception {
    SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
    KeySpec spec = new PBEKeySpec(
        password.toCharArray(),
        salt,
        100000,  // 100k iteraciones (ajustar según capacidad del dispositivo)
        256      // AES-256
    );
    SecretKey tmp = factory.generateSecret(spec);
    return new SecretKeySpec(tmp.getEncoded(), "AES");
}
```

**Parámetros importantes:**
- **Salt**: 16 bytes aleatorios (único por usuario o por secreto)
- **Iteraciones**: 100,000 (2024) - aumentar cada año
- **Output**: 256 bits (AES-256)

---

## Flujo Completo de la Aplicación

### 1. Registro de Usuario

```
Cliente:
1. Usuario ingresa: username, email, password
2. Generar salt aleatorio (16 bytes)
3. Derivar authKey = PBKDF2(password, salt_auth, 100k)
4. Hash de auth: authHash = SHA-256(authKey)
5. POST /api/auth/register { username, email, authHash, salt_auth }

Servidor:
6. Guardar: user { username, email, authHash, salt_auth }
   ⚠️ NO guardar password en claro
```

### 2. Login

```
Cliente:
1. Usuario ingresa: username, password
2. GET /api/auth/salt?username=user → { salt_auth }
3. Derivar authKey = PBKDF2(password, salt_auth, 100k)
4. authHash = SHA-256(authKey)
5. POST /api/auth/login { username, authHash }

Servidor:
6. Verificar authHash con el guardado
7. Si coincide → JWT token
8. Devolver { token, userId }

Cliente:
9. Guardar password en memoria (para cifrado)
   ⚠️ NO guardar en disco/localStorage
```

### 3. Guardar Secreto

```
Cliente:
1. Usuario escribe: "Mi secreto importante"
2. Generar salt_secret aleatorio (16 bytes)
3. Derivar encKey = PBKDF2(password, salt_secret, 100k)
4. Generar IV aleatorio (12 bytes para GCM)
5. Cifrar: encrypted = AES-256-GCM(data, encKey, IV)
6. POST /api/vault/secrets
   Headers: { Authorization: "Bearer JWT" }
   Body: {
     encryptedData: base64(encrypted),
     iv: base64(IV),
     salt: base64(salt_secret),
     metadata: { title: "Mi Nota" } // Opcional: también cifrado
   }

Servidor:
7. Validar JWT
8. Guardar en DB: { user_id, encrypted_data, iv, salt }
9. Devolver: { secretId: 123 }
```

### 4. Recuperar Secreto

```
Cliente:
1. GET /api/vault/secrets/123
   Headers: { Authorization: "Bearer JWT" }

Servidor:
2. Verificar JWT y que secret.user_id == JWT.userId
3. Devolver: { encryptedData, iv, salt }

Cliente:
4. Derivar encKey = PBKDF2(password, salt, 100k)
5. Descifrar: data = AES-256-GCM.decrypt(encryptedData, encKey, iv)
6. Mostrar al usuario
```

---

## Consideraciones de Seguridad

### ✅ Buenas Prácticas

1. **Salt único por secreto** (o al menos por usuario)
   - Evita rainbow tables
   - Permite diferentes iteraciones PBKDF2

2. **HTTPS obligatorio (TLS 1.3)**
   - Aunque datos van cifrados, protege metadatos

3. **No guardar password en el cliente**
   - Solo en memoria RAM durante la sesión
   - Limpiar al cerrar app

4. **Rate limiting en servidor**
   - Evitar fuerza bruta en login
   - Limitar intentos por IP

5. **Auditoría**
   - Log de accesos (sin datos sensibles)
   - Alertas de accesos sospechosos

### ⚠️ Riesgos

1. **Olvido de password = datos perdidos**
   - Solución: Opción de "recovery key" (clave de 128 bits para imprimir)

2. **Keylogger en dispositivo del usuario**
   - No hay defensa si el dispositivo está comprometido
   - Usar teclados seguros, autenticación biométrica

3. **Shoulder surfing**
   - Ocultar password al escribir
   - No mostrar datos en notificaciones

4. **Captura de pantalla**
   - Deshabilitar screenshots en secciones sensibles (Android FLAG_SECURE)

---

## Mejoras Opcionales

### 1. Autenticación Biométrica (Móvil)

```java
// Android: Usar Keystore para proteger la key derivada
BiometricPrompt.authenticate() → 
  KeyStore.getKey("user_vault_key") →
    Descifrar datos
```

**Ventaja**: Usuario no escribe password cada vez

**Implementación**:
- Primera vez: Derivar key con password → Guardar en Android Keystore (protegido por huella)
- Usos posteriores: Huella → Keystore devuelve key → Descifrar

### 2. Compartir Secretos con Otros Usuarios

```
Usuario A quiere compartir con Usuario B:

1. A recupera el secreto (descifra con su password)
2. A obtiene la clave pública de B del servidor
3. A cifra el secreto con RSA/EC usando publicKey_B
4. A envía el secreto cifrado a B
5. B descifra con su privateKey
```

**Requiere**: Infraestructura PKI adicional

### 3. Recuperación de Cuenta

**Opción A: Recovery Key** (recomendado)
```
Registro:
1. Generar recovery key aleatoria (128 bits)
2. Mostrar al usuario: "GUARDA ESTO: XXXX-XXXX-XXXX-XXXX"
3. Cifrar master key con recovery key
4. Guardar encrypted_master_key en servidor

Recuperación:
1. Usuario ingresa recovery key
2. Descifra master key
3. Genera nueva password y re-cifra master key
```

**Opción B: Email de emergencia**
```
⚠️ Menos seguro: Admin puede resetear → Pierde zero-knowledge
```

---

## Comparación: ¿Dónde Cifrar?

| Aspecto | Cifrado en Cliente | Cifrado en Servidor |
|---------|-------------------|---------------------|
| **Seguridad** | ⭐⭐⭐⭐⭐ Zero-knowledge | ⭐⭐⭐ Server-side |
| **Privacidad** | ✅ Total | ⚠️ Servidor ve datos |
| **Recuperación** | ❌ Imposible sin recovery key | ✅ Admin puede resetear |
| **Rendimiento** | Cliente hace trabajo | Servidor hace trabajo |
| **Complejidad** | Alta (crypto en cliente) | Media (solo backend) |
| **Búsqueda** | ❌ Imposible | ✅ Posible |
| **Uso típico** | Apps de privacidad | Apps empresariales |

---

## Recomendación Final

### Para tu Proyecto de Caja Fuerte:

**🎯 Usa Cifrado en Cliente (Opción 1)**

**Razones:**
1. Es el objetivo educativo más valioso (aprender crypto real)
2. Demuestra comprensión de seguridad end-to-end
3. Es la arquitectura usada por apps reales (Signal, ProtonMail)
4. No requieres funcionalidad de búsqueda avanzada
5. Privacidad máxima (buen selling point)

**Implementación:**
- **Móvil**: Kotlin/Swift con librería crypto nativa
- **Web**: JavaScript con Web Crypto API
- **Backend**: Spring Boot (solo almacenamiento)

**Librerías recomendadas:**

```java
// Cliente Android (Kotlin)
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import java.security.SecureRandom

// Cliente Web (JavaScript)
crypto.subtle.importKey(...)
crypto.subtle.encrypt({ name: "AES-GCM", ... }, ...)
crypto.subtle.deriveBits({ name: "PBKDF2", ... }, ...)
```

---

## Ejemplo de API REST Completa

```java
@RestController
@RequestMapping("/api/vault")
public class VaultController {
    
    @PostMapping("/secrets")
    public SecretResponse saveSecret(
        @RequestHeader("Authorization") String token,
        @RequestBody SaveSecretRequest request
    ) {
        // 1. Validar JWT y obtener userId
        Long userId = jwtService.getUserIdFromToken(token);
        
        // 2. Crear entidad (datos YA VIENEN CIFRADOS)
        Secret secret = new Secret();
        secret.setUserId(userId);
        secret.setEncryptedData(request.getEncryptedData());
        secret.setIv(request.getIv());
        secret.setSalt(request.getSalt());
        secret.setMetadata(request.getMetadata()); // Título, tags (cifrados también)
        
        // 3. Guardar
        secret = secretRepository.save(secret);
        
        return new SecretResponse(secret.getId(), secret.getCreatedAt());
    }
    
    @GetMapping("/secrets/{id}")
    public SecretDetailResponse getSecret(
        @RequestHeader("Authorization") String token,
        @PathVariable Long id
    ) {
        Long userId = jwtService.getUserIdFromToken(token);
        
        Secret secret = secretRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Secret not found"));
        
        // Verificar ownership
        if (!secret.getUserId().equals(userId)) {
            throw new ForbiddenException("Not your secret");
        }
        
        // Devolver datos cifrados (cliente descifrará)
        return new SecretDetailResponse(
            secret.getEncryptedData(),
            secret.getIv(),
            secret.getSalt(),
            secret.getMetadata()
        );
    }
    
    @GetMapping("/secrets")
    public List<SecretListItem> listSecrets(
        @RequestHeader("Authorization") String token
    ) {
        Long userId = jwtService.getUserIdFromToken(token);
        return secretRepository.findByUserId(userId)
            .stream()
            .map(s -> new SecretListItem(
                s.getId(),
                s.getMetadata(), // Título cifrado
                s.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }
}
```

**Nota importante**: El servidor **nunca** descifra los datos. Solo los almacena y devuelve.

---

## Siguiente Paso

¿Quieres que implemente el código completo?

1. **Entidades JPA** (Secret, User)
2. **DTOs** (Request/Response)
3. **Controllers REST**
4. **Cliente de ejemplo en JavaScript** (Web Crypto API)
5. **Tests con HTTP Client** (IntelliJ)

