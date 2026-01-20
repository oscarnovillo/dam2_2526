# 🔐 Vault - Caja Fuerte con Cifrado Zero-Knowledge

## ¿Qué es esto?

Un sistema de **caja fuerte personal** donde cada usuario puede guardar secretos (mensajes, contraseñas, notas, etc.) que **solo él puede descifrar** con su password.

### 🎯 Características Principales

- ✅ **Zero-Knowledge**: El servidor NUNCA ve los datos en claro
- ✅ **Cifrado en Cliente**: AES-256-GCM con PBKDF2 (100,000 iteraciones)
- ✅ **Privacidad Total**: Ni administradores pueden acceder a tus datos
- ✅ **Web Crypto API**: Criptografía nativa del navegador (sin librerías externas)
- ✅ **RESTful API**: Backend Spring Boot con JPA/H2

## 🏗️ Arquitectura

```
┌──────────────────────────────────────┐
│  CLIENTE (Navegador)                 │
│  ┌────────────────────────────────┐  │
│  │ 1. Usuario ingresa password    │  │
│  │ 2. PBKDF2 → Deriva clave AES   │  │
│  │ 3. AES-GCM → Cifra los datos   │  │
│  │ 4. Envía datos CIFRADOS ──────────────┐
│  └────────────────────────────────┘  │   │
└──────────────────────────────────────┘   │
                                           │ HTTPS
┌──────────────────────────────────────┐   │
│  SERVIDOR (Spring Boot)              │ ◄─┘
│  ┌────────────────────────────────┐  │
│  │ ❌ NO descifra                 │  │
│  │ ✅ Solo almacena blob cifrado  │  │
│  │ ✅ CRUD sobre datos cifrados   │  │
│  └────────────────────────────────┘  │
│  ┌────────────────────────────────┐  │
│  │ H2 Database (en memoria)       │  │
│  │ ┌──────────────────────────┐   │  │
│  │ │ id | encrypted_data | iv │   │  │
│  │ └──────────────────────────┘   │  │
│  └────────────────────────────────┘  │
└──────────────────────────────────────┘
```

## 🚀 Inicio Rápido

### 1. Compilar y Ejecutar

```bash
# Compilar
mvn clean package

# Ejecutar
java -jar target/SpringCrypto-0.0.1-SNAPSHOT.jar
```

### 2. Abrir la Interfaz Web

```
http://localhost:8080/vault-demo.html
```

### 3. Probar

1. **Guardar un secreto**:
   - Password: `miPasswordSegura123`
   - Datos: `Este es mi secreto importante`
   - Título: `Mi Nota Personal`
   - Click en "Cifrar y Guardar"
   - **Guarda el ID que se muestra** (ej: 1)

2. **Recuperar el secreto**:
   - ID: `1` (el que guardaste)
   - Password: `miPasswordSegura123` (la misma)
   - Click en "Recuperar y Descifrar"
   - Verás el mensaje original

3. **Listar secretos**:
   - Click en "Cargar Lista"
   - Verás todos tus secretos (con IDs)

## 📡 API REST

### Endpoints

| Método | URL | Descripción |
|--------|-----|-------------|
| `POST` | `/api/vault/secrets` | Guardar secreto cifrado |
| `GET` | `/api/vault/secrets/{id}` | Obtener secreto cifrado |
| `GET` | `/api/vault/secrets` | Listar todos los secretos |
| `PUT` | `/api/vault/secrets/{id}` | Actualizar secreto |
| `DELETE` | `/api/vault/secrets/{id}` | Eliminar secreto |

### Ejemplo con IntelliJ HTTP Client

Ver archivo: `api-tests-vault.http`

```http
### Guardar secreto
POST http://localhost:8080/api/vault/secrets
Content-Type: application/json
X-User-Id: 1

{
  "encryptedData": "base64_encrypted_data",
  "iv": "base64_iv",
  "salt": "base64_salt",
  "metadata": "{\"title\": \"encrypted_title\"}"
}
```

## 🔒 Criptografía Utilizada

### Derivación de Clave (PBKDF2)

```javascript
// Password del usuario → Clave AES-256
key = PBKDF2(
  password,        // "miPasswordSegura123"
  salt,            // 16 bytes aleatorios
  100000,          // iteraciones (ajustar según CPU)
  256              // AES-256
)
```

**¿Por qué PBKDF2?**
- Hace que probar passwords sea computacionalmente caro
- Cada intento tarda ~100ms → fuerza bruta inviable
- Estándar recomendado (RFC 2898)

### Cifrado (AES-256-GCM)

```javascript
ciphertext = AES-256-GCM(
  plaintext,       // Datos a cifrar
  key,             // Derivada con PBKDF2
  iv               // 12 bytes aleatorios para GCM
)
```

**¿Por qué AES-GCM?**
- **Authenticated Encryption**: Detecta manipulación de datos
- **Estándar militar**: AES-256 (NSA Suite B)
- **Eficiente**: Implementación hardware en CPUs modernas
- **Seguro**: Tag de autenticación de 128 bits

### Parámetros Criptográficos

| Parámetro | Valor | Tamaño |
|-----------|-------|--------|
| **Algoritmo de cifrado** | AES-GCM | 256 bits |
| **KDF** | PBKDF2-HMAC-SHA256 | - |
| **Iteraciones PBKDF2** | 100,000 | - |
| **Salt** | Aleatorio (SecureRandom) | 16 bytes |
| **IV** | Aleatorio (SecureRandom) | 12 bytes |
| **Tag de autenticación** | GCM | 128 bits |

## 🛡️ Seguridad

### ✅ Protecciones Implementadas

1. **Zero-Knowledge**: Servidor nunca tiene acceso a la clave
2. **Salt único**: Previene rainbow tables
3. **IV único**: Cada cifrado tiene IV diferente (nunca reusar)
4. **PBKDF2**: Hace lenta la fuerza bruta (100k iteraciones)
5. **GCM Tag**: Detecta modificaciones del ciphertext
6. **HTTPS**: Datos cifrados en tránsito (doble capa)

### ⚠️ Limitaciones y Advertencias

1. **Password perdida = datos perdidos**: Sin recuperación posible
2. **Password débil = vulnerable**: Usa 12+ caracteres mezclados
3. **Dispositivo comprometido**: Keyloggers pueden capturar password
4. **No hay autenticación**: Versión demo usa `X-User-Id` mock
5. **H2 en memoria**: Datos se pierden al reiniciar (usar PostgreSQL en prod)

### 💡 Mejoras para Producción

```diff
+ Autenticación JWT real (no mock con X-User-Id)
+ Rate limiting (evitar fuerza bruta)
+ Base de datos persistente (PostgreSQL/MySQL)
+ Backup cifrado de la base de datos
+ Auditoría de accesos
+ 2FA para login
+ Recovery key (clave de 128 bits para imprimir)
+ Autenticación biométrica (móvil)
```

## 📊 Comparación con Alternativas

| Sistema | Cifrado | Zero-Knowledge | Open Source |
|---------|---------|----------------|-------------|
| **Este Vault** | ✅ AES-256-GCM | ✅ Sí | ✅ Sí |
| **Bitwarden** | ✅ AES-256-CBC | ✅ Sí | ✅ Sí |
| **1Password** | ✅ AES-256-GCM | ✅ Sí | ❌ No |
| **LastPass** | ✅ AES-256-CBC | ✅ Sí | ❌ No |
| **Google Drive** | ✅ AES-256 | ❌ No | ❌ No |

## 🧪 Pruebas

### Prueba Manual

1. **Test de cifrado correcto**:
   ```
   - Guardar: "Hola Mundo" con password "test123"
   - Recuperar con password "test123"
   - Debe mostrar: "Hola Mundo" ✓
   ```

2. **Test de password incorrecta**:
   ```
   - Guardar: "Secreto" con password "abc"
   - Intentar recuperar con password "xyz"
   - Debe dar error: "Password incorrecta" ✓
   ```

3. **Test de persistencia**:
   ```
   - Guardar secreto con ID=1
   - Cerrar navegador
   - Reabrir y recuperar ID=1
   - Debe funcionar (mientras servidor esté corriendo) ✓
   ```

### Verificar Seguridad

```bash
# 1. Ver datos en la base de datos H2
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:vaultdb
User: sa
Password: (dejar vacío)

# 2. Query para ver datos cifrados
SELECT * FROM vault_secrets;

# 3. Verificar que 'encrypted_data' es binario ilegible ✓
```

## 📚 Recursos Adicionales

- **Web Crypto API**: https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API
- **PBKDF2**: https://tools.ietf.org/html/rfc2898
- **AES-GCM**: https://nvlpubs.nist.gov/nistpubs/Legacy/SP/nistspecialpublication800-38d.pdf
- **OWASP Crypto**: https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html

## 🤝 Contribuir

Ideas para mejorar:

1. Implementar autenticación JWT real
2. Añadir soporte para compartir secretos entre usuarios
3. Implementar recovery key
4. Cliente móvil (Android/iOS)
5. Exportar/importar vault cifrado
6. Categorías y tags de secretos
7. Historial de versiones (con re-cifrado)

## 📝 Licencia

MIT - Úsalo como quieras, pero sin garantías.

---

**⚠️ ADVERTENCIA**: Este es un proyecto educativo. Para uso en producción, considera:
- Auditoría de seguridad profesional
- Pruebas de penetración
- Certificación (ej: SOC 2)
- Seguros de responsabilidad

