# ⚠️ Aclaración Importante: "ECB" en RSA

## El Malentendido Común

Cuando ves esto en Java:
```java
Cipher.getInstance("RSA/ECB/PKCS1Padding");
```

**NO significa** que estés usando el modo ECB (Electronic Codebook) como en AES.

## ¿Por qué dice "ECB" entonces?

### Razón Histórica

Java requiere el formato: `"Algoritmo/Modo/Padding"`

Para RSA:
- **Algoritmo:** RSA
- **Modo:** "ECB" (placeholder histórico, NO es un modo real)
- **Padding:** PKCS1Padding o OAEPPadding

### La Verdad

```java
// Estos son equivalentes:
"RSA/ECB/PKCS1Padding"   // Convención estándar
"RSA/NONE/PKCS1Padding"  // Más correcto técnicamente
"RSA"                    // Java asume ECB/PKCS1 por defecto
```

## ¿Por qué RSA NO necesita modos de operación?

### 1. Limitación de Tamaño

RSA solo puede cifrar datos menores que el tamaño de la clave:

```
RSA-2048 con PKCS#1 v1.5:
- Tamaño clave: 256 bytes
- Overhead padding: 11 bytes
- Máximo mensaje: 245 bytes
→ SIEMPRE es un solo bloque

RSA-2048 con OAEP:
- Tamaño clave: 256 bytes  
- Overhead padding: 42 bytes
- Máximo mensaje: 214 bytes
→ SIEMPRE es un solo bloque
```

### 2. Comparación con AES

```java
// AES necesita modos porque puede cifrar datos de cualquier tamaño
AES/ECB - Múltiples bloques de 16 bytes (inseguro)
AES/CBC - Múltiples bloques encadenados (seguro)
AES/GCM - Múltiples bloques con autenticación (muy seguro)

// RSA NO necesita modos porque SIEMPRE es un solo bloque
RSA/ECB - Solo un bloque (el "ECB" es irrelevante)
```

## Demostración Visual

### AES (necesita modo ECB/CBC/GCM)

```
Mensaje largo: "Este es un mensaje muy largo que necesita múltiples bloques..."

AES/ECB:
┌──────────┬──────────┬──────────┬──────────┐
│ Bloque 1 │ Bloque 2 │ Bloque 3 │ Bloque 4 │ ← Múltiples bloques
└──────────┴──────────┴──────────┴──────────┘
    ↓           ↓           ↓           ↓
  Cifrar      Cifrar      Cifrar      Cifrar  ← Cada uno independiente
```

### RSA (NO necesita modo)

```
Mensaje corto: "Clave AES" (solo 9 bytes)

RSA:
┌──────────────────┐
│  Un solo bloque  │ ← Siempre un solo bloque (máx ~200 bytes)
└──────────────────┘
         ↓
      Cifrar         ← Una sola operación
```

## ¿Qué pasa si necesitas cifrar más datos con RSA?

### Opción 1: Encriptación Híbrida (RECOMENDADO)

```java
// 1. Genera clave AES aleatoria
SecretKey aesKey = generateAESKey();

// 2. Cifra datos grandes con AES
byte[] dataCifrada = aesEncrypt(datosGrandes, aesKey);

// 3. Cifra SOLO la clave AES con RSA (pequeña, cabe en un bloque)
byte[] claveCifrada = rsaEncrypt(aesKey, publicKey);

// Envía: dataCifrada + claveCifrada
```

**Ventajas:**
- ✅ Sin límite de tamaño
- ✅ Rápido (AES es ~1000x más rápido que RSA)
- ✅ Seguro (usa lo mejor de ambos algoritmos)

### Opción 2: Dividir manualmente en bloques (NO RECOMENDADO)

```java
// ❌ MAL: Dividir datos y cifrar cada bloque con RSA
for (bloque in dividirDatos(mensaje, 214)) {
    bloquesCifrados.add(rsaEncrypt(bloque, publicKey));
}

// Problemas:
// - Lento (RSA es muy lento)
// - Patrones detectables (como ECB en AES)
// - No hay integridad entre bloques
```

## Corrección en la Documentación

### Terminología Correcta

| ❌ Incorrecto | ✅ Correcto |
|---------------|-------------|
| "RSA en modo ECB" | "RSA directo" o "RSA sin modo de bloques" |
| "RSA necesita modo ECB" | "RSA no requiere modo de operación" |
| "Modo de operación RSA/ECB" | "RSA de bloque único" |

### Explicación Precisa

```java
// INCORRECTO decir:
"RSA/ECB/PKCS1Padding usa el modo ECB"

// CORRECTO decir:
"RSA/ECB/PKCS1Padding es la sintaxis de Java para RSA con padding PKCS1.
El 'ECB' es un placeholder histórico que no indica un modo de operación real,
ya que RSA siempre procesa un solo bloque a la vez."
```

## Ejemplos en SpringCrypto

### Código Actual (con "ECB")

```java
// AsymmetricEncryptionService.java

public String encryptPKCS1(String plainText, PublicKey publicKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
    // El "ECB" aquí es solo sintaxis, no un modo real
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}

public String encryptOAEP(String plainText, PublicKey publicKey) throws Exception {
    Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
    // El "ECB" aquí también es solo sintaxis
    cipher.init(Cipher.ENCRYPT_MODE, publicKey);
    byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(encrypted);
}
```

### Alternativas Válidas (menos comunes)

```java
// Estas son equivalentes:
Cipher.getInstance("RSA/ECB/PKCS1Padding");   // Estándar
Cipher.getInstance("RSA/NONE/PKCS1Padding");  // Más preciso
Cipher.getInstance("RSA");                     // Usa PKCS1 por defecto
```

## Comparación: AES vs RSA

| Aspecto | AES | RSA |
|---------|-----|-----|
| **Tamaño de bloque** | Fijo: 16 bytes | Varía: hasta ~256 bytes (RSA-2048) |
| **Datos a cifrar** | Ilimitados (múltiples bloques) | Limitados (un solo bloque) |
| **Necesita modo** | ✅ Sí (ECB/CBC/CTR/GCM) | ❌ No (siempre un bloque) |
| **"ECB" en nombre** | Indica modo real | Placeholder histórico |
| **Para datos grandes** | Directo con modo adecuado | Usar encriptación híbrida |

## Conclusión

### Resumen

1. ✅ **Tienes razón:** RSA no necesita realmente "ECB" porque siempre cifra un solo bloque
2. ⚠️ El "ECB" en `RSA/ECB/PKCS1Padding` es **solo nomenclatura de Java**, no un modo real
3. 🎯 RSA está limitado a ~214-245 bytes por operación (un solo bloque)
4. 🔄 Para datos grandes, usa **encriptación híbrida** (RSA + AES)

### Analogía

```
AES es como un tren:
- Puede llevar muchos vagones (bloques)
- Necesita decidir cómo conectarlos (modo: ECB/CBC/GCM)

RSA es como un taxi:
- Solo lleva una carga pequeña (un bloque)
- No necesita modo de conexión (siempre es una sola operación)
```

---

## Para Recordar

```java
// Cuando veas esto:
Cipher.getInstance("RSA/ECB/PKCS1Padding");

// Piensa: "RSA con padding PKCS1, bloque único"
// NO pienses: "RSA en modo ECB como AES"

// El nombre correcto sería:
// Cipher.getInstance("RSA/SINGLE_BLOCK/PKCS1Padding");
// Pero Java usa "ECB" por convención histórica
```

---

**Creado para**: SpringCrypto - Proyecto PSP DAM2  
**Versión**: 1.0.0  
**Fecha**: 2026-01-20  
**Nota**: Aclaración sobre la nomenclatura "ECB" en RSA

