# 📘 Curvas Elípticas (EC) - Guía Rápida

## ¿Qué son las Curvas Elípticas?

Las **Curvas Elípticas (EC - Elliptic Curves)** son una alternativa moderna a RSA para criptografía asimétrica. Ofrecen el mismo nivel de seguridad que RSA pero con claves mucho más pequeñas.

## Ventajas de EC sobre RSA

### 🔑 Tamaño de Claves Mucho Menor

| Seguridad Equivalente | RSA | EC (ECDSA) | Reducción |
|----------------------|-----|------------|-----------|
| 80 bits | 1024 bits | 160 bits | **84% más pequeña** |
| 112 bits | 2048 bits | 224 bits | **89% más pequeña** |
| **128 bits** | **3072 bits** | **256 bits** | **92% más pequeña** |
| 192 bits | 7680 bits | 384 bits | **95% más pequeña** |
| 256 bits | 15360 bits | 521 bits | **97% más pequeña** |

### ⚡ Rendimiento

- **Generación de claves**: Más rápida que RSA
- **Firma digital**: Mucho más rápida que RSA
- **Verificación**: Similar o ligeramente más lenta que RSA
- **Tamaño de firma**: ~70 bytes (EC-256) vs ~256 bytes (RSA-2048)

### 💾 Menor Uso de Ancho de Banda

- Claves públicas más pequeñas
- Firmas digitales más compactas
- Ideal para IoT y dispositivos móviles

## Curvas Elípticas Estándar

### secp256r1 (P-256 / prime256v1)

✅ **Usada en este proyecto**

- **Seguridad**: Equivalente a RSA-3072 (128 bits de seguridad)
- **Estándar**: NIST P-256, FIPS 186-4
- **Uso**: TLS, Bitcoin, Ethereum
- **Tamaño de clave pública**: 64 bytes (sin comprimir)
- **Tamaño de firma**: ~70-72 bytes

### Otras Curvas Populares

| Curva | Bits | Seguridad | Uso |
|-------|------|-----------|-----|
| **secp256k1** | 256 | 128 bits | Bitcoin, Ethereum |
| **secp384r1** (P-384) | 384 | 192 bits | Aplicaciones de alta seguridad |
| **secp521r1** (P-521) | 521 | 256 bits | Máxima seguridad |
| **Curve25519** | 255 | 128 bits | Signal, SSH, WireGuard |
| **Ed25519** | 255 | 128 bits | Firmas EdDSA (muy rápidas) |

## ECDSA vs RSA

### Algoritmo de Firma

```java
// RSA
Signature signature = Signature.getInstance("SHA256withRSA");

// ECDSA
Signature signature = Signature.getInstance("SHA256withECDSA");
```

### Comparación Práctica

#### Generar Claves
```java
// RSA-2048 (1-2 segundos)
KeyPairGenerator rsaGen = KeyPairGenerator.getInstance("RSA");
rsaGen.initialize(2048);

// EC-256 (milisegundos)
KeyPairGenerator ecGen = KeyPairGenerator.getInstance("EC");
ecGen.initialize(new ECGenParameterSpec("secp256r1"));
```

#### Firmar
```java
// RSA: ~256 bytes
String rsaSignature = rsaService.sign(message, rsaPrivateKey);

// ECDSA: ~70 bytes
String ecSignature = ecService.sign(message, ecPrivateKey);
```

## Casos de Uso en SpringCrypto

### 1. Generar Par de Claves EC

**HTTP Request:**
```http
GET http://localhost:8080/api/asymmetric/generate-keypair?algorithm=EC
```

**Response:**
```json
{
  "publicKey": "MFkw...base64...",
  "privateKey": "MIG...base64...",
  "algorithm": "EC",
  "keySize": "256 (P-256/secp256r1)"
}
```

### 2. Firmar con ECDSA

**HTTP Request:**
```http
POST http://localhost:8080/api/asymmetric/sign
Content-Type: application/json

{
  "message": "Mensaje a firmar",
  "privateKey": "MIG...base64..."
}
```

**Response:**
```json
{
  "signature": "MEU...base64..." // ~70 bytes en Base64
}
```

### 3. Verificar Firma ECDSA

**HTTP Request:**
```http
POST http://localhost:8080/api/asymmetric/verify
Content-Type: application/json

{
  "message": "Mensaje a firmar",
  "signature": "MEU...base64...",
  "publicKey": "MFkw...base64..."
}
```

**Response:**
```json
{
  "valid": true
}
```

## Limitaciones de EC

### ❌ No se puede usar para Encriptación Directa

EC (específicamente ECDSA) se usa principalmente para:
- ✅ **Firma Digital** (ECDSA)
- ✅ **Intercambio de Claves** (ECDH - Elliptic Curve Diffie-Hellman)

**NO para:**
- ❌ Encriptación directa de mensajes (como RSA)

Si necesitas encriptar con EC:
- Usa **ECIES** (Elliptic Curve Integrated Encryption Scheme)
- O usa **ECDH** para intercambiar una clave AES

## Implementación en el Proyecto

### Servicio (AsymmetricEncryptionService.java)

```java
// Generar claves EC
public KeyPair generateKeyPair(String algorithm) throws Exception {
    if (algorithm.equalsIgnoreCase("EC")) {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        keyPairGenerator.initialize(ecSpec);
        return keyPairGenerator.generateKeyPair();
    }
    // ... RSA ...
}

// Firmar (detecta automáticamente el algoritmo)
public String sign(String message, PrivateKey privateKey) throws Exception {
    String algorithm = privateKey.getAlgorithm();
    String signatureAlgorithm = algorithm.equals("EC") ? 
        "SHA256withECDSA" : "SHA256withRSA";
    
    Signature signature = Signature.getInstance(signatureAlgorithm);
    signature.initSign(privateKey);
    signature.update(message.getBytes(StandardCharsets.UTF_8));
    return Base64.getEncoder().encodeToString(signature.sign());
}
```

### Controlador (AsymmetricEncryptionController.java)

```java
@GetMapping("/generate-keypair")
public ResponseEntity<?> generateKeyPair(
    @RequestParam(required = false, defaultValue = "RSA") String algorithm) {
    
    KeyPair keyPair = encryptionService.generateKeyPair(algorithm);
    return ResponseEntity.ok(Map.of(
        "publicKey", base64PublicKey,
        "privateKey", base64PrivateKey,
        "algorithm", keyPair.getPublic().getAlgorithm(),
        "keySize", algorithm.equalsIgnoreCase("EC") ? 
            "256 (P-256/secp256r1)" : "2048"
    ));
}
```

## Pruebas en api-tests.http

El archivo `api-tests.http` incluye pruebas completas de EC:

- **Test 10b**: Generar claves EC
- **Test 18b**: Firmar con ECDSA
- **Test 18c**: Verificar firma ECDSA válida
- **Test 18d**: Verificar firma ECDSA inválida
- **Test 18e**: Comparar tamaños RSA vs ECDSA

## Seguridad y Recomendaciones

### ✅ Cuándo Usar EC

- Aplicaciones móviles (menor tamaño)
- IoT y dispositivos con recursos limitados
- Blockchain y criptomonedas
- Comunicaciones de baja latencia
- TLS moderno (TLS 1.3 prefiere EC)

### ✅ Cuándo Usar RSA

- Compatibilidad con sistemas legacy
- Encriptación directa de datos pequeños
- Cuando el soporte de EC no está garantizado
- Normativas que requieren RSA específicamente

### 🔒 Mejores Prácticas

1. **Usar curvas estándar**: secp256r1, secp384r1, secp521r1
2. **Evitar curvas personalizadas** (alto riesgo de error)
3. **Para firma digital**: ECDSA es excelente
4. **Para intercambio de claves**: ECDH
5. **Actualizar bibliotecas** regularmente
6. **Validar curvas**: Asegurar que sean seguras y estándar

## Referencias y Más Información

- [NIST SP 800-186 - Discrete Logarithm-based Crypto](https://csrc.nist.gov/publications/detail/sp/800-186/final)
- [SEC 2: Recommended Elliptic Curve Domain Parameters](https://www.secg.org/sec2-v2.pdf)
- [SafeCurves - Secure ECC Curves](https://safecurves.cr.yp.to/)
- [RFC 6090 - Fundamental ECC Algorithms](https://tools.ietf.org/html/rfc6090)

---

**Creado para**: SpringCrypto - Proyecto PSP DAM2  
**Versión**: 1.0.0  
**Fecha**: 2026-01-20

