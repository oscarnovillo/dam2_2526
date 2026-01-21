# 🔏 Certificación de Claves Públicas - Prevención de MITM

## El Problema: Man-in-the-Middle (MITM)

### Escenario de Ataque

```
Usuario A                    Atacante                    Servidor
   │                            │                            │
   │ GET /public-key/B          │                            │
   ├────────────────────────────┼────────────────────────────>
   │                            │                            │
   │                            │  publicKey_B               │
   │                            <────────────────────────────┤
   │                            │                            │
   │  publicKey_ATACANTE 😈     │                            │
   <────────────────────────────┤                            │
   │                            │                            │
   │ (A cifra con clave del atacante)                        │
   │ POST /share                │                            │
   ├────────────────────────────>                            │
   │                            │                            │
   │        Atacante puede descifrar el secreto 💀           │
```

**Problema**: El cliente no tiene forma de verificar que la clave pública realmente pertenece al usuario B.

## ✅ Solución: Servidor Firma las Claves Públicas

### Concepto: PKI Simplificada

```
┌────────────────────────────────────────────────────────┐
│  SERVIDOR tiene par de claves maestro (RSA/EC)         │
│  ├─ Server Private Key (SOLO en servidor, protegida)   │
│  └─ Server Public Key (embebida en clientes)           │
└────────────────────────────────────────────────────────┘
              │
              ↓
┌────────────────────────────────────────────────────────┐
│  Cuando Usuario registra su clave pública:             │
│  1. Usuario envía publicKey                            │
│  2. Servidor FIRMA la clave con Server Private Key     │
│  3. Servidor guarda: publicKey + signature + timestamp │
└────────────────────────────────────────────────────────┘
              │
              ↓
┌────────────────────────────────────────────────────────┐
│  Cuando Usuario A pide clave de Usuario B:             │
│  1. Servidor envía: publicKey_B + signature            │
│  2. Cliente A verifica signature con Server Public Key │
│  3. Si válida → clave auténtica ✓                      │
│  4. Si inválida → rechazo, posible MITM ✗              │
└────────────────────────────────────────────────────────┘
```

---

## 🔧 Implementación

### 1. Nueva Entidad: Clave Pública Certificada

```java
@Entity
@Table(name = "user_public_keys")
public class UserPublicKey {
    
    // ...campos existentes...
    
    @Lob
    @Column(name = "server_signature", nullable = false)
    private byte[] serverSignature; // Firma del servidor sobre la clave pública
    
    @Column(name = "signature_algorithm")
    private String signatureAlgorithm; // "SHA256withRSA" o "SHA256withECDSA"
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt; // Cuándo se firmó
    
    // Getter/Setter
}
```

### 2. Servicio de Certificación

```java
@Service
public class KeyCertificationService {
    
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    
    @PostConstruct
    public void init() throws Exception {
        // Cargar o generar clave del servidor
        loadServerKeys();
    }
    
    /**
     * Carga las claves del servidor desde un KeyStore seguro
     */
    private void loadServerKeys() throws Exception {
        // En producción: cargar desde archivo protegido o HSM
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("server-signing-keys.p12")) {
            keyStore.load(fis, "server-password".toCharArray());
        }
        
        serverPrivateKey = (PrivateKey) keyStore.getKey(
            "server-signing", 
            "server-password".toCharArray()
        );
        
        Certificate cert = keyStore.getCertificate("server-signing");
        serverPublicKey = cert.getPublicKey();
    }
    
    /**
     * Firma una clave pública con la clave privada del servidor
     * 
     * Datos firmados:
     * userId + publicKey + algorithm + timestamp
     */
    public byte[] signPublicKey(
        Long userId,
        byte[] publicKeyBytes,
        String algorithm,
        LocalDateTime timestamp
    ) throws Exception {
        
        // Crear payload a firmar
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeLong(userId);
        dos.writeInt(publicKeyBytes.length);
        dos.write(publicKeyBytes);
        dos.writeUTF(algorithm);
        dos.writeLong(timestamp.toEpochSecond(ZoneOffset.UTC));
        
        byte[] dataToSign = baos.toByteArray();
        
        // Firmar con clave privada del servidor
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(serverPrivateKey);
        signature.update(dataToSign);
        
        return signature.sign();
    }
    
    /**
     * Verifica la firma del servidor sobre una clave pública
     */
    public boolean verifyPublicKeySignature(
        Long userId,
        byte[] publicKeyBytes,
        String algorithm,
        LocalDateTime timestamp,
        byte[] signatureBytes
    ) throws Exception {
        
        // Reconstruir payload
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        
        dos.writeLong(userId);
        dos.writeInt(publicKeyBytes.length);
        dos.write(publicKeyBytes);
        dos.writeUTF(algorithm);
        dos.writeLong(timestamp.toEpochSecond(ZoneOffset.UTC));
        
        byte[] dataToVerify = baos.toByteArray();
        
        // Verificar con clave pública del servidor
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(serverPublicKey);
        signature.update(dataToVerify);
        
        return signature.verify(signatureBytes);
    }
    
    /**
     * Obtiene la clave pública del servidor (para embeber en clientes)
     */
    public byte[] getServerPublicKey() {
        return serverPublicKey.getEncoded();
    }
}
```

### 3. Servicio Actualizado con Firma

```java
@Service
public class SharingService {
    
    private final KeyCertificationService certificationService;
    // ...otros servicios...
    
    /**
     * Registra clave pública Y LA FIRMA
     */
    @Transactional
    public void registerPublicKey(Long userId, RegisterPublicKeyRequest request) {
        byte[] publicKeyBytes = Base64.getDecoder().decode(request.publicKey());
        LocalDateTime now = LocalDateTime.now();
        
        // FIRMAR la clave pública con la clave del servidor
        byte[] signature = certificationService.signPublicKey(
            userId,
            publicKeyBytes,
            request.algorithm(),
            now
        );
        
        UserPublicKey userPublicKey = userPublicKeyRepository.findByUserId(userId)
            .orElse(new UserPublicKey());
        
        userPublicKey.setUserId(userId);
        userPublicKey.setPublicKey(publicKeyBytes);
        userPublicKey.setAlgorithm(request.algorithm());
        userPublicKey.setKeySize(request.keySize());
        userPublicKey.setServerSignature(signature);
        userPublicKey.setSignatureAlgorithm("SHA256withRSA");
        userPublicKey.setSignedAt(now);
        
        userPublicKeyRepository.save(userPublicKey);
    }
    
    /**
     * Obtiene clave pública CON FIRMA para verificación
     */
    public CertifiedPublicKeyResponse getUserPublicKey(Long userId) {
        UserPublicKey key = userPublicKeyRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no tiene clave pública"));
        
        return new CertifiedPublicKeyResponse(
            key.getUserId(),
            Base64.getEncoder().encodeToString(key.getPublicKey()),
            key.getAlgorithm(),
            key.getKeySize(),
            Base64.getEncoder().encodeToString(key.getServerSignature()),
            key.getSignatureAlgorithm(),
            key.getSignedAt()
        );
    }
}
```

### 4. DTO Actualizado

```java
public record CertifiedPublicKeyResponse(
    Long userId,
    String publicKey,           // Base64
    String algorithm,
    Integer keySize,
    String serverSignature,     // Base64 - NUEVA
    String signatureAlgorithm,  // "SHA256withRSA" - NUEVA
    LocalDateTime signedAt      // Timestamp - NUEVA
) {}
```

### 5. Endpoint para Obtener Clave Pública del Servidor

```java
@RestController
@RequestMapping("/api/sharing")
public class SharingController {
    
    private final KeyCertificationService certificationService;
    
    /**
     * Obtiene la clave pública del servidor para verificación
     * 
     * Los clientes deben embeber esta clave (hardcoded) o
     * obtenerla una vez y guardarla de forma segura.
     */
    @GetMapping("/server-public-key")
    public ResponseEntity<ServerPublicKeyResponse> getServerPublicKey() {
        byte[] publicKey = certificationService.getServerPublicKey();
        
        return ResponseEntity.ok(new ServerPublicKeyResponse(
            Base64.getEncoder().encodeToString(publicKey),
            "RSA",
            2048
        ));
    }
}

record ServerPublicKeyResponse(
    String publicKey,    // Base64
    String algorithm,
    Integer keySize
) {}
```

---

## 💻 Cliente: Verificación de Firma

### JavaScript (Web)

```javascript
class PublicKeyVerifier {
    
    constructor() {
        // Clave pública del servidor (EMBEBIDA en el código)
        // En producción: hardcoded o obtenida una vez y guardada
        this.serverPublicKeyBase64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA...";
        this.serverPublicKey = null;
    }
    
    /**
     * Inicializar: importar clave pública del servidor
     */
    async init() {
        const publicKeyBytes = Uint8Array.from(
            atob(this.serverPublicKeyBase64),
            c => c.charCodeAt(0)
        );
        
        this.serverPublicKey = await crypto.subtle.importKey(
            "spki",
            publicKeyBytes,
            {
                name: "RSASSA-PKCS1-v1_5",
                hash: "SHA-256"
            },
            false,
            ["verify"]
        );
    }
    
    /**
     * Obtiene y VERIFICA la clave pública de un usuario
     */
    async getVerifiedPublicKey(userId) {
        // 1. Obtener del servidor
        const response = await fetch(`/api/sharing/public-key/${userId}`);
        const data = await response.json();
        
        // 2. Verificar firma del servidor
        const isValid = await this.verifySignature(
            data.userId,
            data.publicKey,
            data.algorithm,
            data.signedAt,
            data.serverSignature
        );
        
        if (!isValid) {
            throw new Error("⚠️ ADVERTENCIA: Firma inválida! Posible ataque MITM.");
        }
        
        // 3. Si es válida, devolver clave
        console.log("✅ Clave pública verificada correctamente");
        return data.publicKey;
    }
    
    /**
     * Verifica la firma del servidor
     */
    async verifySignature(userId, publicKeyBase64, algorithm, signedAt, signatureBase64) {
        // Reconstruir payload (mismo orden que en el servidor)
        const publicKeyBytes = Uint8Array.from(atob(publicKeyBase64), c => c.charCodeAt(0));
        const timestamp = new Date(signedAt).getTime() / 1000;
        
        const payload = new ArrayBuffer(
            8 + 4 + publicKeyBytes.length + algorithm.length * 2 + 8
        );
        const view = new DataView(payload);
        let offset = 0;
        
        // userId (long, 8 bytes)
        view.setBigInt64(offset, BigInt(userId), false);
        offset += 8;
        
        // publicKey length (int, 4 bytes)
        view.setInt32(offset, publicKeyBytes.length, false);
        offset += 4;
        
        // publicKey bytes
        new Uint8Array(payload, offset, publicKeyBytes.length).set(publicKeyBytes);
        offset += publicKeyBytes.length;
        
        // algorithm (UTF-8 string length + bytes)
        const algorithmBytes = new TextEncoder().encode(algorithm);
        view.setInt16(offset, algorithmBytes.length, false);
        offset += 2;
        new Uint8Array(payload, offset, algorithmBytes.length).set(algorithmBytes);
        offset += algorithmBytes.length;
        
        // timestamp (long, 8 bytes)
        view.setBigInt64(offset, BigInt(Math.floor(timestamp)), false);
        
        // Verificar firma
        const signatureBytes = Uint8Array.from(atob(signatureBase64), c => c.charCodeAt(0));
        
        return await crypto.subtle.verify(
            "RSASSA-PKCS1-v1_5",
            this.serverPublicKey,
            signatureBytes,
            payload
        );
    }
}

// Uso
const verifier = new PublicKeyVerifier();
await verifier.init();

try {
    const userBPublicKey = await verifier.getVerifiedPublicKey(2);
    console.log("Clave verificada, seguro para usar");
    // Proceder a cifrar secreto con userBPublicKey
} catch (error) {
    console.error("ERROR:", error.message);
    alert("⚠️ No se pudo verificar la clave pública. NO compartir secreto.");
}
```

### Android (Kotlin)

```kotlin
class PublicKeyVerifier(context: Context) {
    
    // Clave pública del servidor (hardcoded en resources)
    private val serverPublicKeyBase64 = context.getString(R.string.server_public_key)
    private lateinit var serverPublicKey: PublicKey
    
    init {
        loadServerPublicKey()
    }
    
    private fun loadServerPublicKey() {
        val publicKeyBytes = Base64.decode(serverPublicKeyBase64, Base64.NO_WRAP)
        val keySpec = X509EncodedKeySpec(publicKeyBytes)
        val keyFactory = KeyFactory.getInstance("RSA")
        serverPublicKey = keyFactory.generatePublic(keySpec)
    }
    
    /**
     * Obtiene y verifica la clave pública de un usuario
     */
    suspend fun getVerifiedPublicKey(userId: Long): String {
        // 1. Obtener del servidor
        val response = sharingApi.getPublicKey(userId)
        
        // 2. Verificar firma
        val isValid = verifySignature(
            response.userId,
            response.publicKey,
            response.algorithm,
            response.signedAt,
            response.serverSignature
        )
        
        if (!isValid) {
            throw SecurityException("⚠️ Firma inválida! Posible ataque MITM.")
        }
        
        Log.d("Security", "✅ Clave pública verificada correctamente")
        return response.publicKey
    }
    
    private fun verifySignature(
        userId: Long,
        publicKeyBase64: String,
        algorithm: String,
        signedAt: String,
        signatureBase64: String
    ): Boolean {
        // Reconstruir payload
        val publicKeyBytes = Base64.decode(publicKeyBase64, Base64.NO_WRAP)
        val timestamp = Instant.parse(signedAt).epochSecond
        
        val baos = ByteArrayOutputStream()
        val dos = DataOutputStream(baos)
        
        dos.writeLong(userId)
        dos.writeInt(publicKeyBytes.size)
        dos.write(publicKeyBytes)
        dos.writeUTF(algorithm)
        dos.writeLong(timestamp)
        
        val dataToVerify = baos.toByteArray()
        
        // Verificar con clave pública del servidor
        val signature = Signature.getInstance("SHA256withRSA")
        signature.initVerify(serverPublicKey)
        signature.update(dataToVerify)
        
        val signatureBytes = Base64.decode(signatureBase64, Base64.NO_WRAP)
        return signature.verify(signatureBytes)
    }
}

// Uso en ViewModel
viewModelScope.launch {
    try {
        val verifiedPublicKey = keyVerifier.getVerifiedPublicKey(targetUserId)
        // Proceder a cifrar
    } catch (e: SecurityException) {
        _errorState.value = "⚠️ No se pudo verificar la autenticidad de la clave"
        // NO compartir el secreto
    }
}
```

---

## 🔐 Generación de Claves del Servidor

### Opción 1: OpenSSL (Producción)

```bash
# Generar clave privada RSA del servidor
openssl genrsa -out server-signing-private.pem 2048

# Generar clave pública
openssl rsa -in server-signing-private.pem -pubout -out server-signing-public.pem

# Convertir a PKCS12 para Java
openssl pkcs12 -export \
  -inkey server-signing-private.pem \
  -in server-signing-public.pem \
  -out server-signing-keys.p12 \
  -name server-signing \
  -password pass:super-secure-password
```

### Opción 2: Keytool (Java)

```bash
keytool -genkeypair \
  -alias server-signing \
  -keyalg RSA \
  -keysize 2048 \
  -keystore server-signing-keys.p12 \
  -storetype PKCS12 \
  -storepass super-secure-password \
  -dname "CN=VaultServer, O=MyCompany, C=ES" \
  -validity 3650
```

### Extraer Clave Pública para Clientes

```bash
# Exportar clave pública en Base64
keytool -exportcert \
  -alias server-signing \
  -keystore server-signing-keys.p12 \
  -rfc \
  -file server-public-key.pem

# El contenido de este archivo se HARDCODEA en los clientes
```

---

## 🛡️ Seguridad

### ✅ Protecciones Implementadas

1. **Autenticidad**: La firma prueba que la clave fue registrada en el servidor legítimo
2. **Integridad**: Cualquier modificación de la clave invalida la firma
3. **No repudio**: El servidor no puede negar haber firmado una clave
4. **Timestamp**: Previene ataques de replay con claves antiguas

### ⚠️ Consideraciones

| Aspecto | Solución |
|---------|----------|
| **Protección de clave privada del servidor** | HSM o KeyStore cifrado |
| **Rotación de claves del servidor** | Versionar firmas, múltiples claves públicas |
| **Distribución inicial de clave del servidor** | Hardcoded, Certificate Pinning, o TLS |
| **Revocación de claves** | Lista de revocación (CRL) o expiración |

### 🔄 Rotación de Claves del Servidor

```java
@Entity
class ServerSigningKey {
    Long id;
    byte[] publicKey;
    LocalDateTime validFrom;
    LocalDateTime validUntil;
    boolean revoked;
}

// Clientes verifican con múltiples claves si la actual falla
```

---

## 📊 Comparación de Opciones

| Enfoque | Seguridad | Complejidad | Escalabilidad |
|---------|-----------|-------------|---------------|
| **Sin verificación** | ❌ Vulnerable a MITM | Baja | Alta |
| **Firma del servidor** | ✅ Protege contra MITM | Media | Alta |
| **Certificados X.509 completos** | ✅✅ Máxima | Alta | Media |
| **Web of Trust (PGP)** | ✅ Descentralizada | Alta | Baja |

**Recomendación**: Firma del servidor (implementada aquí) para la mayoría de casos.

---

## ✨ Resumen

**¿Cómo verificar claves públicas?**

1. **Servidor firma cada clave pública** al registrarla
2. **Clientes embeben la clave pública del servidor**
3. **Al obtener una clave de usuario**, el cliente:
   - Recibe: publicKey + signature
   - Verifica signature con clave del servidor
   - Si válida → usar
   - Si inválida → rechazar (posible MITM)

**Implementación:**
- `KeyCertificationService.java` - Firma y verifica
- `CertifiedPublicKeyResponse.java` - DTO con firma
- Cliente JavaScript/Kotlin - Verificación

**Protección contra:**
- ✅ Man-in-the-Middle
- ✅ Sustitución de claves
- ✅ Ataques de suplantación

Esta es la misma técnica que usa **TLS con Certificate Pinning** y **Signal Protocol**.

