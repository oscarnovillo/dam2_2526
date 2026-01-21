# 🔐 SpringCrypto - Proyecto de Criptografía Aplicada

Aplicación Spring Boot que demuestra diversos conceptos de criptografía aplicada, incluyendo cifrado simétrico, asimétrico, firmas digitales y arquitecturas zero-knowledge.

## 🎯 Características

### ✅ Implementado

- **Cifrado Simétrico (AES)**
  - AES-128, AES-256
  - Modos: ECB, CBC, CTR, GCM
  - Derivación de claves con PBKDF2

- **Cifrado Asimétrico (RSA y EC)**
  - Generación de pares de claves RSA (2048, 4096 bits)
  - Generación de pares de claves EC (P-256, P-384, P-521)
  - Cifrado/descifrado RSA con OAEP y PKCS#1
  - ECDH para acuerdo de claves

- **Firmas Digitales**
  - RSA (SHA256withRSA)
  - ECDSA (SHA256withECDSA)
  - Verificación de firmas

- **Cifrado Híbrido**
  - RSA para cifrar clave simétrica
  - AES-GCM para cifrar datos
  - Combina velocidad y seguridad

- **Vault - Caja Fuerte Zero-Knowledge** ⭐ NUEVO
  - Cifrado en cliente con AES-256-GCM
  - PBKDF2 con 100,000 iteraciones
  - Arquitectura zero-knowledge
  - El servidor NUNCA ve los datos en claro
  - Cliente web funcional
  - Ejemplo de cliente Android (Kotlin)

## 🚀 Inicio Rápido

### 1. Clonar y Compilar

```bash
git clone <repo-url>
cd SpringCrypto
mvn clean package
```

### 2. Ejecutar

```bash
mvn spring-boot:run
```

O:

```bash
java -jar target/SpringCrypto-0.0.1-SNAPSHOT.jar
```

### 3. Probar

#### Opción A: Cliente Web del Vault (Recomendado)

```
http://localhost:8080/vault-demo.html
```

**Demo rápida:**
1. Password: `test123`
2. Datos: `Este es mi secreto`
3. Click "Cifrar y Guardar"
4. Anota el ID que aparece
5. Usa el mismo ID y password para recuperar

#### Opción B: API REST con HTTP Client

Abre en IntelliJ IDEA:
- `api-tests.http` - Cifrado simétrico/asimétrico
- `api-tests-vault.http` - Vault zero-knowledge

#### Opción C: Consola H2 (Ver datos cifrados)

```
http://localhost:8080/h2-console

JDBC URL: jdbc:h2:mem:vaultdb
User: sa
Password: (vacío)

Query: SELECT * FROM vault_secrets;
```

## 📚 Documentación

Consulta **[INDEX.md](INDEX.md)** para el índice completo de documentación.

### Documentos Principales

| Documento | Descripción |
|-----------|-------------|
| **[CRIPTOGRAFIA.md](CRIPTOGRAFIA.md)** | Teoría completa de criptografía |
| **[ECDH_AES_KEYS.md](ECDH_AES_KEYS.md)** | ECDH, TLS 1.3, certificados ECDSA vs RSA |
| **[SECURE_VAULT_ARCHITECTURE.md](SECURE_VAULT_ARCHITECTURE.md)** | 3 arquitecturas de caja fuerte |
| **[VAULT_README.md](VAULT_README.md)** | Guía completa del Vault |
| **[VaultCryptoManager.kt](VaultCryptoManager.kt)** | Cliente Android ejemplo |

## 🏗️ Arquitectura del Vault

```
┌──────────────────────────────────────┐
│  CLIENTE (Web/Móvil)                 │
│  ┌────────────────────────────────┐  │
│  │ 1. Usuario → Password          │  │
│  │ 2. PBKDF2 → Clave AES          │  │
│  │ 3. AES-GCM → Cifrado           │  │
│  │ 4. POST datos cifrados ─────────────┐
│  └────────────────────────────────┘  │ │
└──────────────────────────────────────┘ │
                                         │
┌─────────────────────────────────────┐ │
│  SERVIDOR (Spring Boot)             │◄┘
│  ┌───────────────────────────────┐  │
│  │ ❌ NO descifra                │  │
│  │ ✅ Solo almacena cifrado      │  │
│  └───────────────────────────────┘  │
│  Database: H2 (desarrollo)          │
└─────────────────────────────────────┘
```

**Ventajas:**
- ✅ Zero-Knowledge: Servidor no puede ver datos
- ✅ Seguridad máxima: Hackear servidor = datos siguen protegidos
- ✅ Privacidad total: Ni administradores pueden acceder

**Trade-off:**
- ❌ Password perdida = datos irrecuperables

## 📡 API REST

### Cifrado Simétrico

```http
POST /api/symmetric/encrypt
POST /api/symmetric/decrypt
```

### Cifrado Asimétrico

```http
GET  /api/asymmetric/keypair?algorithm=RSA&keySize=2048
POST /api/asymmetric/encrypt
POST /api/asymmetric/decrypt
POST /api/asymmetric/sign
POST /api/asymmetric/verify
```

### Vault (Caja Fuerte)

```http
POST   /api/vault/secrets          # Guardar secreto
GET    /api/vault/secrets/{id}     # Recuperar secreto
GET    /api/vault/secrets          # Listar
PUT    /api/vault/secrets/{id}     # Actualizar
DELETE /api/vault/secrets/{id}     # Eliminar
```

## 🔒 Criptografía Utilizada

| Componente | Algoritmo | Parámetros |
|------------|-----------|------------|
| **Cifrado simétrico** | AES-256-GCM | IV: 12 bytes, Tag: 128 bits |
| **Derivación de claves** | PBKDF2-HMAC-SHA256 | 100,000 iteraciones |
| **Cifrado asimétrico** | RSA-2048/4096, EC P-256 | OAEP padding |
| **Firmas** | SHA256withRSA, SHA256withECDSA | - |
| **Salts e IVs** | SecureRandom | Únicos por operación |

## 🛡️ Seguridad

### ✅ Implementado

- AES-GCM (authenticated encryption)
- PBKDF2 con 100k iteraciones
- Salts aleatorios únicos
- IVs únicos (nunca reutilizados)
- Zero-knowledge architecture (Vault)
- Perfect Forward Secrecy (documentado)

### ⚠️ Advertencias

- **Nunca usar ECB** para datos reales
- **Passwords fuertes**: Mínimo 12 caracteres
- **HTTPS en producción**: TLS 1.3 recomendado
- **No hardcodear claves** en el código
- **Vault**: Password perdida = datos perdidos

## 📱 Cliente Móvil

Ver **[VaultCryptoManager.kt](VaultCryptoManager.kt)** para ejemplo completo de cliente Android.

### Ejemplo Kotlin

```kotlin
val cryptoManager = VaultCryptoManager()

// Cifrar
val encrypted = cryptoManager.encrypt("Mi secreto", "password123")

// Enviar al servidor
vaultApi.saveSecret(SaveSecretRequest(
    encryptedData = encrypted.toBase64().ciphertext,
    iv = encrypted.toBase64().iv,
    salt = encrypted.toBase64().salt
))

// Recuperar y descifrar
val response = vaultApi.getSecret(secretId)
val decrypted = cryptoManager.decrypt(
    response.encryptedData,
    "password123",
    response.iv,
    response.salt
)
```

## 🧪 Pruebas

### Prueba Manual del Vault

```
1. Abrir: http://localhost:8080/vault-demo.html
2. Guardar secreto con password "test123"
3. Anotar el ID devuelto (ej: 1)
4. Recuperar con el mismo ID y password
5. Verificar que el mensaje es el original ✓
```

### Verificar Seguridad

```
1. Abrir H2 Console: http://localhost:8080/h2-console
2. Query: SELECT encrypted_data FROM vault_secrets WHERE id = 1;
3. Verificar que es binario ilegible (no texto plano) ✓
```

## 📊 Comparativa con Productos Reales

| Sistema | Este Vault | Bitwarden | 1Password | Google Drive |
|---------|-----------|-----------|-----------|--------------|
| **Cifrado** | AES-256-GCM | AES-256-CBC | AES-256-GCM | AES-256 |
| **Zero-Knowledge** | ✅ Sí | ✅ Sí | ✅ Sí | ❌ No |
| **Open Source** | ✅ Sí | ✅ Sí | ❌ No | ❌ No |
| **Educativo** | ✅ Sí | ❌ No | ❌ No | ❌ No |

## 🎓 Conceptos Aprendidos

1. **Zero-Knowledge Architecture**: El servidor no necesita descifrar
2. **Key Derivation (PBKDF2)**: Passwords → Claves criptográficas
3. **Authenticated Encryption (GCM)**: Confidencialidad + Integridad
4. **Client-Side Cryptography**: Web Crypto API, javax.crypto
5. **Perfect Forward Secrecy**: Claves efímeras (ECDHE)
6. **TLS 1.3**: Handshake moderno explicado
7. **Certificados**: RSA vs ECDSA en HTTPS real

## 📁 Estructura del Proyecto

```
SpringCrypto/
├── src/main/java/org/example/springcrypto/
│   ├── controller/         # REST controllers
│   ├── service/            # Lógica de negocio
│   ├── entity/             # Entidades JPA (Vault)
│   ├── repository/         # Repositorios JPA
│   └── dto/                # Request/Response DTOs
│
├── src/main/resources/
│   ├── static/
│   │   └── vault-demo.html     # Cliente web del Vault
│   └── application.properties
│
├── Documentación/
│   ├── CRIPTOGRAFIA.md          # Teoría completa
│   ├── ECDH_AES_KEYS.md         # ECDH, TLS, certificados
│   ├── SECURE_VAULT_ARCHITECTURE.md  # 3 arquitecturas
│   ├── VAULT_README.md          # Guía del Vault
│   ├── IMPLEMENTACION_VAULT.md  # Resumen implementación
│   └── INDEX.md                 # Índice completo
│
├── api-tests.http               # Pruebas de cifrado
├── api-tests-vault.http         # Pruebas del Vault
├── VaultCryptoManager.kt        # Cliente Android
└── README.md (este archivo)
```

## 🚀 Roadmap

### ✅ Completado
- [x] Cifrado simétrico (AES todos los modos)
- [x] Cifrado asimétrico (RSA, EC)
- [x] Firmas digitales (RSA, ECDSA)
- [x] Cifrado híbrido
- [x] Vault zero-knowledge
- [x] Cliente web funcional
- [x] Ejemplo cliente Android
- [x] Documentación completa

### 🔮 Futuro
- [ ] Autenticación JWT real
- [ ] Rate limiting
- [ ] Recovery keys
- [ ] Compartir secretos entre usuarios
- [ ] 2FA
- [ ] Base de datos PostgreSQL
- [ ] Cliente iOS (Swift)
- [ ] Docker Compose

## 📞 Ayuda

### ¿Por dónde empezar?

1. **Quiero aprender teoría**: Lee [CRIPTOGRAFIA.md](CRIPTOGRAFIA.md)
2. **Quiero probar rápido**: Abre http://localhost:8080/vault-demo.html
3. **Quiero entender arquitecturas**: Lee [SECURE_VAULT_ARCHITECTURE.md](SECURE_VAULT_ARCHITECTURE.md)
4. **Quiero ver código móvil**: Mira [VaultCryptoManager.kt](VaultCryptoManager.kt)
5. **Quiero usar la API**: Abre `api-tests-vault.http`

### FAQ

**¿Es seguro para producción?**
- El código criptográfico sí (usa librerías estándar)
- Falta autenticación real, rate limiting, etc.
- Es un proyecto **educativo**, no auditado profesionalmente

**¿Puedo recuperar datos si olvido la password?**
- No en la implementación actual (zero-knowledge)
- Puedes implementar recovery keys (ver documentación)

**¿Funciona en móvil?**
- Sí, mismo concepto
- Ver VaultCryptoManager.kt para Android
- Para iOS: usar CryptoKit con misma lógica

**¿Por qué no usar RSA para el Vault?**
- No es necesario (solo un usuario cifra/descifra)
- AES es más rápido
- RSA es útil para compartir entre usuarios (futuro)

## 📚 Referencias

- [NIST Cryptographic Standards](https://csrc.nist.gov/)
- [OWASP Crypto](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)
- [Web Crypto API](https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API)
- [RFC 5869 - HKDF](https://tools.ietf.org/html/rfc5869)
- [Signal Protocol](https://signal.org/docs/)

## 📝 Licencia

MIT - Úsalo como quieras para aprender.

---

## ⭐ Destacados

### 🔥 Vault - Caja Fuerte Zero-Knowledge

El módulo **Vault** es una implementación completa de un sistema de almacenamiento seguro donde:

- ✅ **Zero-Knowledge**: El servidor NUNCA ve tus datos
- ✅ **Web Crypto API**: Cifrado nativo del navegador
- ✅ **Cliente Android**: Ejemplo completo en Kotlin
- ✅ **Documentación**: 3 arquitecturas explicadas
- ✅ **Funcional**: Pruébalo ahora en http://localhost:8080/vault-demo.html

### 📖 Documentación Completa sobre ECDH y TLS

El archivo **[ECDH_AES_KEYS.md](ECDH_AES_KEYS.md)** explica:

- Cómo funciona ECDH
- TLS 1.3 handshake paso a paso
- Certificados ECDSA vs RSA en servidores reales (Google, Cloudflare)
- Perfect Forward Secrecy
- Claves estáticas vs efímeras

---

**¡Empieza ahora!** → http://localhost:8080/vault-demo.html

*Última actualización: 2026-01-21*

