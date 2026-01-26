# ECIES - Elliptic Curve Integrated Encryption Scheme

## ¿Qué es ECIES?

**ECIES** (Elliptic Curve Integrated Encryption Scheme) es un esquema de cifrado híbrido que combina:
- **ECDH** (Elliptic Curve Diffie-Hellman) para intercambio de claves
- **KDF** (Key Derivation Function) para derivar claves simétricas
- **Cifrado simétrico** (AES-GCM) para cifrar los datos
- **MAC** (Message Authentication Code) para autenticación (incluido en GCM)

## ¿Por qué ECIES?

### Ventajas sobre RSA puro:

1. **Claves más pequeñas**: EC-256 = RSA-3072 en seguridad
   - Clave pública EC: ~91 bytes
   - Clave pública RSA-2048: ~294 bytes

2. **Sin límite de tamaño**: Cifra mensajes de cualquier longitud
   - RSA-2048 con OAEP: máximo 214 bytes
   - ECIES: sin límite

3. **Más rápido**: Operaciones EC son más eficientes

4. **Forward Secrecy**: Usa claves efímeras (opcionales)

## Cómo funciona ECIES

### Cifrado (Alice envía mensaje a Bob)

```
1. Alice tiene la clave pública de Bob: Qb (punto en curva elíptica)

2. Alice genera un par de claves efímero:
   - Clave privada efímera: r (número aleatorio)
   - Clave pública efímera: R = r × G (punto en curva)

3. Alice calcula el secreto compartido usando ECDH:
   - S = r × Qb (multiplicación de punto por escalar)
   - S es un punto en la curva elíptica

4. Alice deriva una clave AES del secreto compartido:
   - aesKey = KDF(S) 
   - Ejemplo: aesKey = SHA-256(S.x)  (coordenada x del punto S)

5. Alice cifra el mensaje con AES-GCM:
   - iv = random(12 bytes)
   - encryptedData = AES-GCM(aesKey, iv, plainText)

6. Alice envía a Bob:
   - R (clave pública efímera)
   - encryptedData
   - iv
```

### Descifrado (Bob recibe mensaje de Alice)

```
1. Bob tiene su clave privada: db (número)

2. Bob recibe de Alice:
   - R (clave pública efímera)
   - encryptedData
   - iv

3. Bob calcula el mismo secreto compartido:
   - S = db × R
   - S = db × (r × G) = r × (db × G) = r × Qb  (¡mismo valor que Alice!)

4. Bob deriva la misma clave AES:
   - aesKey = KDF(S)
   - aesKey = SHA-256(S.x)

5. Bob descifra el mensaje:
   - plainText = AES-GCM-Decrypt(aesKey, iv, encryptedData)
```

## Matemáticas detrás de ECIES

### ECDH (Elliptic Curve Diffie-Hellman)

La "magia" está en la propiedad conmutativa de la multiplicación escalar:

```
a × (b × G) = b × (a × G) = (a × b) × G

Donde:
- G: punto generador de la curva (público)
- a, b: números privados (escalares)
- a × G, b × G: puntos públicos en la curva
```

**Aplicado a ECIES:**
```
Alice calcula: r × Qb = r × (db × G)
Bob calcula:   db × R = db × (r × G)

Resultado: ¡Ambos obtienen el mismo punto S!
```

### Seguridad

La seguridad de ECIES se basa en el **Problema del Logaritmo Discreto en Curvas Elípticas (ECDLP)**:

```
Dado: Q = d × G
Encontrar: d

Es computacionalmente intratable si la curva es segura.
```

## Curvas Elípticas en Java

### Curva P-256 (secp256r1)

```java
// Generar claves EC
KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
keyGen.initialize(ecSpec);
KeyPair keyPair = keyGen.generateKeyPair();
```

**Parámetros de P-256:**
- **Nombre**: secp256r1, prime256v1, NIST P-256
- **Tamaño**: 256 bits
- **Seguridad**: Equivalente a AES-128 o RSA-3072
- **Uso**: TLS 1.3, Bitcoin, muchos sistemas

### Otras curvas disponibles en Java

```java
// Curva P-384 (más segura, más lenta)
new ECGenParameterSpec("secp384r1");  // Equivalente a AES-192

// Curva P-521 (máxima seguridad)
new ECGenParameterSpec("secp521r1");  // Equivalente a AES-256

// Curve25519 (no disponible en Java estándar, requiere BouncyCastle)
// new ECGenParameterSpec("curve25519");
```

## Comparación: RSA vs ECIES

| Característica | RSA-2048 | EC-256 (ECIES) |
|----------------|----------|----------------|
| **Seguridad equivalente** | ~112 bits | ~128 bits |
| **Tamaño clave pública** | ~294 bytes | ~91 bytes |
| **Tamaño clave privada** | ~1192 bytes | ~32 bytes |
| **Tamaño firma** | 256 bytes | ~64 bytes |
| **Máx. datos directo** | 214 bytes (OAEP) | Sin límite (híbrido) |
| **Velocidad cifrado** | Lento | Rápido |
| **Velocidad firma** | Lento | Muy rápido |
| **Forward Secrecy** | ❌ No (sin modificar) | ✅ Sí (con claves efímeras) |

## Ejemplo práctico en Java

```java
// 1. Bob genera su par de claves
KeyPair bobKeyPair = generateKeyPair("EC");
PublicKey bobPublicKey = bobKeyPair.getPublic();
PrivateKey bobPrivateKey = bobKeyPair.getPrivate();

// 2. Alice cifra un mensaje para Bob usando ECIES
String message = "Mensaje secreto para Bob";
ECIESResult encrypted = encryptECIES(message, bobPublicKey);

// encrypted contiene:
// - ephemeralPublicKey (R): clave pública efímera de Alice
// - encryptedData: mensaje cifrado con AES-GCM
// - iv: vector de inicialización

// 3. Bob descifra el mensaje
String decrypted = decryptECIES(encrypted, bobPrivateKey);
// decrypted = "Mensaje secreto para Bob"
```

## KDF - Key Derivation Function

En nuestra implementación usamos un KDF simple:

```java
// Secreto compartido ECDH (punto en curva)
byte[] sharedSecret = keyAgreement.generateSecret();

// Derivar clave AES con SHA-256
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] aesKey = digest.digest(sharedSecret);
```

**KDF más robustos:**
- **HKDF** (HMAC-based KDF): RFC 5869
- **PBKDF2**: Para passwords
- **Argon2**: Para passwords (más moderno)

## AES-GCM en ECIES

Usamos **AES-GCM** (Galois/Counter Mode) porque:

1. **Confidencialidad**: Cifra los datos
2. **Autenticidad**: Incluye MAC automáticamente
3. **AEAD**: Authenticated Encryption with Associated Data
4. **Rendimiento**: Muy rápido en hardware moderno

```java
Cipher aesCipher = Cipher.getInstance("AES/GCM/NoPadding");
byte[] iv = new byte[12];  // GCM recomienda 12 bytes
new SecureRandom().nextBytes(iv);

GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);  // 128-bit tag
aesCipher.init(Cipher.ENCRYPT_MODE, aesKey, gcmSpec);

byte[] encrypted = aesCipher.doFinal(plainText.getBytes());
```

## Forward Secrecy

ECIES proporciona **Perfect Forward Secrecy (PFS)** cuando se usan claves efímeras:

```
- Cada mensaje usa una nueva clave efímera (r, R)
- Si se compromete la clave privada de Bob, los mensajes anteriores 
  siguen siendo seguros porque las claves efímeras fueron destruidas
```

**Usado en:**
- TLS 1.3 con ECDHE (Ephemeral DH)
- Signal Protocol (doble ratchet)
- WhatsApp, Telegram

## Casos de uso

### ✅ Cuándo usar ECIES

1. **Mensajes largos**: Sin límite de tamaño
2. **Eficiencia**: Claves pequeñas, rápido
3. **Forward Secrecy**: Seguridad a largo plazo
4. **Sistemas modernos**: Móviles, IoT

### ❌ Cuándo NO usar ECIES

1. **Compatibilidad legacy**: Algunos sistemas viejos no soportan EC
2. **Firmas con validez legal**: RSA más establecido jurídicamente
3. **Hardware específico**: Algunos HSM no soportan EC

## Vulnerabilidades y mejores prácticas

### ⚠️ Peligros

1. **Curvas débiles**: Usar solo curvas estándar (P-256, P-384, P-521)
2. **Implementación incorrecta de KDF**: Usar SHA-256 o HKDF
3. **Reutilizar claves efímeras**: Generar nueva para cada mensaje
4. **IV reutilizado**: Generar nuevo IV aleatorio cada vez

### ✅ Mejores prácticas

1. **Usar curvas NIST**: P-256 (secp256r1) es estándar
2. **Validar puntos**: Verificar que estén en la curva
3. **Usar bibliotecas probadas**: Java JCA, BouncyCastle
4. **AEAD**: AES-GCM en vez de AES-CBC + HMAC

## Comparación con otros esquemas

### ECIES vs RSA-OAEP

```
ECIES:
+ Claves más pequeñas
+ Más rápido
+ Sin límite de tamaño
+ Forward Secrecy
- Menos maduro legalmente

RSA-OAEP:
+ Más establecido
+ Más compatible
- Claves grandes
- Límite de tamaño estricto
- Más lento
```

### ECIES vs RSA Híbrido (RSA + AES)

```
ECIES:
+ Más eficiente (claves EC pequeñas)
+ Forward Secrecy con claves efímeras
+ Más rápido

RSA Híbrido:
+ Más compatible
+ Establecido
- Claves RSA grandes
- Sin Forward Secrecy (sin modificar)
```

## Referencias

- **SEC 1**: Elliptic Curve Cryptography (Standards for Efficient Cryptography)
- **NIST FIPS 186-4**: Digital Signature Standard (DSS)
- **RFC 6637**: Elliptic Curve Cryptography in OpenPGP
- **ANSI X9.63**: Elliptic Curve Key Agreement and Key Transport Protocols

## ⚠️ ECIES: Datos Efímeros vs Persistentes

### Forward Secrecy Real (Mensajes Efímeros)

```
Ejemplo: Signal, WhatsApp
━━━━━━━━━━━━━━━━━━━━━

1. Alice cifra mensaje con ECIES
   - Genera claves efímeras (r, R)
   - Cifra mensaje
   - Envía (R, encrypted, iv)
   - ✅ DESTRUYE r inmediatamente

2. Bob recibe y descifra
   - Usa su clave privada + R
   - Lee el mensaje
   - ✅ DESTRUYE todo

Resultado:
✅ Forward Secrecy REAL
✅ Hackear después = mensajes siguen seguros
```

### Forward Secrecy Perdido (Datos Persistentes)

```
Ejemplo: Caja fuerte compartida
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

1. Alice cifra secreto para guardar en BD
   - Genera claves efímeras (r, R)
   - Cifra mensaje
   - ❌ GUARDA (R, encrypted, iv) en BD
   - ❌ r se destruye pero R queda expuesta

2. Bob puede descifrar N veces
   - Lee (R, encrypted, iv) de BD
   - Descifra con su clave privada
   - Puede repetir indefinidamente

Resultado:
❌ NO hay Forward Secrecy
❌ Hackear BD = pueden descifrar todo
❌ La clave efímera R es permanente, no efímera
```

### ¿Cuándo usar ECIES para datos persistentes?

**❌ NO usar ECIES puro si:**
- Múltiples usuarios (> 5)
- Mensajes grandes (> 10KB)
- Necesitas eficiencia

**Problema:** N copias del mensaje cifrado

```
Mensaje: 1MB
Usuarios: 100

ECIES puro:
  - 100 copias del mensaje = 100MB
  - 100 claves efímeras = 9.1KB
  - TOTAL: ~100MB

RSA híbrido:
  - 1 copia del mensaje = 1MB
  - 100 claves AES cifradas = 25.6KB
  - TOTAL: ~1MB
```

**✅ Usar ECIES puro si:**
- Pocos usuarios (< 5)
- Mensajes pequeños (< 1KB)
- Necesitas aislamiento total entre usuarios
- Cada usuario tiene su propia versión del secreto

### Solución Híbrida Recomendada

Para compartir secretos persistentes con múltiples usuarios:

```
1. Generar 1 clave AES aleatoria
2. Cifrar mensaje 1 vez con AES
3. Para cada usuario:
   - RSA: cifrar clave AES con su pública
   - O EC: usar ECIES solo para cifrar clave AES (32 bytes)

Ventajas:
✅ 1 copia del mensaje
✅ Claves pequeñas por usuario
✅ Fácil agregar/remover usuarios
✅ Estándar (PGP, S/MIME)
```

**Ver:** `SHARING_PERSISTENT_SECRETS.md` para comparación detallada.

---

## Conclusión

ECIES es un esquema de cifrado moderno y eficiente que combina lo mejor de:
- **Criptografía de curva elíptica**: Claves pequeñas, rápido
- **Cifrado simétrico**: Sin límite de tamaño
- **Forward Secrecy**: Seguridad a largo plazo (solo con mensajes efímeros)

**Ideal para:**
- ✅ Mensajería cifrada (Signal, WhatsApp)
- ✅ Comunicaciones efímeras
- ✅ Intercambio de claves una vez

**NO ideal para:**
- ❌ Almacenamiento compartido persistente (usar RSA/EC híbrido)
- ❌ Múltiples usuarios con mensajes grandes
- ❌ Cuando necesitas Forward Secrecy real pero datos persistentes (imposible)

