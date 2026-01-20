# ECDH (Elliptic Curve Diffie-Hellman) y Generación de Claves AES

## ¿Qué es ECDH?

**ECDH** (Elliptic Curve Diffie-Hellman) es un protocolo de **acuerdo de claves** basado en criptografía de curva elíptica que permite a dos partes establecer un **secreto compartido** a través de un canal inseguro.

### Características Principales

- **No es encriptación**: ECDH es un mecanismo de acuerdo de claves, no de cifrado
- **Basado en curvas elípticas**: Utiliza la matemática de curvas elípticas (más eficiente que DH tradicional)
- **Secreto compartido**: Ambas partes generan el mismo secreto sin transmitirlo
- **Seguridad**: Un atacante que observe la comunicación no puede obtener el secreto compartido

## ¿Cómo Funciona ECDH?

### Proceso Básico

1. **Alice** y **Bob** acuerdan usar la misma curva elíptica (ej: secp256r1)

2. **Generación de claves**:
   - Alice genera un par de claves: `(privateKeyA, publicKeyA)`
   - Bob genera un par de claves: `(privateKeyB, publicKeyB)`

3. **Intercambio de claves públicas**:
   - Alice envía `publicKeyA` a Bob (puede ser interceptada, no es problema)
   - Bob envía `publicKeyB` a Alice (puede ser interceptada, no es problema)

4. **Cálculo del secreto compartido**:
   - Alice calcula: `sharedSecret = ECDH(privateKeyA, publicKeyB)`
   - Bob calcula: `sharedSecret = ECDH(privateKeyB, publicKeyA)`
   - **Ambos obtienen el mismo valor** gracias a las propiedades matemáticas de las curvas elípticas

### Propiedades Matemáticas

```
sharedSecret_Alice = privateKeyA × publicKeyB
                   = privateKeyA × (privateKeyB × G)
                   = (privateKeyA × privateKeyB) × G

sharedSecret_Bob = privateKeyB × publicKeyA
                 = privateKeyB × (privateKeyA × G)
                 = (privateKeyB × privateKeyA) × G

Donde G es el punto generador de la curva elíptica
```

Como la multiplicación es conmutativa: `privateKeyA × privateKeyB = privateKeyB × privateKeyA`

## De ECDH a AES: Derivación de Claves

El secreto compartido generado por ECDH **no se usa directamente como clave AES**. En su lugar, se pasa por una **Función de Derivación de Claves (KDF)**.

### ¿Por Qué Usar KDF?

1. **Formato adecuado**: El secreto ECDH puede no tener el tamaño exacto necesario (128, 192 o 256 bits para AES)
2. **Distribución uniforme**: KDF garantiza que los bits estén uniformemente distribuidos
3. **Múltiples claves**: Se pueden derivar varias claves del mismo secreto (clave de cifrado, clave de MAC, IV, etc.)

### KDFs Comunes

#### 1. **HKDF (HMAC-based Key Derivation Function)** - RFC 5869

```java
// Usar HKDF para derivar clave AES-256
SecretKey sharedSecret = keyAgreement.generateSecret("AES");
// O manualmente con HKDF:
byte[] salt = new byte[16]; // Idealmente aleatorio
SecureRandom.getInstanceStrong().nextBytes(salt);

// Extraer
Mac hmac = Mac.getInstance("HmacSHA256");
hmac.init(new SecretKeySpec(salt, "HmacSHA256"));
byte[] prk = hmac.doFinal(sharedSecretBytes);

// Expandir (simplificado)
hmac.init(new SecretKeySpec(prk, "HmacSHA256"));
hmac.update(new byte[]{0x01}); // Info + counter
byte[] aesKeyBytes = Arrays.copyOf(hmac.doFinal(), 32); // 256 bits
SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
```

#### 2. **SHA-256 Simple** (menos recomendado pero funcional)

```java
MessageDigest digest = MessageDigest.getInstance("SHA-256");
byte[] hash = digest.digest(sharedSecretBytes);
SecretKey aesKey = new SecretKeySpec(hash, "AES"); // 256 bits
```

#### 3. **PBKDF2** (normalmente para passwords, pero también válido)

```java
SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
KeySpec spec = new PBEKeySpec(
    new String(sharedSecretBytes).toCharArray(),
    salt,
    65536, // iteraciones
    256    // longitud de la clave
);
SecretKey aesKey = factory.generateSecret(spec);
```

## Ejemplo Completo: ECDH + AES

### Paso 1: Generación de Pares de Claves EC

```java
KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
kpg.initialize(ecSpec);

KeyPair aliceKeyPair = kpg.generateKeyPair();
KeyPair bobKeyPair = kpg.generateKeyPair();
```

### Paso 2: Alice Genera el Secreto Compartido

```java
KeyAgreement aliceKA = KeyAgreement.getInstance("ECDH");
aliceKA.init(aliceKeyPair.getPrivate());
aliceKA.doPhase(bobKeyPair.getPublic(), true);

// Derivar directamente a AES
SecretKey aliceAesKey = aliceKA.generateSecret("AES");

// O obtener bytes y usar KDF
byte[] aliceSharedSecret = aliceKA.generateSecret();
```

### Paso 3: Bob Genera el Mismo Secreto

```java
KeyAgreement bobKA = KeyAgreement.getInstance("ECDH");
bobKA.init(bobKeyPair.getPrivate());
bobKA.doPhase(aliceKeyPair.getPublic(), true);

SecretKey bobAesKey = bobKA.generateSecret("AES");

// aliceAesKey == bobAesKey ✓
```

### Paso 4: Usar la Clave AES para Cifrar

```java
// Alice cifra
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
byte[] iv = new byte[12];
SecureRandom.getInstanceStrong().nextBytes(iv);
GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv);

cipher.init(Cipher.ENCRYPT_MODE, aliceAesKey, gcmSpec);
byte[] ciphertext = cipher.doFinal("Mensaje secreto".getBytes());

// Bob descifra (usando la misma clave derivada)
cipher.init(Cipher.DECRYPT_MODE, bobAesKey, gcmSpec);
byte[] plaintext = cipher.doFinal(ciphertext);
```

## Ventajas de ECDH + AES

| Aspecto | Ventaja |
|---------|---------|
| **Eficiencia** | EC usa claves más cortas que RSA para el mismo nivel de seguridad |
| **Perfect Forward Secrecy** | Si se generan nuevos pares efímeros por sesión, comprometer una clave no afecta sesiones pasadas |
| **Velocidad** | AES es mucho más rápido que RSA para cifrar datos grandes |
| **Flexibilidad** | Se puede derivar múltiples claves del mismo secreto compartido |

## Comparación: ECDH vs RSA Key Exchange

| Característica | ECDH | RSA |
|----------------|------|-----|
| **Tipo** | Acuerdo de claves | Cifrado de claves |
| **Tamaño de clave (128-bit security)** | 256 bits | 3072 bits |
| **Perfect Forward Secrecy** | Sí (con claves efímeras) | No (típicamente) |
| **Velocidad** | Muy rápida | Más lenta |
| **Uso típico** | TLS 1.3, Signal Protocol | TLS 1.2 (legacy) |

## Curvas Elípticas Recomendadas

### NIST Curves (ampliamente soportadas)

- **secp256r1** (P-256) - 128 bits de seguridad
- **secp384r1** (P-384) - 192 bits de seguridad
- **secp521r1** (P-521) - 256 bits de seguridad

### Modern Curves (mejores propiedades)

- **Curve25519** (X25519 para ECDH) - 128 bits, muy rápida
- **Curve448** (X448 para ECDH) - 224 bits

```java
// Usar Curve25519 (requiere Java 11+ o BouncyCastle)
KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
KeyPair keyPair = kpg.generateKeyPair();

KeyAgreement ka = KeyAgreement.getInstance("X25519");
// ... mismo proceso
```

## Seguridad y Consideraciones

### ✅ Buenas Prácticas

1. **Usar claves efímeras**: Generar nuevos pares por sesión
2. **Autenticación**: ECDH solo proporciona secreto compartido, no autenticación
   - Combinar con firmas digitales (ECDSA)
   - O usar esquemas autenticados como ECIES
3. **KDF robusta**: Usar HKDF en lugar de SHA simple
4. **Validar claves públicas**: Verificar que están en la curva
5. **Salt aleatorio**: Si se usa KDF con salt

### ⚠️ Riesgos

- **Man-in-the-Middle**: Sin autenticación, un atacante puede interceptar y reemplazar claves públicas
- **Curvas débiles**: Algunas curvas tienen vulnerabilidades conocidas
- **Implementación**: Errores en la implementación pueden comprometer la seguridad

## Ejemplo Práctico: Protocolo Híbrido Completo

```java
public class SecureMessaging {
    
    // 1. Setup inicial
    public KeyPair generateEphemeralKeyPair() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        kpg.initialize(new ECGenParameterSpec("secp256r1"));
        return kpg.generateKeyPair();
    }
    
    // 2. Emisor: Cifrar mensaje
    public EncryptedMessage encrypt(String message, PublicKey recipientPublicKey) 
            throws Exception {
        // Generar par efímero
        KeyPair ephemeralKeyPair = generateEphemeralKeyPair();
        
        // ECDH para derivar clave AES
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(ephemeralKeyPair.getPrivate());
        ka.doPhase(recipientPublicKey, true);
        
        // Derivar clave AES (usando SHA-256 como KDF simple)
        byte[] sharedSecret = ka.generateSecret();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        
        // Cifrar con AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, aesKey, new GCMParameterSpec(128, iv));
        byte[] ciphertext = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
        
        // Retornar: clave pública efímera + IV + ciphertext
        return new EncryptedMessage(
            ephemeralKeyPair.getPublic().getEncoded(),
            iv,
            ciphertext
        );
    }
    
    // 3. Receptor: Descifrar mensaje
    public String decrypt(EncryptedMessage encrypted, PrivateKey recipientPrivateKey) 
            throws Exception {
        // Reconstruir clave pública efímera
        KeyFactory kf = KeyFactory.getInstance("EC");
        PublicKey ephemeralPublicKey = kf.generatePublic(
            new X509EncodedKeySpec(encrypted.ephemeralPublicKey)
        );
        
        // ECDH para derivar la misma clave AES
        KeyAgreement ka = KeyAgreement.getInstance("ECDH");
        ka.init(recipientPrivateKey);
        ka.doPhase(ephemeralPublicKey, true);
        
        byte[] sharedSecret = ka.generateSecret();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        SecretKey aesKey = new SecretKeySpec(aesKeyBytes, "AES");
        
        // Descifrar con AES-GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, aesKey, 
            new GCMParameterSpec(128, encrypted.iv));
        byte[] plaintext = cipher.doFinal(encrypted.ciphertext);
        
        return new String(plaintext, StandardCharsets.UTF_8);
    }
    
    record EncryptedMessage(byte[] ephemeralPublicKey, byte[] iv, byte[] ciphertext) {}
}
```

## Uso en Protocolos Reales

### TLS 1.3: Handshake Completo con ECDHE

**Sí, tanto el CLIENTE como el SERVIDOR generan pares de claves EC efímeras.** Así es como funciona:

#### El Proceso Paso a Paso

```
CLIENTE                                    SERVIDOR
-------                                    --------

1. Genera par EC efímero
   (clientPrivate, clientPublic)
   
2. ClientHello + clientPublic     -------->

                                            3. Genera par EC efímero
                                               (serverPrivate, serverPublic)
                                            
                                            4. Calcula sharedSecret:
                                               ECDH(serverPrivate, clientPublic)
                                            
                                            5. Deriva claves de sesión con KDF
                                            
                                  <--------  ServerHello + serverPublic
                                             + [Encrypted Extensions]
                                             + Certificate
                                             + CertificateVerify
                                             + Finished

6. Calcula sharedSecret:
   ECDH(clientPrivate, serverPublic)
   
7. Deriva las MISMAS claves de sesión

8. [Application Data]            <------->  [Application Data]
   (cifrado con AES-GCM)                    (cifrado con AES-GCM)
```

#### Características Clave de TLS 1.3

1. **Ambos generan claves EC efímeras**:
   - El cliente genera un par EC (típicamente X25519 o P-256)
   - El servidor también genera su propio par EC
   - Estas claves son **únicas por sesión**

2. **Perfect Forward Secrecy (PFS)**:
   - Las claves efímeras se descartan después de la sesión
   - Si un atacante compromete la clave privada del servidor en el futuro, **no puede descifrar tráfico pasado**
   - Cada sesión tiene claves completamente independientes

3. **Derivación de múltiples claves**:
   ```
   sharedSecret (ECDH)
       ↓
   HKDF-Extract (con salt)
       ↓
   HKDF-Expand
       ↓
   ┌────────────────────────────────┐
   │ client_write_key (AES)         │
   │ server_write_key (AES)         │
   │ client_write_iv                │
   │ server_write_iv                │
   └────────────────────────────────┘
   ```

4. **No se usa RSA para intercambio de claves**:
   - En TLS 1.2: RSA podía usarse para cifrar una clave pre-master
   - En TLS 1.3: **SOLO** ECDHE (o DHE clásico)
   - RSA solo se usa para **firmar** (autenticación), no para cifrar claves

#### Ejemplo Simplificado en Java

```java
// CLIENTE
KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
kpg.initialize(new ECGenParameterSpec("secp256r1"));
KeyPair clientKeyPair = kpg.generateKeyPair();

// Enviar clientKeyPair.getPublic() al servidor
// Recibir serverPublicKey del servidor

KeyAgreement clientKA = KeyAgreement.getInstance("ECDH");
clientKA.init(clientKeyPair.getPrivate());
clientKA.doPhase(serverPublicKey, true);
byte[] clientSharedSecret = clientKA.generateSecret();

// Derivar claves AES con HKDF
SecretKey clientWriteKey = deriveKey(clientSharedSecret, "c ap traffic", ...);
SecretKey serverWriteKey = deriveKey(clientSharedSecret, "s ap traffic", ...);

// -----------------------------------------------------------

// SERVIDOR (hace lo mismo pero al revés)
KeyPair serverKeyPair = kpg.generateKeyPair();

// Recibir clientPublicKey del cliente
// Enviar serverKeyPair.getPublic() al cliente

KeyAgreement serverKA = KeyAgreement.getInstance("ECDH");
serverKA.init(serverKeyPair.getPrivate());
serverKA.doPhase(clientPublicKey, true);
byte[] serverSharedSecret = serverKA.generateSecret();

// Deriva las MISMAS claves
SecretKey clientWriteKey = deriveKey(serverSharedSecret, "c ap traffic", ...);
SecretKey serverWriteKey = deriveKey(serverSharedSecret, "s ap traffic", ...);
```

#### ¿Por Qué Dos Claves AES Diferentes?

TLS 1.3 deriva **dos claves AES separadas** del secreto compartido:

- **client_write_key**: Solo el cliente usa esta para cifrar datos que envía
- **server_write_key**: Solo el servidor usa esta para cifrar datos que envía

**Razón**: Evita ataques de reflexión y facilita análisis de seguridad.

#### Comparación TLS 1.2 vs TLS 1.3

| Aspecto | TLS 1.2 | TLS 1.3 |
|---------|---------|---------|
| **Intercambio de claves** | RSA o ECDHE (opcional) | SOLO ECDHE/DHE |
| **Perfect Forward Secrecy** | Opcional (solo con ECDHE) | Obligatorio |
| **Round trips** | 2-RTT | 1-RTT (0-RTT con resumption) |
| **Cifrados permitidos** | Muchos (algunos inseguros) | Solo AEAD (GCM, ChaCha20-Poly1305) |
| **Claves efímeras** | Opcional | Obligatorio |

### Signal Protocol (Mensajería End-to-End)

- **Múltiples capas de ECDH**:
  - Identity keys (a largo plazo)
  - Prekeys firmadas
  - Ephemeral keys (por mensaje)
- Usa X25519 para todos los acuerdos de claves
- Double Ratchet combina ECDH con derivación de claves para forward/backward secrecy

### Noise Protocol Framework

- Patrones flexibles de handshake usando ECDH
- Usado en **WireGuard VPN**
- Permite diferentes configuraciones (NK, KK, XX, IK, etc.)
- Altamente eficiente y verificable formalmente

## ECDH con Certificados (Claves Estáticas)

### ¿Se Puede Usar ECDH con Certificados?

**Sí, se puede**, pero hay diferencias importantes respecto al uso de claves efímeras:

| Aspecto | Claves Efímeras (ECDHE) | Claves Estáticas (ECDH) |
|---------|-------------------------|-------------------------|
| **Perfect Forward Secrecy** | ✅ Sí | ❌ No |
| **Rendimiento** | Genera nuevas claves cada vez | Más rápido (claves pre-generadas) |
| **Uso típico** | TLS 1.3, mensajería moderna | Legacy, sistemas embebidos |
| **Seguridad a largo plazo** | ✅ Alta | ⚠️ Menor (si se compromete la clave) |

### Escenario: Cliente y Servidor con Certificados EC

Imagina que tanto el cliente como el servidor tienen certificados X.509 con pares de claves EC:

```
SERVIDOR:
- Certificado: server.crt (con publicKeyServer)
- Clave privada: server.key (privateKeyServer)
- Curva: secp256r1

CLIENTE:
- Certificado: client.crt (con publicKeyClient)
- Clave privada: client.key (privateKeyClient)
- Curva: secp256r1
```

### Implementación en Java

#### 1. Cargar Certificados y Claves del Servidor

```java
public class ECDHServerWithCertificate {
    
    private PrivateKey serverPrivateKey;
    private PublicKey serverPublicKey;
    
    public void loadServerKeys() throws Exception {
        // Cargar KeyStore del servidor
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("server.p12")) {
            keyStore.load(fis, "password".toCharArray());
        }
        
        // Obtener par de claves del certificado
        String alias = "server-ec";
        serverPrivateKey = (PrivateKey) keyStore.getKey(alias, "password".toCharArray());
        Certificate cert = keyStore.getCertificate(alias);
        serverPublicKey = cert.getPublicKey();
        
        System.out.println("Servidor - Algoritmo: " + serverPublicKey.getAlgorithm()); // EC
    }
    
    public SecretKey deriveSharedSecret(PublicKey clientPublicKey) throws Exception {
        // Validar que sea EC
        if (!(clientPublicKey instanceof java.security.interfaces.ECPublicKey)) {
            throw new IllegalArgumentException("La clave pública del cliente debe ser EC");
        }
        
        // ECDH con clave estática del servidor
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(serverPrivateKey);
        keyAgreement.doPhase(clientPublicKey, true);
        
        // Derivar clave AES-256
        byte[] sharedSecret = keyAgreement.generateSecret();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        
        return new SecretKeySpec(aesKeyBytes, "AES");
    }
}
```

#### 2. Cargar Certificados y Claves del Cliente

```java
public class ECDHClientWithCertificate {
    
    private PrivateKey clientPrivateKey;
    private PublicKey clientPublicKey;
    
    public void loadClientKeys() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream("client.p12")) {
            keyStore.load(fis, "password".toCharArray());
        }
        
        String alias = "client-ec";
        clientPrivateKey = (PrivateKey) keyStore.getKey(alias, "password".toCharArray());
        Certificate cert = keyStore.getCertificate(alias);
        clientPublicKey = cert.getPublicKey();
    }
    
    public SecretKey deriveSharedSecret(PublicKey serverPublicKey) throws Exception {
        KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH");
        keyAgreement.init(clientPrivateKey);
        keyAgreement.doPhase(serverPublicKey, true);
        
        byte[] sharedSecret = keyAgreement.generateSecret();
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] aesKeyBytes = digest.digest(sharedSecret);
        
        return new SecretKeySpec(aesKeyBytes, "AES");
    }
}
```

#### 3. Protocolo Completo con Autenticación Mutua

```java
public class ECDHStaticProtocol {
    
    public static void main(String[] args) throws Exception {
        // SERVIDOR: Cargar claves del certificado
        ECDHServerWithCertificate server = new ECDHServerWithCertificate();
        server.loadServerKeys();
        
        // CLIENTE: Cargar claves del certificado
        ECDHClientWithCertificate client = new ECDHClientWithCertificate();
        client.loadClientKeys();
        
        // 1. Cliente envía su certificado (clave pública) al servidor
        PublicKey clientPublicKey = client.clientPublicKey;
        
        // 2. Servidor envía su certificado (clave pública) al cliente
        PublicKey serverPublicKey = server.serverPublicKey;
        
        // 3. Ambos derivan el MISMO secreto compartido
        SecretKey serverAesKey = server.deriveSharedSecret(clientPublicKey);
        SecretKey clientAesKey = client.deriveSharedSecret(serverPublicKey);
        
        // 4. Verificar que son iguales
        boolean keysMatch = Arrays.equals(
            serverAesKey.getEncoded(), 
            clientAesKey.getEncoded()
        );
        System.out.println("¿Claves coinciden? " + keysMatch); // true
        
        // 5. Usar para cifrado AES
        String mensaje = "Mensaje seguro con ECDH estático";
        byte[] encrypted = encryptAES(mensaje, serverAesKey);
        String decrypted = decryptAES(encrypted, clientAesKey);
        
        System.out.println("Original:    " + mensaje);
        System.out.println("Descifrado:  " + decrypted);
    }
    
    private static byte[] encryptAES(String data, SecretKey key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12];
        SecureRandom.getInstanceStrong().nextBytes(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        
        byte[] ciphertext = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
        
        // Concatenar IV + ciphertext para transmitir
        byte[] result = new byte[iv.length + ciphertext.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);
        return result;
    }
    
    private static String decryptAES(byte[] data, SecretKey key) throws Exception {
        // Separar IV + ciphertext
        byte[] iv = Arrays.copyOfRange(data, 0, 12);
        byte[] ciphertext = Arrays.copyOfRange(data, 12, data.length);
        
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] plaintext = cipher.doFinal(ciphertext);
        
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
```

### Generar Certificados EC para Pruebas

Puedes generar certificados EC autofirmados con OpenSSL:

```bash
# 1. Generar clave privada EC del servidor
openssl ecparam -name secp256r1 -genkey -noout -out server-key.pem

# 2. Crear certificado autofirmado del servidor
openssl req -new -x509 -key server-key.pem -out server-cert.pem -days 365 \
  -subj "/CN=Server/O=MyOrg/C=ES"

# 3. Convertir a PKCS12 (para Java)
openssl pkcs12 -export -in server-cert.pem -inkey server-key.pem \
  -out server.p12 -name "server-ec" -password pass:password

# Repetir para el cliente
openssl ecparam -name secp256r1 -genkey -noout -out client-key.pem
openssl req -new -x509 -key client-key.pem -out client-cert.pem -days 365 \
  -subj "/CN=Client/O=MyOrg/C=ES"
openssl pkcs12 -export -in client-cert.pem -inkey client-key.pem \
  -out client.p12 -name "client-ec" -password pass:password
```

### ⚠️ Problema: Sin Perfect Forward Secrecy

Con claves estáticas, si un atacante:

1. **Graba todo el tráfico cifrado** (cliente ↔ servidor)
2. **Años después compromete** la clave privada del servidor (o del cliente)
3. **Puede descifrar TODO el tráfico pasado**

**¿Por qué?**
- Las claves del certificado no cambian
- El secreto compartido SIEMPRE es el mismo entre el mismo par cliente-servidor
- No hay renovación de claves por sesión

### Solución Híbrida: Claves Estáticas + Efímeras

La mejor práctica es **combinar ambas**:

```java
public class HybridECDH {
    
    public SecretKey deriveSessionKey(
            PrivateKey myStaticPrivateKey,      // Del certificado
            PublicKey theirStaticPublicKey,     // Del certificado
            PrivateKey myEphemeralPrivateKey,   // Generada para esta sesión
            PublicKey theirEphemeralPublicKey   // Generada para esta sesión
    ) throws Exception {
        
        // 1. ECDH con claves estáticas (autenticación)
        KeyAgreement ka1 = KeyAgreement.getInstance("ECDH");
        ka1.init(myStaticPrivateKey);
        ka1.doPhase(theirStaticPublicKey, true);
        byte[] staticSecret = ka1.generateSecret();
        
        // 2. ECDH con claves efímeras (forward secrecy)
        KeyAgreement ka2 = KeyAgreement.getInstance("ECDH");
        ka2.init(myEphemeralPrivateKey);
        ka2.doPhase(theirEphemeralPublicKey, true);
        byte[] ephemeralSecret = ka2.generateSecret();
        
        // 3. Combinar ambos secretos con HKDF
        byte[] combinedSecret = new byte[staticSecret.length + ephemeralSecret.length];
        System.arraycopy(staticSecret, 0, combinedSecret, 0, staticSecret.length);
        System.arraycopy(ephemeralSecret, 0, combinedSecret, staticSecret.length, ephemeralSecret.length);
        
        // 4. Derivar clave de sesión
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] sessionKeyBytes = digest.digest(combinedSecret);
        
        return new SecretKeySpec(sessionKeyBytes, "AES");
    }
}
```

**Ventajas del enfoque híbrido:**
- ✅ **Autenticación**: Las claves estáticas prueban la identidad
- ✅ **Forward Secrecy**: Las claves efímeras protegen sesiones pasadas
- ✅ **Seguridad en profundidad**: Combina ambos mecanismos

### Comparación: TLS 1.2 con Autenticación de Cliente

En TLS 1.2 con autenticación mutua de certificados:

1. **Certificados EC/RSA** se usan para **autenticación** (firmas digitales)
2. **ECDHE** se usa para **acuerdo de claves** (forward secrecy)
3. **No se usa ECDH estático** directamente

```
Cliente                                  Servidor
-------                                  --------
ClientHello + 
  claves EC efímeras        -------->
                                        ServerHello + 
                                          claves EC efímeras
                                        Certificate (RSA/EC) ← Autenticación
                                        ServerKeyExchange (ECDHE)
                                        CertificateRequest
                            <--------   ServerHelloDone

Certificate (RSA/EC)        -------->   ← Cliente se autentica
ClientKeyExchange (ECDHE)
CertificateVerify (firma)   -------->   ← Prueba que tiene la clave privada

[Derivación de claves de sesión con ECDHE]
[Comunicación cifrada con AES]
```

**Nota importante**: Los certificados EC/RSA se usan para **firmar**, no para ECDH directo.

### Certificados HTTPS Reales: ¿RSA o EC?

#### La Realidad Actual (2026)

**Respuesta corta**: La mayoría de servidores grandes como Google, Facebook, Cloudflare **usan ambos**, con preferencia creciente por **ECDSA (EC)**.

#### ¿Qué Tienen los Certificados de Google?

Vamos a comprobarlo. Puedes verificar esto tú mismo:

```bash
# Opción 1: Con OpenSSL
echo | openssl s_client -connect google.com:443 -showcerts 2>/dev/null | openssl x509 -noout -text | grep "Public Key Algorithm"

# Opción 2: Ver en el navegador
# Chrome: Candado → Certificate → Details → "Public Key Info"
```

**Resultado típico en 2026**:
- **google.com**: ECDSA con P-256 (secp256r1)
- **youtube.com**: ECDSA con P-256
- **cloudflare.com**: ECDSA con P-256
- **facebook.com**: ECDSA con P-256

#### Evolución Histórica

| Período | Tipo de Certificado | Motivo |
|---------|---------------------|--------|
| **1990-2010** | RSA 1024-2048 bits | Único estándar ampliamente soportado |
| **2010-2016** | RSA 2048-4096 bits | Mejora de seguridad, EC aún no adoptado |
| **2016-2020** | RSA + ECDSA dual | Transición: servidores sirven ambos |
| **2020-ahora** | ECDSA primario, RSA fallback | EC preferido, RSA para compatibilidad |

#### ¿Por Qué Algunos Usan RSA y Otros ECDSA?

**Ventajas de ECDSA (EC) para Certificados:**

```
Certificado RSA-2048:          ~1200 bytes
Certificado ECDSA-P256:        ~320 bytes
                               ↓
                         75% más pequeño!
```

| Aspecto | RSA | ECDSA (EC) |
|---------|-----|------------|
| **Tamaño de certificado** | Grande (1-3 KB) | Pequeño (300-500 bytes) |
| **Velocidad de verificación** | Lenta | 10x más rápida |
| **Latencia TLS handshake** | Mayor | Menor (importante en móviles) |
| **Compatibilidad** | Universal (incluso IE6) | Moderna (IE11+, Android 4.4+) |
| **Generación de firma** | Lenta | Muy rápida |

#### Dual Certificates: La Estrategia Real

**Google, Cloudflare y otros grandes usan certificados DUALES:**

```
Servidor HTTPS Moderno
  ├── Certificado ECDSA (P-256) ← Preferido para clientes modernos
  └── Certificado RSA (2048)    ← Fallback para clientes antiguos
  
Cliente conecta:
  - Si soporta ECDSA → Usa certificado ECDSA
  - Si solo soporta RSA → Usa certificado RSA
```

**Ejemplo de configuración Nginx:**

```nginx
server {
    listen 443 ssl;
    server_name ejemplo.com;
    
    # Certificado ECDSA (preferido)
    ssl_certificate     /path/to/ecdsa-cert.pem;
    ssl_certificate_key /path/to/ecdsa-key.pem;
    
    # Certificado RSA (fallback)
    ssl_certificate     /path/to/rsa-cert.pem;
    ssl_certificate_key /path/to/rsa-key.pem;
    
    # El servidor elige automáticamente según el cliente
}
```

#### ¿Cómo Verificarlo en Vivo?

**Ejemplo en Java para Ver el Certificado de un Servidor:**

```java
import javax.net.ssl.*;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class CheckServerCertificate {
    
    public static void main(String[] args) throws Exception {
        String host = "www.google.com";
        int port = 443;
        
        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket socket = (SSLSocket) factory.createSocket(host, port);
        
        socket.startHandshake();
        
        Certificate[] certs = socket.getSession().getPeerCertificates();
        X509Certificate cert = (X509Certificate) certs[0];
        
        System.out.println("Host: " + host);
        System.out.println("Algoritmo de clave pública: " + cert.getPublicKey().getAlgorithm());
        System.out.println("Algoritmo de firma: " + cert.getSigAlgName());
        System.out.println("Tamaño de clave: " + getKeySize(cert.getPublicKey()));
        
        socket.close();
    }
    
    private static String getKeySize(java.security.PublicKey key) {
        if (key instanceof java.security.interfaces.RSAPublicKey) {
            return ((java.security.interfaces.RSAPublicKey) key).getModulus().bitLength() + " bits";
        } else if (key instanceof java.security.interfaces.ECPublicKey) {
            int fieldSize = ((java.security.interfaces.ECPublicKey) key)
                .getParams().getCurve().getField().getFieldSize();
            return fieldSize + " bits (EC)";
        }
        return "Desconocido";
    }
}
```

**Salida típica para Google (2026):**
```
Host: www.google.com
Algoritmo de clave pública: EC
Algoritmo de firma: SHA256withECDSA
Tamaño de clave: 256 bits (EC)
```

#### Resumen del Uso de Certificados en TLS 1.3

```
┌─────────────────────────────────────────────┐
│  Handshake TLS 1.3 (Google.com)             │
├─────────────────────────────────────────────┤
│                                             │
│  1. ECDHE (X25519)                          │
│     ↓                                       │
│     Genera secreto compartido               │
│     ↓                                       │
│     Deriva claves AES-GCM                   │
│                                             │
│  2. Certificado ECDSA (P-256)               │
│     ↓                                       │
│     Firma con ECDSA la transcripción        │
│     ↓                                       │
│     Cliente verifica la firma               │
│     ↓                                       │
│     Autenticación del servidor ✓            │
│                                             │
└─────────────────────────────────────────────┘

Resultado:
- Intercambio de claves: ECDHE (efímero) → Forward Secrecy
- Autenticación: ECDSA (del certificado) → Identidad verificada
- Cifrado de datos: AES-256-GCM → Confidencialidad
```

#### ¿Por Qué No Todo el Mundo Usa EC?

**Razones para seguir usando RSA:**

1. **Compatibilidad**: Dispositivos antiguos (Android <4.4, Windows XP)
2. **Hardware legacy**: Algunos HSMs solo soportan RSA
3. **Procesos internos**: Empresas con infraestructura PKI antigua
4. **Regulaciones**: Algunas certificaciones gubernamentales especifican RSA

**Pero la tendencia es clara**: ECDSA está ganando.

#### Estadísticas Reales (2025-2026)

Según datos de [SSL Pulse](https://www.ssllabs.com/ssl-pulse/):

| Tipo de Certificado | % de Sitios Top 100,000 |
|---------------------|-------------------------|
| RSA solo | ~35% |
| ECDSA solo | ~25% |
| Dual (RSA + ECDSA) | ~40% |

**Tendencia**: +15% ECDSA año tras año.

#### Recomendación Práctica para tu Servidor

**Configuración óptima en 2026:**

```
✅ Certificado ECDSA (P-256) como primario
✅ Certificado RSA (2048) como fallback (opcional)
✅ TLS 1.3 con ECDHE obligatorio
✅ Cipher suites: TLS_AES_256_GCM_SHA384, TLS_CHACHA20_POLY1305_SHA256
```

**Spring Boot application.properties:**

```properties
# TLS 1.3 con certificado ECDSA
server.ssl.key-store=classpath:keystore-ecdsa.p12
server.ssl.key-store-password=changeit
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=server-ecdsa

# Protocolos
server.ssl.enabled-protocols=TLSv1.3,TLSv1.2

# Cipher suites (ECDSA primero)
server.ssl.ciphers=TLS_AES_256_GCM_SHA384,TLS_CHACHA20_POLY1305_SHA256,TLS_AES_128_GCM_SHA256
```

**Generar certificado ECDSA autofirmado para pruebas:**

```bash
# Con OpenSSL (más control)
openssl ecparam -name prime256v1 -genkey -noout -out server-key.pem
openssl req -new -x509 -key server-key.pem -out server-cert.pem -days 365 \
  -subj "/CN=localhost/O=MyCompany/C=ES"
openssl pkcs12 -export -in server-cert.pem -inkey server-key.pem \
  -out keystore-ecdsa.p12 -name server-ecdsa -password pass:changeit

# O con keytool (más simple, Java 11+)
keytool -genkeypair -alias server-ecdsa -keyalg EC -groupname secp256r1 \
  -keystore keystore-ecdsa.p12 -storetype PKCS12 -storepass changeit \
  -dname "CN=localhost, O=MyCompany, C=ES" -validity 365
```

### Cuándo Usar Cada Enfoque

| Escenario | Recomendación |
|-----------|---------------|
| **Internet público (HTTPS)** | ECDHE puro (TLS 1.3) |
| **Sistemas legacy** | Híbrido (estático + efímero) |
| **Dispositivos IoT con recursos limitados** | ECDH estático (si PFS no es crítico) |
| **Comunicación P2P autenticada** | Híbrido o ECDHE + firmas |
| **Sistemas de alta seguridad** | ECDHE + certificados para autenticación |

## Resumen

**ECDH** es un mecanismo elegante para establecer un secreto compartido que luego se usa para **derivar claves simétricas (AES)**. Esta combinación ofrece:

- ✅ **Eficiencia**: Claves pequeñas + cifrado rápido
- ✅ **Seguridad**: Basada en problemas matemáticos difíciles
- ✅ **Forward Secrecy**: Con claves efímeras
- ✅ **Escalabilidad**: No requiere infraestructura PKI compleja

Es la base de la criptografía moderna en internet (HTTPS, mensajería segura, VPNs).

