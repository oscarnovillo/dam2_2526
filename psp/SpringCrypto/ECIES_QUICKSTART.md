# Guía Rápida: Cifrado con EC y ECIES

## 🔑 ¿Qué es ECIES?

**ECIES** (Elliptic Curve Integrated Encryption Scheme) es un método de cifrado que combina:
- **EC (Elliptic Curves)**: Para intercambio seguro de claves
- **AES**: Para cifrar los datos de forma rápida
- **ECDH**: Para generar una clave compartida sin transmitirla

## 📋 Flujo completo de uso

### 1️⃣ Generar claves EC

```http
GET http://localhost:8080/api/asymmetric/generate-keypair?algorithm=EC
```

**Respuesta:**
```json
{
  "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
  "privateKey": "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA...",
  "algorithm": "EC",
  "keySize": "256 (P-256/secp256r1)"
}
```

**💡 Importante:**
- La **clave pública** se puede compartir con cualquiera
- La **clave privada** debe mantenerse en secreto
- Usa la curva **P-256** (secp256r1) - estándar y segura

### 2️⃣ Cifrar con ECIES

```http
POST http://localhost:8080/api/asymmetric/encrypt-ecies
Content-Type: application/json

{
  "plainText": "Mensaje secreto 🔐",
  "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."
}
```

**Respuesta:**
```json
{
  "ephemeralPublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
  "encryptedData": "vR3K8pL...",
  "iv": "xK9mN2p...",
  "algorithm": "ECIES (ECDH + AES-GCM)",
  "description": "Cifrado híbrido con curvas elípticas"
}
```

**🔍 ¿Qué son estos campos?**
- **ephemeralPublicKey**: Clave pública temporal generada para este mensaje
- **encryptedData**: El mensaje cifrado con AES-GCM
- **iv**: Vector de inicialización para AES (debe ser único)

### 3️⃣ Descifrar con ECIES

```http
POST http://localhost:8080/api/asymmetric/decrypt-ecies
Content-Type: application/json

{
  "privateKey": "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA...",
  "ephemeralPublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
  "encryptedData": "vR3K8pL...",
  "iv": "xK9mN2p..."
}
```

**Respuesta:**
```json
{
  "plainText": "Mensaje secreto 🔐",
  "algorithm": "ECIES"
}
```

## 🎯 Ejemplo completo paso a paso

### Escenario: Alice envía un mensaje cifrado a Bob

**Paso 1: Bob genera su par de claves**
```bash
# Bob genera sus claves EC
GET /api/asymmetric/generate-keypair?algorithm=EC

# Bob guarda su clave privada en secreto
bobPrivateKey = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA..."

# Bob comparte su clave pública
bobPublicKey = "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."
```

**Paso 2: Alice cifra un mensaje para Bob**
```bash
# Alice usa la clave pública de Bob para cifrar
POST /api/asymmetric/encrypt-ecies
{
  "plainText": "Hola Bob, esto es secreto!",
  "publicKey": "<bobPublicKey>"
}

# Alice recibe el resultado cifrado:
{
  "ephemeralPublicKey": "MFkwEw...",  # Clave temporal
  "encryptedData": "xK9mN2p...",       # Mensaje cifrado
  "iv": "vR3K8pL..."                   # IV
}
```

**Paso 3: Alice envía los datos a Bob**
```bash
# Alice envía a Bob:
- ephemeralPublicKey
- encryptedData
- iv
```

**Paso 4: Bob descifra el mensaje**
```bash
# Bob usa su clave privada para descifrar
POST /api/asymmetric/decrypt-ecies
{
  "privateKey": "<bobPrivateKey>",
  "ephemeralPublicKey": "MFkwEw...",
  "encryptedData": "xK9mN2p...",
  "iv": "vR3K8pL..."
}

# Bob recupera el mensaje:
{
  "plainText": "Hola Bob, esto es secreto!"
}
```

## 🔬 ¿Cómo funciona internamente?

### Durante el cifrado:

```
1. Alice genera claves efímeras (temporales):
   - Clave privada efímera: r (número aleatorio)
   - Clave pública efímera: R = r × G

2. Alice calcula un secreto compartido usando ECDH:
   - Secreto = r × (clave pública de Bob)

3. Alice deriva una clave AES del secreto:
   - clave_AES = SHA-256(secreto)

4. Alice cifra el mensaje con AES-GCM:
   - mensaje_cifrado = AES-GCM(clave_AES, mensaje)

5. Alice envía:
   - R (clave pública efímera)
   - mensaje_cifrado
   - IV
```

### Durante el descifrado:

```
1. Bob calcula el MISMO secreto compartido:
   - Secreto = (clave privada de Bob) × R
   - ¡Es el mismo valor que calculó Alice!

2. Bob deriva la misma clave AES:
   - clave_AES = SHA-256(secreto)

3. Bob descifra el mensaje:
   - mensaje = AES-GCM-Decrypt(clave_AES, mensaje_cifrado)
```

## ✨ Ventajas de ECIES

1. **Sin límite de tamaño**: Puedes cifrar mensajes de cualquier longitud
2. **Claves pequeñas**: EC-256 = RSA-3072 en seguridad
3. **Forward Secrecy**: Cada mensaje usa claves efímeras nuevas
4. **Rápido**: Más eficiente que RSA puro
5. **Seguro**: Combina lo mejor de EC y AES

## 📊 Comparación con RSA

| Característica | RSA-2048 | ECIES (EC-256) |
|----------------|----------|----------------|
| **Tamaño clave pública** | ~294 bytes | ~91 bytes |
| **Tamaño máximo directo** | 214 bytes | ∞ (sin límite) |
| **Velocidad** | Lento | Rápido |
| **Forward Secrecy** | ❌ | ✅ |
| **Tamaño firma** | 256 bytes | ~64 bytes |

## 🛡️ Seguridad

### ✅ Buenas prácticas:

1. **Nunca reutilices claves efímeras**: ECIES las genera automáticamente
2. **Nunca reutilices el IV**: Cada cifrado genera uno nuevo
3. **Protege la clave privada**: Solo el destinatario debe tenerla
4. **Usa curvas estándar**: P-256 es segura y compatible

### ⚠️ Qué NO hacer:

1. ❌ No compartas tu clave privada
2. ❌ No uses curvas no estándar
3. ❌ No implementes tu propia criptografía
4. ❌ No guardes claves en texto plano

## 🔐 Firma digital con EC (ECDSA)

ECIES es para **cifrado**. Para **firmas digitales** usa ECDSA:

```http
# Firmar
POST /api/asymmetric/sign
{
  "message": "Documento a firmar",
  "privateKey": "<tu_clave_privada_EC>"
}

# Verificar
POST /api/asymmetric/verify
{
  "message": "Documento a firmar",
  "signature": "<firma>",
  "publicKey": "<clave_publica_EC>"
}
```

## 💻 Código Java

```java
// Generar claves EC
KeyPair keyPair = asymmetricService.generateKeyPair("EC");
PublicKey publicKey = keyPair.getPublic();
PrivateKey privateKey = keyPair.getPrivate();

// Cifrar con ECIES
ECIESResult encrypted = asymmetricService.encryptECIES(
    "Mensaje secreto", 
    publicKey
);

// Descifrar
String decrypted = asymmetricService.decryptECIES(
    encrypted, 
    privateKey
);
```

## 🌐 Casos de uso reales

1. **Mensajería cifrada**: WhatsApp, Signal (usan variantes de ECIES)
2. **TLS 1.3**: Usa ECDH para intercambio de claves
3. **Blockchain**: Bitcoin, Ethereum usan EC
4. **VPN**: WireGuard usa Curve25519
5. **Email cifrado**: PGP puede usar EC

## 📚 Archivos de referencia

- **api-tests-ecies.http**: Ejemplos de llamadas HTTP
- **ECIES_EXPLAINED.md**: Explicación técnica detallada
- **ELLIPTIC_CURVES.md**: Teoría de curvas elípticas

## 🚀 Pruébalo ahora

1. Inicia el servidor:
   ```bash
   mvn spring-boot:run
   ```

2. Abre el archivo `api-tests-ecies.http` en IntelliJ

3. Ejecuta las peticiones en orden:
   - Genera claves
   - Cifra un mensaje
   - Descifra el mensaje

4. Ve el demo completo:
   ```http
   GET http://localhost:8080/api/asymmetric/demo
   ```

## ❓ Preguntas frecuentes

**¿Por qué "efímera"?**
- Porque la clave solo existe para ese mensaje y luego se descarta
- Proporciona Forward Secrecy

**¿Por qué ECDH?**
- Permite que dos partes generen el mismo secreto sin transmitirlo
- Matemática de curvas elípticas: `a×(b×G) = b×(a×G)`

**¿Es seguro para producción?**
- Sí, si usas curvas estándar (P-256, P-384)
- Usado en sistemas críticos: banca, militar, gobierno

**¿Diferencia entre EC, ECDSA y ECIES?**
- **EC**: Curvas elípticas (concepto general)
- **ECDSA**: Firma digital con EC
- **ECIES**: Cifrado con EC
- **ECDH**: Intercambio de claves con EC

---

**¡Disfruta cifrando con ECIES! 🎉🔐**

