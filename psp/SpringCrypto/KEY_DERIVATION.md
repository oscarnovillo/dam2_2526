# 🔑 Generación de Claves desde Configuración

## Introducción

SpringCrypto ahora soporta la generación de claves AES desde un string configurado en `application.properties` usando **PBKDF2** (Password-Based Key Derivation Function 2).

## ¿Por qué usar PBKDF2?

PBKDF2 es un estándar (RFC 2898) que deriva claves criptográficas seguras desde passwords o strings arbitrarios mediante:

- **Múltiples iteraciones** (65,536 en este proyecto)
- **Salt único** (previene ataques con tablas precalculadas)
- **Función hash** (HMAC-SHA256)

### Ventaja Principal

✅ **La misma contraseña/string siempre genera la misma clave**

Esto permite:
- Configurar una clave fija en producción
- Compartir la clave de forma segura (compartiendo el password, no la clave directamente)
- Recuperar la clave si se pierde (regenerándola desde el password)

## Configuración en application.properties

### Habilitar Clave Fija

Edita `src/main/resources/application.properties`:

```properties
# Descomentar para usar una clave fija derivada del string
crypto.aes.secret-key=MiClaveSecretaSuperSegura2026!
```

### Variables de Entorno (Recomendado para Producción)

```bash
# Linux/Mac
export CRYPTO_AES_SECRET_KEY="MiClaveSecretaSuperSegura2026!"

# Windows PowerShell
$env:CRYPTO_AES_SECRET_KEY="MiClaveSecretaSuperSegura2026!"

# Docker
docker run -e CRYPTO_AES_SECRET_KEY="MiClaveSecretaSuperSegura2026!" ...
```

En `application.properties`:
```properties
crypto.aes.secret-key=${CRYPTO_AES_SECRET_KEY}
```

## Métodos Disponibles

### 1. Generar Clave desde Password

```java
SecretKey key = symmetricService.generateKeyFromPassword("MiPassword123");
```

**Características:**
- Usa salt fijo interno: `"SpringCrypto2026"`
- 65,536 iteraciones PBKDF2
- Genera clave AES-256

### 2. Generar Clave con Salt Personalizado

```java
byte[] salt = "MiSaltUnico12345".getBytes(StandardCharsets.UTF_8);
SecretKey key = symmetricService.generateKeyFromPassword("MiPassword123", salt);
```

**Características:**
- Usa salt proporcionado
- Mismo número de iteraciones
- Mayor seguridad (salt único por aplicación)

### 3. Obtener Clave Configurada

```java
SecretKey key = symmetricService.getConfiguredKey();
```

**Comportamiento:**
- Si `crypto.aes.secret-key` está configurada → Deriva clave desde ese string
- Si NO está configurada → Genera clave aleatoria nueva

## Endpoints REST

### Generar Clave desde Password

**Request:**
```http
POST http://localhost:8080/api/symmetric/generate-key-from-password
Content-Type: application/json

{
  "password": "MiPasswordSuperSeguro123!"
}
```

**Response:**
```json
{
  "key": "jKl8mN9oP0qR1sT2uV3wX4yZ5aB6cD7eF8gH9iJ0kL1m=",
  "algorithm": "AES",
  "keySize": "256",
  "type": "derived",
  "derivationMethod": "PBKDF2WithHmacSHA256",
  "iterations": "65536",
  "info": "La misma contraseña siempre genera la misma clave"
}
```

### Obtener Clave Configurada

**Request:**
```http
GET http://localhost:8080/api/symmetric/configured-key
```

**Response:**
```json
{
  "key": "Base64EncodedKey...",
  "algorithm": "AES",
  "keySize": "256",
  "type": "configured",
  "info": "Clave derivada desde application.properties (crypto.aes.secret-key)"
}
```

## Ejemplo Completo de Uso

### Paso 1: Generar Clave desde Password

```http
POST http://localhost:8080/api/symmetric/generate-key-from-password
Content-Type: application/json

{
  "password": "MiClaveSegura2026"
}
```

Guarda la clave devuelta: `jKl8mN9oP0qR1sT2uV3wX4yZ5aB6cD7eF8gH9iJ0kL1m=`

### Paso 2: Encriptar con esa Clave

```http
POST http://localhost:8080/api/symmetric/encrypt
Content-Type: application/json

{
  "plainText": "Datos confidenciales",
  "mode": "GCM",
  "key": "jKl8mN9oP0qR1sT2uV3wX4yZ5aB6cD7eF8gH9iJ0kL1m="
}
```

### Paso 3: Desencriptar en Otro Momento/Lugar

Si perdiste la clave, puedes regenerarla:

```http
POST http://localhost:8080/api/symmetric/generate-key-from-password
Content-Type: application/json

{
  "password": "MiClaveSegura2026"
}
```

Obtendrás la **MISMA clave**: `jKl8mN9oP0qR1sT2uV3wX4yZ5aB6cD7eF8gH9iJ0kL1m=`

Ahora puedes desencriptar:

```http
POST http://localhost:8080/api/symmetric/decrypt
Content-Type: application/json

{
  "encryptedText": "...",
  "key": "jKl8mN9oP0qR1sT2uV3wX4yZ5aB6cD7eF8gH9iJ0kL1m=",
  "mode": "GCM"
}
```

## Detalles Técnicos

### Algoritmo PBKDF2

```
PBKDF2(
  PRF = HMAC-SHA256,
  Password = "MiPassword",
  Salt = "SpringCrypto2026",
  Iterations = 65536,
  KeyLength = 256 bits
) → SecretKey AES-256
```

### Salt Fijo vs Salt Único

#### Salt Fijo (usado por defecto)
```java
private static final byte[] PBKDF2_SALT = "SpringCrypto2026".getBytes();
```

**Ventaja:** Misma clave siempre  
**Desventaja:** Menos seguro si alguien conoce el salt

#### Salt Único (método alternativo)
```java
byte[] uniqueSalt = new SecureRandom().nextBytes(new byte[16]);
SecretKey key = generateKeyFromPassword("password", uniqueSalt);
```

**Ventaja:** Más seguro  
**Desventaja:** Debes almacenar el salt junto con los datos cifrados

## Mejores Prácticas

### ✅ Hacer

1. **Usar passwords fuertes:**
   ```
   ✅ MiClaveSegura2026!@#$%
   ❌ 123456
   ```

2. **Almacenar passwords en variables de entorno:**
   ```properties
   crypto.aes.secret-key=${CRYPTO_SECRET}
   ```

3. **Nunca versionar passwords en Git:**
   ```gitignore
   # .gitignore
   application-prod.properties
   .env
   ```

4. **Usar gestores de secretos en producción:**
   - AWS Secrets Manager
   - Azure Key Vault
   - HashiCorp Vault
   - Spring Cloud Config

### ❌ Evitar

1. ❌ Hardcodear passwords en código
2. ❌ Usar passwords débiles o predecibles
3. ❌ Compartir passwords por canales inseguros
4. ❌ Reutilizar el mismo password en múltiples sistemas

## Comparación: Random vs Derivada

| Característica | Clave Aleatoria | Clave Derivada (PBKDF2) |
|----------------|-----------------|-------------------------|
| **Generación** | `generateKey()` | `generateKeyFromPassword("pwd")` |
| **Reproducible** | ❌ No | ✅ Sí (mismo password = misma clave) |
| **Seguridad** | ⭐⭐⭐⭐⭐ Máxima | ⭐⭐⭐⭐ Alta (depende del password) |
| **Uso típico** | Sesiones temporales | Claves persistentes |
| **Almacenamiento** | Debe guardarse | Se regenera desde password |
| **Compartir** | Enviar clave completa | Solo enviar password |

## Casos de Uso

### Caso 1: Aplicación con Múltiples Instancias

**Problema:** Varias instancias de la app necesitan usar la misma clave.

**Solución:**
```properties
# Todas las instancias usan la misma configuración
crypto.aes.secret-key=ClaveCompartidaEntre Instancias2026
```

### Caso 2: Cifrado de Base de Datos

**Problema:** Necesitas cifrar campos sensibles en la BD.

**Solución:**
```java
@Service
public class UserService {
    @Autowired
    private SymmetricEncryptionService crypto;
    
    public void saveUser(User user) {
        // Usa clave configurada (siempre la misma)
        SecretKey key = crypto.getConfiguredKey();
        String encryptedEmail = crypto.encryptGCM(user.getEmail(), key, ...);
        // Guardar en BD
    }
}
```

### Caso 3: Recuperación de Datos

**Problema:** Perdiste la clave pero tienes el password.

**Solución:**
```java
// Regenerar la misma clave desde el password
SecretKey recoveredKey = crypto.generateKeyFromPassword("MiPasswordOriginal");
String decryptedData = crypto.decryptGCM(encrypted, recoveredKey);
```

## Seguridad

### Fortaleza del Password

La seguridad de la clave derivada depende directamente del password:

| Password | Bits Entropía | Seguridad | Tiempo Fuerza Bruta |
|----------|---------------|-----------|---------------------|
| `123456` | ~20 bits | ⚠️ Muy débil | Segundos |
| `Password1` | ~30 bits | ⚠️ Débil | Minutos |
| `MiClave2026` | ~50 bits | ⚡ Media | Días |
| `MiCl@ve$egura!2026` | ~70 bits | ✅ Buena | Años |
| `Correct Horse Battery Staple` | ~80 bits | ⭐ Excelente | Siglos |

### Iteraciones PBKDF2

Las 65,536 iteraciones hacen que cada intento de fuerza bruta sea ~65,536 veces más lento.

**Sin PBKDF2:** 1 billón de intentos/segundo  
**Con PBKDF2:** ~15 millones de intentos/segundo

## Pruebas en api-tests.http

El archivo incluye pruebas completas:

- **Test 1b:** Generar clave desde password
- **Test 1c:** Obtener clave configurada
- **Test 1d:** Verificar que mismo password = misma clave
- **Test 1e:** Verificar que password diferente = clave diferente
- **Test 9b:** Encriptar con clave derivada
- **Test 9c:** Desencriptar con clave derivada

## Referencias

- [RFC 2898 - PKCS #5: PBKDF2](https://tools.ietf.org/html/rfc2898)
- [NIST SP 800-132 - Password-Based Key Derivation](https://csrc.nist.gov/publications/detail/sp/800-132/final)
- [OWASP Password Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Password_Storage_Cheat_Sheet.html)

---

**Creado para**: SpringCrypto - Proyecto PSP DAM2  
**Versión**: 1.0.0  
**Fecha**: 2026-01-20

