# 📚 SpringCrypto - Índice de Documentación

## 🎯 Proyecto

Aplicación Spring Boot que demuestra diversos conceptos de criptografía aplicada, incluyendo cifrado simétrico, asimétrico, firmas digitales y arquitecturas zero-knowledge.

---

## 📖 Documentación Principal

### 🔐 Criptografía Básica

1. **[CRIPTOGRAFIA.md](CRIPTOGRAFIA.md)**
   - Teoría de encriptación
   - Algoritmos simétricos y asimétricos
   - Modos de operación (ECB, CBC, CTR, GCM)
   - Funciones hash
   - Firmas digitales

### 🔑 Derivación de Claves

2. **[KEY_DERIVATION.md](KEY_DERIVATION.md)**
   - ¿Qué es KDF?
   - PBKDF2 (Password-Based)
   - HKDF (HMAC-based)
   - Scrypt y Argon2
   - Casos de uso

### 🔒 RSA y Padding

3. **[RSA_PADDING.md](RSA_PADDING.md)**
   - PKCS#1 v1.5
   - OAEP (Optimal Asymmetric Encryption Padding)
   - PSS (Probabilistic Signature Scheme)
   - Comparativas y recomendaciones

4. **[RSA_ECB_CLARIFICATION.md](RSA_ECB_CLARIFICATION.md)**
   - ¿Por qué RSA/ECB no es ECB real?
   - Cifrado por bloques vs operación única
   - Limitaciones de tamaño en RSA

### 📈 Curvas Elípticas

5. **[ELLIPTIC_CURVES.md](ELLIPTIC_CURVES.md)**
   - Matemática de curvas elípticas
   - ECDSA (firmas)
   - ECDH (acuerdo de claves)
   - Curvas recomendadas (P-256, P-384, Curve25519)

6. **[ECDH_AES_KEYS.md](ECDH_AES_KEYS.md)**
   - ¿Qué es ECDH?
   - Derivación de claves AES desde ECDH
   - TLS 1.3 handshake explicado
   - Certificados RSA vs ECDSA en HTTPS
   - Claves estáticas vs efímeras
   - Perfect Forward Secrecy

---

## 🏗️ Arquitecturas y Casos de Uso

### 🔐 Vault - Caja Fuerte Zero-Knowledge

7. **[SECURE_VAULT_ARCHITECTURE.md](SECURE_VAULT_ARCHITECTURE.md)** ⭐
   - **3 opciones de arquitectura**:
     - Opción 1: Cifrado en Cliente (Zero-Knowledge) ✅ Implementada
     - Opción 2: Cifrado en Servidor
     - Opción 3: Híbrida con Master Key
   - Comparativa detallada
   - Flujos completos (registro, login, guardar, recuperar)
   - Mejoras opcionales (biometría, compartir secretos, recovery key)

8. **[VAULT_README.md](VAULT_README.md)**
   - Guía de inicio rápido
   - API REST completa
   - Parámetros criptográficos
   - Seguridad y limitaciones
   - Pruebas y verificación

9. **[IMPLEMENTACION_VAULT.md](IMPLEMENTACION_VAULT.md)**
   - Resumen de implementación
   - Archivos creados
   - Cómo probar
   - Código para móvil (Android/iOS)

---

## 🚀 Inicio Rápido

### 1. Ejecutar el Servidor

```bash
mvn spring-boot:run
```

### 2. Probar Vault (Caja Fuerte)

```
http://localhost:8080/vault-demo.html
```

### 3. API REST (HTTP Client)

Abrir en IntelliJ IDEA:
- `api-tests.http` - Cifrado simétrico/asimétrico
- `api-tests-vault.http` - Caja fuerte zero-knowledge

---

## 📁 Estructura del Proyecto

```
SpringCrypto/
├── src/main/java/org/example/springcrypto/
│   ├── controller/
│   │   ├── SymmetricEncryptionController.java
│   │   ├── AsymmetricEncryptionController.java
│   │   └── VaultController.java ⭐
│   ├── service/
│   │   ├── SymmetricEncryptionService.java
│   │   ├── AsymmetricEncryptionService.java
│   │   └── VaultService.java ⭐
│   ├── entity/
│   │   └── VaultSecret.java ⭐
│   ├── repository/
│   │   └── VaultSecretRepository.java ⭐
│   └── dto/
│       ├── (DTOs de cifrado simétrico)
│       ├── (DTOs de cifrado asimétrico)
│       └── (DTOs de Vault) ⭐
│
├── src/main/resources/
│   ├── application.properties
│   ├── static/
│   │   └── vault-demo.html ⭐
│   └── banner.txt
│
├── Documentación/
│   ├── CRIPTOGRAFIA.md
│   ├── KEY_DERIVATION.md
│   ├── RSA_PADDING.md
│   ├── RSA_ECB_CLARIFICATION.md
│   ├── ELLIPTIC_CURVES.md
│   ├── ECDH_AES_KEYS.md
│   ├── SECURE_VAULT_ARCHITECTURE.md ⭐
│   ├── VAULT_README.md ⭐
│   ├── IMPLEMENTACION_VAULT.md ⭐
│   └── INDEX.md (este archivo)
│
├── api-tests.http
├── api-tests-vault.http ⭐
├── QUICKSTART.md
└── pom.xml
```

⭐ = Nuevo en la implementación de Vault

---

## 🎓 Conceptos por Documento

### Cifrado Simétrico
- **CRIPTOGRAFIA.md**: AES, ChaCha20, modos (ECB, CBC, GCM, CTR)
- **KEY_DERIVATION.md**: PBKDF2, HKDF
- **VAULT**: Implementación práctica con AES-256-GCM

### Cifrado Asimétrico
- **CRIPTOGRAFIA.md**: RSA, ECC conceptos básicos
- **RSA_PADDING.md**: PKCS#1, OAEP, PSS
- **ELLIPTIC_CURVES.md**: ECDSA, ECDH
- **ECDH_AES_KEYS.md**: Acuerdo de claves, TLS 1.3

### Arquitecturas
- **SECURE_VAULT_ARCHITECTURE.md**: 3 arquitecturas de caja fuerte
- **ECDH_AES_KEYS.md**: TLS 1.3, Perfect Forward Secrecy
- **VAULT**: Implementación completa zero-knowledge

---

## 🔧 APIs Disponibles

### 1. Cifrado Simétrico
```
POST /api/symmetric/encrypt
POST /api/symmetric/decrypt
```

### 2. Cifrado Asimétrico
```
GET  /api/asymmetric/keypair
POST /api/asymmetric/encrypt
POST /api/asymmetric/decrypt
POST /api/asymmetric/sign
POST /api/asymmetric/verify
```

### 3. Cifrado Híbrido
```
POST /api/asymmetric/hybrid/encrypt
POST /api/asymmetric/hybrid/decrypt
```

### 4. Vault (Caja Fuerte) ⭐
```
POST   /api/vault/secrets          # Guardar secreto cifrado
GET    /api/vault/secrets/{id}     # Recuperar secreto
GET    /api/vault/secrets          # Listar secretos
PUT    /api/vault/secrets/{id}     # Actualizar secreto
DELETE /api/vault/secrets/{id}     # Eliminar secreto
```

---

## 📊 Comparativas

### Algoritmos de Cifrado Simétrico

| Algoritmo | Tamaño de Clave | Velocidad | Seguridad | Uso |
|-----------|----------------|-----------|-----------|-----|
| AES-128 | 128 bits | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | General |
| AES-256 | 256 bits | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Alto secreto |
| ChaCha20 | 256 bits | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Móviles |

### Algoritmos de Cifrado Asimétrico

| Algoritmo | Tamaño de Clave | Velocidad | Seguridad | Uso |
|-----------|----------------|-----------|-----------|-----|
| RSA-2048 | 2048 bits | ⭐⭐ | ⭐⭐⭐⭐ | Legacy |
| RSA-4096 | 4096 bits | ⭐ | ⭐⭐⭐⭐⭐ | Alto secreto |
| ECDSA-P256 | 256 bits | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | Moderno |
| Ed25519 | 256 bits | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | Vanguardia |

### Arquitecturas de Vault

| Arquitectura | Zero-Knowledge | Recuperación | Complejidad |
|--------------|----------------|--------------|-------------|
| Cifrado en Cliente | ✅ Sí | ❌ No | Alta |
| Cifrado en Servidor | ❌ No | ✅ Sí | Media |
| Híbrida | ✅ Sí | ⚠️ Con recovery key | Alta |

---

## 🛡️ Seguridad

### ✅ Buenas Prácticas Implementadas

1. **AES-GCM**: Authenticated encryption (confidencialidad + integridad)
2. **PBKDF2**: Key derivation con 100,000 iteraciones
3. **Salts aleatorios**: Prevención de rainbow tables
4. **IVs únicos**: Nunca reutilizados
5. **Zero-Knowledge**: Servidor no puede descifrar (Vault)
6. **Perfect Forward Secrecy**: Explicado en ECDH_AES_KEYS.md

### ⚠️ Advertencias

1. **Nunca usar ECB** para cifrado real
2. **Passwords fuertes**: Mínimo 12 caracteres
3. **HTTPS en producción**: TLS 1.3 recomendado
4. **Key management**: No hardcodear claves
5. **Password perdida en Vault**: Datos irrecuperables

---

## 🧪 Pruebas

### Manual (Interfaz Web)
```
http://localhost:8080/vault-demo.html
```

### HTTP Client (IntelliJ)
```
api-tests.http
api-tests-vault.http
```

### H2 Console (Ver datos cifrados)
```
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:vaultdb
```

---

## 📚 Referencias Externas

- [NIST Cryptographic Standards](https://csrc.nist.gov/)
- [OWASP Crypto Storage](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)
- [Web Crypto API (MDN)](https://developer.mozilla.org/en-US/docs/Web/API/Web_Crypto_API)
- [RFC 5869 - HKDF](https://tools.ietf.org/html/rfc5869)
- [RFC 8017 - RSA PKCS#1](https://tools.ietf.org/html/rfc8017)

---

## 🎯 Roadmap

### ✅ Completado
- [x] Cifrado simétrico (AES)
- [x] Cifrado asimétrico (RSA, EC)
- [x] Firmas digitales
- [x] Cifrado híbrido
- [x] Vault zero-knowledge
- [x] Documentación completa

### 🔮 Futuro
- [ ] Autenticación JWT
- [ ] Cliente Android
- [ ] Cliente iOS
- [ ] Recovery keys
- [ ] Compartir secretos entre usuarios
- [ ] 2FA
- [ ] Rate limiting
- [ ] Base de datos PostgreSQL

---

## 📞 Ayuda

¿No sabes por dónde empezar?

1. **Aprender teoría**: Lee `CRIPTOGRAFIA.md`
2. **Probar cifrado básico**: Usa `api-tests.http`
3. **Entender arquitecturas**: Lee `SECURE_VAULT_ARCHITECTURE.md`
4. **Probar Vault**: Abre `vault-demo.html`
5. **Ver código**: Explora `controller/`, `service/`

---

*Última actualización: 2026-01-21*

