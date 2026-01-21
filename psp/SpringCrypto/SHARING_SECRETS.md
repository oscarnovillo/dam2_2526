# 🔐 Compartir Secretos entre Usuarios

## El Problema

Tienes un sistema de caja fuerte donde cada usuario cifra sus datos con su password (AES + PBKDF2). **¿Cómo compartir un secreto con otro usuario sin comprometer la seguridad?**

## ❌ Lo que NO funciona

### Opción Mala 1: Compartir la Password
```
Usuario A → password123 → Usuario B
```
**Problemas:**
- B tiene acceso TOTAL a TODOS los secretos de A
- A no puede revocar acceso sin cambiar su password
- Si B es comprometido, todos los datos de A están expuestos

### Opción Mala 2: Re-cifrar con Password de B
```
A descifra con su password
A cifra con password de B
A envía al servidor
```
**Problemas:**
- A necesita conocer la password de B (inseguro)
- Zero-knowledge se rompe (el servidor podría ver la password de B)

## ✅ La Solución: Criptografía Asimétrica

### Concepto: Cada Usuario Tiene un Par de Claves

```
Usuario A:
  ├─ Password (para sus secretos AES)
  ├─ Clave Privada RSA/EC (solo en su dispositivo)
  └─ Clave Pública RSA/EC (en el servidor)

Usuario B:
  ├─ Password (para sus secretos AES)
  ├─ Clave Privada RSA/EC (solo en su dispositivo)
  └─ Clave Pública RSA/EC (en el servidor)
```

### Flujo Completo de Compartición

```
┌──────────────────────────────────────────────────────────────┐
│  PASO 1: SETUP (Una vez por usuario)                         │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Usuario A (cliente):                                        │
│  1. Genera par RSA: (privateKeyA, publicKeyA)                │
│  2. Guarda privateKeyA en Keystore local (nunca sale)        │
│  3. POST /api/sharing/public-key { publicKeyA }              │
│                                                               │
│  Usuario B (cliente):                                        │
│  1. Genera par RSA: (privateKeyB, publicKeyB)                │
│  2. Guarda privateKeyB en Keystore local (nunca sale)        │
│  3. POST /api/sharing/public-key { publicKeyB }              │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  PASO 2: COMPARTIR (Usuario A comparte con Usuario B)       │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Usuario A (cliente):                                        │
│  1. Descifra su secreto con su password (AES)                │
│     plaintext = AES.decrypt(encrypted, password_A)           │
│                                                               │
│  2. Obtiene clave pública de B del servidor                  │
│     GET /api/sharing/public-key/userB                        │
│                                                               │
│  3. Cifra el secreto con publicKeyB (RSA)                    │
│     encrypted_for_B = RSA.encrypt(plaintext, publicKeyB)     │
│                                                               │
│  4. Envía al servidor                                        │
│     POST /api/sharing/share {                                │
│       secretId: 1,                                           │
│       sharedWithUserId: B,                                   │
│       encryptedData: encrypted_for_B,                        │
│       permission: "READ"                                     │
│     }                                                         │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  PASO 3: ACCEDER (Usuario B accede al secreto compartido)   │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Usuario B (cliente):                                        │
│  1. Obtiene secretos compartidos con él                      │
│     GET /api/sharing/shared-with-me                          │
│                                                               │
│  2. Descarga el secreto específico                           │
│     GET /api/sharing/shares/1                                │
│     → { encryptedData: encrypted_for_B }                     │
│                                                               │
│  3. Descifra con su clave privada (RSA)                      │
│     plaintext = RSA.decrypt(encrypted_for_B, privateKeyB)    │
│                                                               │
│  4. Muestra el secreto al usuario                            │
│                                                               │
└──────────────────────────────────────────────────────────────┘

┌──────────────────────────────────────────────────────────────┐
│  PASO 4: REVOCAR (Usuario A revoca acceso de B)             │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  Usuario A:                                                  │
│  DELETE /api/sharing/revoke/secretId/userB                   │
│                                                               │
│  Ahora B ya NO puede obtener el secreto cifrado del servidor│
│  (Aunque si ya lo descargó antes, lo tiene)                  │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

## 🗂️ Estructura de Datos

### Base de Datos

#### Tabla: `vault_secrets`
```sql
id | user_id | encrypted_data | iv | salt
1  | 1       | 0xABC...      | ...| ...
```
Secretos personales cifrados con AES (password del usuario)

#### Tabla: `user_public_keys`
```sql
id | user_id | public_key     | algorithm | key_size
1  | 1       | 0xMII...       | RSA       | 2048
2  | 2       | 0xMII...       | RSA       | 2048
```
Claves públicas de cada usuario (para que otros cifren)

#### Tabla: `shared_secrets`
```sql
id | secret_id | owner_id | shared_with_id | encrypted_secret_key | permission | expires_at
1  | 1         | 1        | 2              | 0xRSA...            | READ       | 2026-02-01
```
Secretos compartidos (cifrados con la clave pública del receptor)

## 🔑 Permisos

### READ (Solo Lectura)
- El usuario puede descifrar y ver el secreto
- No puede modificarlo

### READ_WRITE (Lectura y Escritura)
- Puede ver y modificar
- **Implementación futura:** Necesitaría re-cifrar con la clave del owner

## 🕐 Expiración

```java
// Compartir por 7 días
{
  "expiresInDays": 7
}
```

El servidor verifica:
```java
if (expiresAt != null && LocalDateTime.now().isAfter(expiresAt)) {
    throw new RuntimeException("El acceso ha expirado");
}
```

## 🔒 Seguridad

### ✅ Protecciones

1. **Claves privadas nunca salen del dispositivo**
   - Web: Guardadas en IndexedDB (cifradas)
   - Android: Android Keystore (hardware-backed)
   - iOS: Keychain

2. **Zero-Knowledge del contenido**
   - El servidor solo ve datos cifrados
   - No puede leer los secretos compartidos

3. **Revocación de acceso**
   - El owner puede revocar en cualquier momento
   - El servidor borra el registro

4. **Expiración automática**
   - Compartidos temporales
   - Se invalidan automáticamente

### ⚠️ Limitaciones

1. **Metadatos visibles**
   - El servidor sabe QUIÉN comparte con QUIÉN
   - Sabe CUÁNDO se compartió
   - Similar a Signal/WhatsApp

2. **Forward Secrecy limitada**
   - Si B descarga el secreto, revocar no lo borra de su dispositivo
   - B podría haber hecho copia

3. **Confianza en el receptor**
   - Una vez compartido, B puede copiarlo
   - No hay DRM que lo evite

## 🎯 Casos de Uso

### Caso 1: Equipo de Trabajo
```
Manager crea secreto con credenciales de producción
→ Comparte con Developer A (READ)
→ Comparte con Developer B (READ)
→ Comparte con DevOps (READ_WRITE)

Cuando Developer A sale del equipo:
→ Manager revoca acceso de Developer A
```

### Caso 2: Compartir Contraseña Temporal
```
Usuario A comparte password WiFi con Usuario B
→ expiresInDays: 1 (expira mañana)

Automáticamente revocado después de 24h
```

### Caso 3: Familia
```
Padre comparte clave de la caja fuerte con Madre
→ permission: READ_WRITE

Ambos pueden ver y actualizar
```

## 💻 Implementación en el Cliente

### JavaScript (Web Crypto API)

#### 1. Generar Par de Claves

```javascript
// Generar par RSA
const keyPair = await crypto.subtle.generateKey(
  {
    name: "RSA-OAEP",
    modulusLength: 2048,
    publicExponent: new Uint8Array([1, 0, 1]),
    hash: "SHA-256"
  },
  true,  // extractable
  ["encrypt", "decrypt"]
);

// Exportar clave pública (para enviar al servidor)
const publicKeySpki = await crypto.subtle.exportKey("spki", keyPair.publicKey);
const publicKeyBase64 = btoa(String.fromCharCode(...new Uint8Array(publicKeySpki)));

// Guardar clave privada localmente (cifrada con password del usuario)
const privateKeyPkcs8 = await crypto.subtle.exportKey("pkcs8", keyPair.privateKey);
const encryptedPrivateKey = await encryptWithPassword(privateKeyPkcs8, userPassword);
localStorage.setItem('encryptedPrivateKey', encryptedPrivateKey);

// Registrar clave pública en servidor
await fetch('/api/sharing/public-key', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    publicKey: publicKeyBase64,
    algorithm: 'RSA',
    keySize: 2048
  })
});
```

#### 2. Compartir Secreto

```javascript
async function shareSecret(secretId, targetUserId, myPassword) {
  // 1. Descifrar mi secreto con mi password (AES)
  const mySecret = await getAndDecryptSecret(secretId, myPassword);
  
  // 2. Obtener clave pública del receptor
  const response = await fetch(`/api/sharing/public-key/${targetUserId}`);
  const { publicKey: publicKeyBase64 } = await response.json();
  
  // 3. Importar clave pública
  const publicKeyBytes = Uint8Array.from(atob(publicKeyBase64), c => c.charCodeAt(0));
  const recipientPublicKey = await crypto.subtle.importKey(
    "spki",
    publicKeyBytes,
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["encrypt"]
  );
  
  // 4. Cifrar secreto con clave pública del receptor
  const encoder = new TextEncoder();
  const encryptedForRecipient = await crypto.subtle.encrypt(
    { name: "RSA-OAEP" },
    recipientPublicKey,
    encoder.encode(mySecret)
  );
  
  const encryptedBase64 = btoa(String.fromCharCode(...new Uint8Array(encryptedForRecipient)));
  
  // 5. Enviar al servidor
  await fetch('/api/sharing/share', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      secretId: secretId,
      sharedWithUserId: targetUserId,
      encryptedData: encryptedBase64,
      permission: 'READ',
      algorithm: 'RSA',
      expiresInDays: 7
    })
  });
}
```

#### 3. Acceder a Secreto Compartido

```javascript
async function accessSharedSecret(shareId, myPassword) {
  // 1. Obtener secreto compartido del servidor
  const response = await fetch(`/api/sharing/shares/${shareId}`);
  const { encryptedData } = await response.json();
  
  // 2. Recuperar mi clave privada (cifrada localmente)
  const encryptedPrivateKey = localStorage.getItem('encryptedPrivateKey');
  const privateKeyPkcs8 = await decryptWithPassword(encryptedPrivateKey, myPassword);
  
  // 3. Importar clave privada
  const myPrivateKey = await crypto.subtle.importKey(
    "pkcs8",
    privateKeyPkcs8,
    { name: "RSA-OAEP", hash: "SHA-256" },
    false,
    ["decrypt"]
  );
  
  // 4. Descifrar con mi clave privada
  const encryptedBytes = Uint8Array.from(atob(encryptedData), c => c.charCodeAt(0));
  const decrypted = await crypto.subtle.decrypt(
    { name: "RSA-OAEP" },
    myPrivateKey,
    encryptedBytes
  );
  
  const decoder = new TextDecoder();
  const secretData = decoder.decode(decrypted);
  
  return secretData;
}
```

### Android (Kotlin)

```kotlin
class SharingCryptoManager {
    
    // Generar par de claves RSA
    fun generateKeyPair(): KeyPair {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        return keyGen.generateKeyPair()
    }
    
    // Guardar clave privada en Android Keystore
    fun savePrivateKey(alias: String, privateKey: PrivateKey) {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)
        
        val entry = KeyStore.PrivateKeyEntry(
            privateKey,
            arrayOf() // No certificate chain needed
        )
        
        keyStore.setEntry(
            alias,
            entry,
            KeyProtection.Builder(KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true) // Requiere biometría
                .build()
        )
    }
    
    // Cifrar con clave pública del receptor
    fun encryptForRecipient(data: String, recipientPublicKey: PublicKey): ByteArray {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, recipientPublicKey)
        return cipher.doFinal(data.toByteArray())
    }
    
    // Descifrar con mi clave privada
    fun decryptSharedSecret(encrypted: ByteArray, myPrivateKey: PrivateKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, myPrivateKey)
        val decrypted = cipher.doFinal(encrypted)
        return String(decrypted)
    }
}
```

## 📊 Comparación: RSA vs EC para Compartir

| Aspecto | RSA-2048 | EC P-256 |
|---------|----------|----------|
| **Tamaño de clave pública** | ~300 bytes | ~65 bytes |
| **Velocidad de cifrado** | Lenta | Rápida |
| **Velocidad de descifrado** | Muy lenta | Rápida |
| **Compatibilidad** | Universal | Moderna (Java 11+) |
| **Recomendación** | OK para pocos usuarios | Mejor para muchos usuarios |

## 🔮 Mejoras Futuras

### 1. Grupos de Compartición

```java
@Entity
class SharingGroup {
    Long id;
    String name;
    Long ownerId;
    List<Long> memberIds;
}
```

Compartir con grupo en vez de usuario individual.

### 2. Claves Efímeras (Perfect Forward Secrecy)

```
Generar nuevo par de claves por cada compartido
Borrar después de usar
```

### 3. Auditoría Completa

```java
@Entity
class ShareAuditLog {
    Long shareId;
    String action; // SHARED, ACCESSED, REVOKED
    LocalDateTime timestamp;
    String ipAddress;
}
```

### 4. Notificaciones

```
Cuando alguien accede a un secreto compartido contigo
→ Enviar notificación push
```

## ✨ Resumen

**¿Cómo compartir secretos?**

1. **Setup**: Cada usuario genera par RSA/EC
   - Clave privada: Solo en su dispositivo
   - Clave pública: En el servidor

2. **Compartir**: 
   - Descifrar con AES (password)
   - Cifrar con RSA (clave pública del receptor)
   - Enviar al servidor

3. **Acceder**:
   - Obtener del servidor (cifrado con RSA)
   - Descifrar con clave privada

4. **Revocar**:
   - Owner puede eliminar el compartido
   - Receptor pierde acceso futuro

**Ventajas:**
- ✅ Zero-knowledge del contenido
- ✅ Revocación de acceso
- ✅ Expiración automática
- ✅ Permisos granulares

**Trade-offs:**
- ⚠️ Metadatos visibles (quién con quién)
- ⚠️ Forward secrecy limitado
- ⚠️ Complejidad mayor (dos capas crypto)

---

**Implementación completa lista en:**
- `SharingController.java`
- `SharingService.java`
- `api-tests-sharing.http`

