# 🔒 Padding en RSA - Guía Completa

## Índice
1. [¿Por qué se necesita Padding?](#por-qué-se-necesita-padding)
2. [Textbook RSA (Sin Padding)](#textbook-rsa-sin-padding)
3. [PKCS#1 v1.5 Padding](#pkcs1-v15-padding)
4. [OAEP Padding](#oaep-padding)
5. [Comparación Detallada](#comparación-detallada)
6. [Recomendaciones](#recomendaciones)

---

## ¿Por qué se necesita Padding?

RSA "puro" (Textbook RSA) tiene serios problemas de seguridad:

### Problema 1: Determinístico
```
Mismo mensaje → Mismo cifrado (¡predecible!)
```

### Problema 2: Maleable
Un atacante puede modificar el mensaje cifrado de forma controlada:
```
Si conoce: C = Encrypt(M)
Puede crear: C' = C × Encrypt(2) = Encrypt(2M)
```

### Problema 3: Pequeños exponentes
Con exponente público e=3, ciertos mensajes se pueden descifrar sin la clave privada.

**Solución:** Añadir **padding** (relleno aleatorio) antes de cifrar.

---

## Textbook RSA (Sin Padding)

### Descripción
RSA matemático puro: `C = M^e mod n`

### Código (NO implementado por seguridad)
```java
// ❌ NUNCA HACER ESTO
Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
cipher.init(Cipher.ENCRYPT_MODE, publicKey);
byte[] encrypted = cipher.doFinal(message);
```

### Problemas de Seguridad

#### 1. Ataque de Mensaje Idéntico
```java
// Mismo mensaje produce mismo cifrado
String msg = "SECRETO";
byte[] cifrado1 = encrypt(msg); // → ABC123...
byte[] cifrado2 = encrypt(msg); // → ABC123... ¡IGUAL!
```

Un atacante puede:
- Detectar cuando se envía el mismo mensaje
- Crear diccionarios de mensajes comunes

#### 2. Ataque de Malleabilidad
```java
// Si un atacante intercepta:
BigInteger C = encrypt(M);

// Puede crear (sin conocer M):
BigInteger C_doble = C.multiply(encrypt(2));
// Cuando se descifre C_doble → obtendrán 2*M
```

#### 3. Ataque de Exponente Pequeño
Si e=3 (exponente público común) y el mensaje es pequeño:
```
M^3 < n  →  C = M^3  →  M = ∛C  (raíz cúbica simple!)
```

### ¿Cuándo se podría usar? (Nunca)
❌ No hay casos legítimos para usar RSA sin padding.

---

## PKCS#1 v1.5 Padding

### Descripción
Estándar de 1993, añade bytes aleatorios antes del mensaje.

### Estructura
```
┌────┬────┬──────────────┬────┬─────────────┐
│ 00 │ 02 │ Random ≥ 8   │ 00 │ Mensaje     │
│ 1B │ 1B │   bytes      │ 1B │             │
└────┴────┴──────────────┴────┴─────────────┘
 Tipo    Padding aleatorio  Sep  Datos reales

Overhead: 11 bytes mínimo
```

### Código en SpringCrypto
```java
// Encriptar
Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
cipher.init(Cipher.ENCRYPT_MODE, publicKey);
byte[] encrypted = cipher.doFinal(plainText.getBytes());

// Desencriptar
cipher.init(Cipher.DECRYPT_MODE, privateKey);
byte[] decrypted = cipher.doFinal(encrypted);
```

### Tamaño Máximo de Mensaje
```
Para RSA-2048:
Tamaño de bloque = 2048 / 8 = 256 bytes
Overhead = 11 bytes
Mensaje máximo = 256 - 11 = 245 bytes
```

### Ventajas
✅ Amplia compatibilidad (casi universal)  
✅ Más rápido que OAEP  
✅ Añade aleatoriedad (mejor que sin padding)  
✅ Simple de implementar  

### Desventajas
⚠️ **Vulnerable a Padding Oracle Attack** (Bleichenbacher, 1998)  
⚠️ Considerado obsoleto para nuevas aplicaciones  
⚠️ No tiene seguridad demostrable matemáticamente  

### Ataque de Padding Oracle (Bleichenbacher)

#### ¿Qué es?
Un atacante envía mensajes cifrados modificados y observa si el servidor responde con error de padding válido o inválido.

#### Ejemplo del ataque:
```java
// Servidor vulnerable
try {
    cipher.doFinal(ciphertext);
    return "OK";
} catch (BadPaddingException e) {
    return "Error: Padding inválido"; // ← ¡Leak de información!
}

// El atacante puede:
for (cada modificación de ciphertext) {
    resultado = servidor.descifrar(ciphertext_modificado);
    if (resultado == "OK") {
        // ¡Descubrió un byte del mensaje!
    }
}
```

Tras ~1 millón de intentos, puede recuperar el mensaje completo.

### Mitigaciones para PKCS1
Si debes usar PKCS1:
1. ✅ Usar timing constante en verificación de padding
2. ✅ No revelar tipo de error (genérico siempre)
3. ✅ Implementar rate limiting
4. ✅ Mejor aún: **migrar a OAEP**

---

## OAEP Padding

### Descripción
**OAEP** = Optimal Asymmetric Encryption Padding  
Estándar moderno (PKCS#1 v2.0, 1998) con seguridad demostrable.

### Estructura Conceptual
```
┌─────────────────────────────────────────────┐
│  Hash(Label) ⊕ MGF(Seed)                   │ ← Masked DB
│  ────────────────────────────────           │
│  Seed ⊕ MGF(MaskedDB)                      │ ← Masked Seed
│  ────────────────────────────────           │
│  00 || Hash || Padding || 01 || Mensaje    │ ← Original
└─────────────────────────────────────────────┘

MGF = Mask Generation Function (basada en hash)
```

### Código en SpringCrypto
```java
// Encriptar con OAEP
Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
cipher.init(Cipher.ENCRYPT_MODE, publicKey);
byte[] encrypted = cipher.doFinal(plainText.getBytes());

// Desencriptar con OAEP
cipher.init(Cipher.DECRYPT_MODE, privateKey);
byte[] decrypted = cipher.doFinal(encrypted);
```

### Parámetros OAEP
```java
// OAEP con diferentes funciones hash
"RSA/ECB/OAEPWithSHA-1AndMGF1Padding"      // SHA-1 (obsoleto)
"RSA/ECB/OAEPWithSHA-256AndMGF1Padding"    // ✅ Recomendado
"RSA/ECB/OAEPWithSHA-384AndMGF1Padding"    // Más seguro
"RSA/ECB/OAEPWithSHA-512AndMGF1Padding"    // Máxima seguridad
```

### Tamaño Máximo de Mensaje
```
Para RSA-2048 con SHA-256:
Tamaño de bloque = 2048 / 8 = 256 bytes
Overhead = 2 * hashLen + 2 = 2 * 32 + 2 = 66 bytes
Mensaje máximo = 256 - 66 = 190 bytes

Nota: En la práctica Java usa 42 bytes de overhead con optimizaciones
```

### Ventajas
✅ **Resistente a padding oracle attacks**  
✅ **Seguridad demostrable** (IND-CCA2)  
✅ Recomendado por NIST, RSA Labs, IETF  
✅ Estándar en aplicaciones modernas  
✅ Protección contra ataques adaptativos  

### Desventajas
❌ Mayor overhead (42 bytes vs 11 de PKCS1)  
❌ Ligeramente más lento (~5-10%)  
❌ Menos compatible con sistemas muy antiguos (pre-2000)  

### Seguridad Demostrable (IND-CCA2)

OAEP proporciona seguridad **IND-CCA2**:
- **IND** = Indistinguishability (Indistinguibilidad)
- **CCA2** = Chosen Ciphertext Attack Adaptive (Ataque de texto cifrado elegido adaptativo)

Significa: Incluso si un atacante puede:
1. Elegir mensajes para cifrar
2. Descifrar mensajes de su elección (excepto el objetivo)
3. Adaptar sus ataques basándose en resultados previos

**No podrá** distinguir entre dos mensajes cifrados elegidos por él.

### Componentes OAEP

#### MGF1 (Mask Generation Function)
```
MGF1(seed, length) = Hash(seed || 0) || Hash(seed || 1) || ...
```

Genera una máscara pseudoaleatoria de cualquier longitud.

#### Proceso de Encoding
```
1. lHash = SHA256(Label)  // Label normalmente vacío
2. PS = Padding de ceros
3. DB = lHash || PS || 0x01 || Mensaje
4. seed = random(32 bytes)
5. dbMask = MGF1(seed, len(DB))
6. maskedDB = DB ⊕ dbMask
7. seedMask = MGF1(maskedDB, 32)
8. maskedSeed = seed ⊕ seedMask
9. EM = 0x00 || maskedSeed || maskedDB
```

---

## Comparación Detallada

### Tabla Comparativa

| Característica | Sin Padding | PKCS#1 v1.5 | OAEP |
|----------------|-------------|-------------|------|
| **Año** | - | 1993 | 1998 |
| **Seguridad** | ❌ Muy baja | ⚠️ Media | ✅ Alta |
| **Aleatoriedad** | ❌ No | ✅ Sí | ✅ Sí |
| **Padding Oracle** | N/A | ⚠️ Vulnerable | ✅ Resistente |
| **Seguridad demostrable** | ❌ No | ❌ No | ✅ Sí (IND-CCA2) |
| **Overhead (RSA-2048)** | 0 bytes | 11 bytes | 42-66 bytes |
| **Msg máx (RSA-2048)** | 256 B | 245 B | 190-214 B |
| **Velocidad** | ⚡⚡⚡ | ⚡⚡ | ⚡ |
| **Compatibilidad** | Alta | Muy alta | Alta (post-2000) |
| **Estándar actual** | ❌ Nunca | ⚠️ Legacy | ✅ Recomendado |
| **Uso en TLS 1.3** | ❌ | ❌ | ✅ |
| **Recomendado por NIST** | ❌ | ⚠️ Transición | ✅ |

### Ejemplos Prácticos

#### Mismo mensaje, diferentes resultados

```java
String mensaje = "SECRETO";
PublicKey pubKey = // ... clave RSA-2048

// Sin padding (inseguro - no implementado)
// byte[] c1 = encryptNoPadding(mensaje); // → siempre igual
// byte[] c2 = encryptNoPadding(mensaje); // → siempre igual

// PKCS1 - Diferente cada vez
byte[] pkcs1_1 = encryptPKCS1(mensaje, pubKey); // → ABC123...
byte[] pkcs1_2 = encryptPKCS1(mensaje, pubKey); // → XYZ789... ✅ Diferente

// OAEP - Diferente cada vez
byte[] oaep1 = encryptOAEP(mensaje, pubKey); // → DEF456...
byte[] oaep2 = encryptOAEP(mensaje, pubKey); // → UVW012... ✅ Diferente
```

### Overhead Visual

```
RSA-2048 (256 bytes totales)

Sin Padding:
┌────────────────────────────────────┐
│ Mensaje (hasta 256 bytes)          │ ← ❌ INSEGURO
└────────────────────────────────────┘

PKCS#1:
┌─────┬──────────────────────────────┐
│ 11B │ Mensaje (hasta 245 bytes)    │
└─────┴──────────────────────────────┘

OAEP:
┌───────┬────────────────────────────┐
│ 42B   │ Mensaje (hasta 214 bytes)  │
└───────┴────────────────────────────┘
```

---

## Uso en SpringCrypto

### Endpoints Disponibles

#### Encriptar con PKCS1
```http
POST http://localhost:8080/api/asymmetric/encrypt
Content-Type: application/json

{
  "plainText": "Mensaje de prueba",
  "publicKey": "MIIBIjANBg...",
  "padding": "PKCS1"
}
```

#### Encriptar con OAEP (Recomendado)
```http
POST http://localhost:8080/api/asymmetric/encrypt
Content-Type: application/json

{
  "plainText": "Mensaje de prueba",
  "publicKey": "MIIBIjANBg...",
  "padding": "OAEP"
}
```

### Código del Servicio

```java
// PKCS1
public String encryptPKCS1(String plainText, PublicKey publicKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}

// OAEP (Recomendado)
public String encryptOAEP(String plainText, PublicKey publicKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}
```

---

## Recomendaciones

### Para Nuevas Implementaciones
```java
✅ USAR: OAEP con SHA-256
Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
```

### Para Compatibilidad Legacy
```java
⚠️ USAR SOLO SI ES NECESARIO: PKCS1
Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");

// Y añadir mitigaciones:
try {
    result = cipher.doFinal(ciphertext);
    return result;
} catch (Exception e) {
    // NO revelar tipo de error específico
    throw new GenericCryptoException("Decryption failed");
}
```

### NUNCA Usar
```java
❌ NUNCA: Sin padding
// Cipher cipher = Cipher.getInstance("RSA/ECB/NoPadding");
```

### Tabla de Decisión

| Escenario | Padding Recomendado | Razón |
|-----------|---------------------|-------|
| Nueva aplicación | **OAEP** | Máxima seguridad |
| Aplicación crítica (banca, salud) | **OAEP con SHA-384** | Extra seguridad |
| Sistema legacy (pre-2000) | PKCS1 + mitigaciones | Compatibilidad |
| Comunicación con sistema antiguo | PKCS1 (temporal) | Migrar a OAEP ASAP |
| Cualquier caso sin restricciones | **OAEP** | Siempre la mejor opción |

### Migración de PKCS1 a OAEP

```java
// Fase 1: Soportar ambos (desencriptación)
public String decrypt(byte[] ciphertext, PrivateKey key, String padding) {
    Cipher cipher = Cipher.getInstance(
        padding.equals("OAEP") 
            ? "RSA/ECB/OAEPWithSHA-256AndMGF1Padding"
            : "RSA/ECB/PKCS1Padding"
    );
    // ...
}

// Fase 2: Cifrar solo con OAEP nuevo
public String encrypt(String plaintext, PublicKey key) {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    // ...
}

// Fase 3: Cuando todos los mensajes PKCS1 expiren, eliminar soporte
```

---

## Estándares y Referencias

### Documentos Oficiales
- [RFC 8017 - PKCS #1 v2.2](https://tools.ietf.org/html/rfc8017)
- [NIST SP 800-56B Rev. 2 - Pair-Wise Key Establishment](https://csrc.nist.gov/publications/detail/sp/800-56b/rev-2/final)
- [OWASP Cryptographic Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)

### Papers Importantes
- Bleichenbacher, D. (1998). "Chosen Ciphertext Attacks Against Protocols Based on the RSA Encryption Standard PKCS #1"
- Bellare, M., & Rogaway, P. (1994). "Optimal Asymmetric Encryption"

### Estándares de Industria
- **TLS 1.3**: Solo OAEP
- **PGP/GPG**: OAEP desde 2009
- **S/MIME**: Transición a OAEP
- **XML Encryption**: OAEP recomendado

---

## Preguntas Frecuentes

### ¿Por qué no simplemente usar AES?
RSA se usa para:
- Intercambio inicial de claves
- Firma digital
- Escenarios donde no hay canal seguro previo

Para datos grandes, usa **encriptación híbrida** (RSA + AES).

### ¿OAEP es compatible con sistemas antiguos?
OAEP es estándar desde 1998. Sistemas posteriores a 2000 lo soportan. Si necesitas compatibilidad con sistemas de los 90s, usa PKCS1 con precaución.

### ¿Puedo mezclar PKCS1 y OAEP?
No. Debe usarse el mismo padding para cifrar y descifrar:
```
PKCS1-Encrypt → PKCS1-Decrypt ✅
OAEP-Encrypt → OAEP-Decrypt ✅
PKCS1-Encrypt → OAEP-Decrypt ❌ Error
```

### ¿OAEP protege contra quantum computers?
No. RSA (con cualquier padding) es vulnerable a computadoras cuánticas. Para resistencia cuántica, investiga algoritmos post-cuánticos (NIST PQC).

---

## Conclusión

### Resumen Ejecutivo

| Pregunta | Respuesta |
|----------|-----------|
| **¿Cuál usar?** | **OAEP** (siempre que sea posible) |
| **¿Por qué OAEP?** | Seguridad demostrable, resistente a ataques |
| **¿Cuándo PKCS1?** | Solo compatibilidad legacy (y con mitigaciones) |
| **¿Sin padding?** | **NUNCA** |

### Regla de Oro

> **"Si puedes elegir, elige OAEP. Si no puedes, migra a OAEP lo antes posible."**

---

**Creado para**: SpringCrypto - Proyecto PSP DAM2  
**Versión**: 1.0.0  
**Fecha**: 2026-01-20

