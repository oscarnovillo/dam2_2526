# Compartir Secretos Cifrados con Múltiples Usuarios

## 🎯 Problema

Un usuario quiere guardar información cifrada que puede compartir con múltiples personas. Cada persona autorizada debe poder descifrar el mensaje **N veces** (datos persistentes en base de datos).

## 🔐 Solución 1: RSA Híbrido (Tradicional)

### Arquitectura

```
┌─────────────────────────────────────────────────┐
│ Mensaje original: "Secreto compartido"         │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 1. Generar clave AES aleatoria (única)          │
│    aesKey = SecureRandom(256 bits)              │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 2. Cifrar mensaje con AES-GCM                   │
│    encryptedMessage = AES-GCM(aesKey, mensaje)  │
│    iv = random(12 bytes)                        │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 3. Cifrar clave AES para cada usuario           │
│    - Usuario A: RSA-OAEP(aesKey, pubKeyA)       │
│    - Usuario B: RSA-OAEP(aesKey, pubKeyB)       │
│    - Usuario C: RSA-OAEP(aesKey, pubKeyC)       │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Base de datos:                                  │
│ ┌─────────────────────────────────────────────┐ │
│ │ Secret {                                    │ │
│ │   encryptedData: "..." (1 copia)            │ │
│ │   iv: "..."                                 │ │
│ │ }                                           │ │
│ │                                             │ │
│ │ SharedWith {                                │ │
│ │   userId: A,                                │ │
│ │   encryptedAesKey: RSA(aesKey, pubA)        │ │
│ │ }                                           │ │
│ │ SharedWith {                                │ │
│ │   userId: B,                                │ │
│ │   encryptedAesKey: RSA(aesKey, pubB)        │ │
│ │ }                                           │ │
│ │ SharedWith {                                │ │
│ │   userId: C,                                │ │
│ │   encryptedAesKey: RSA(aesKey, pubC)        │ │
│ │ }                                           │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### Descifrado (Usuario B)

```
1. Obtener encryptedAesKey del usuario B
2. Descifrar: aesKey = RSA-OAEP-Decrypt(encryptedAesKey, privKeyB)
3. Descifrar: mensaje = AES-GCM-Decrypt(encryptedData, aesKey, iv)
```

### Ventajas ✅

1. **Eficiencia de almacenamiento**
   - Mensaje cifrado: 1 copia (independiente del número de usuarios)
   - Solo guardar N claves AES cifradas (256 bytes c/u con RSA-2048)

2. **Descifrado múltiple**
   - Cada usuario puede descifrar N veces sin problemas
   - La clave AES cifrada es reutilizable

3. **Agregar/Remover usuarios fácilmente**
   - Agregar: Cifrar aesKey con nueva clave pública
   - Remover: Eliminar entrada SharedWith
   - No requiere recifrar mensaje

4. **Estándar establecido**
   - Patrón usado en PGP, S/MIME
   - Bien probado y documentado

### Desventajas ❌

1. **Sin Forward Secrecy**
   - Si se compromete la clave privada RSA del usuario:
     - Puede descifrar todos los mensajes históricos
     - Incluidos los compartidos antes de la brecha

2. **Tamaño de claves**
   - RSA-2048: 256 bytes por encryptedAesKey
   - Para 100 usuarios: 25.6 KB solo en claves

3. **Rendimiento**
   - RSA es lento para cifrar/descifrar claves

---

## 🔐 Solución 2: ECIES (Claves Efímeras por Usuario)

### Arquitectura

```
┌─────────────────────────────────────────────────┐
│ Mensaje original: "Secreto compartido"         │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Para CADA usuario, generar cifrado ECIES:       │
│                                                 │
│ Usuario A:                                      │
│   1. Generar claves efímeras (rA, RA)           │
│   2. Secreto: SA = rA × pubKeyA                 │
│   3. aesKeyA = KDF(SA)                          │
│   4. encryptedA = AES-GCM(aesKeyA, mensaje)     │
│                                                 │
│ Usuario B:                                      │
│   1. Generar claves efímeras (rB, RB)           │
│   2. Secreto: SB = rB × pubKeyB                 │
│   3. aesKeyB = KDF(SB)                          │
│   4. encryptedB = AES-GCM(aesKeyB, mensaje)     │
│                                                 │
│ Usuario C:                                      │
│   1. Generar claves efímeras (rC, RC)           │
│   2. Secreto: SC = rC × pubKeyC                 │
│   3. aesKeyC = KDF(SC)                          │
│   4. encryptedC = AES-GCM(aesKeyC, mensaje)     │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Base de datos:                                  │
│ ┌─────────────────────────────────────────────┐ │
│ │ SharedWith {                                │ │
│ │   userId: A,                                │ │
│ │   encryptedData: encryptedA,                │ │
│ │   ephemeralPublicKey: RA,                   │ │
│ │   iv: ivA                                   │ │
│ │ }                                           │ │
│ │ SharedWith {                                │ │
│ │   userId: B,                                │ │
│ │   encryptedData: encryptedB,                │ │
│ │   ephemeralPublicKey: RB,                   │ │
│ │   iv: ivB                                   │ │
│ │ }                                           │ │
│ │ SharedWith {                                │ │
│ │   userId: C,                                │ │
│ │   encryptedData: encryptedC,                │ │
│ │   ephemeralPublicKey: RC,                   │ │
│ │   iv: ivC                                   │ │
│ │ }                                           │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### Descifrado (Usuario B)

```
1. Obtener ephemeralPublicKey (RB), encryptedData, iv
2. Secreto: SB = privKeyB × RB
3. aesKeyB = KDF(SB)
4. mensaje = AES-GCM-Decrypt(encryptedData, aesKeyB, iv)
```

### Ventajas ✅

1. **Forward Secrecy parcial**
   - Cada usuario tiene su propio cifrado con clave efímera
   - Si se compromete clave privada EC después:
     - Mensajes anteriores siguen seguros (clave efímera destruida)
   - ⚠️ PERO: la clave efímera está en BD para permitir descifrado múltiple

2. **Claves pequeñas**
   - EC-256: ephemeralPublicKey ~91 bytes
   - Para 100 usuarios: 9.1 KB (2.8x más pequeño que RSA)

3. **Aislamiento entre usuarios**
   - Cada usuario tiene cifrado completamente independiente
   - Comprometer cifrado de usuario A no afecta a B o C

4. **Rendimiento**
   - ECDH más rápido que RSA
   - 2-3x más rápido en cifrado

### Desventajas ❌

1. **Almacenamiento ineficiente**
   - Mensaje cifrado: N copias (una por usuario)
   - Para mensaje de 1MB y 100 usuarios: ~100MB
   - RSA híbrido: ~1MB + 25KB

2. **Forward Secrecy comprometido**
   - La clave efímera (R) se guarda en BD
   - No es verdadero Forward Secrecy
   - Si hackean la BD: pueden descifrar todo

3. **Complejidad al agregar usuarios**
   - Agregar nuevo usuario: Recifrar mensaje completo con nueva efímera
   - Más operaciones criptográficas

4. **No estándar para este caso de uso**
   - ECIES diseñado para mensajes efímeros (uno-a-uno)
   - No para datos persistentes compartidos

---

## 🔐 Solución 3: ECIES Híbrido (Mejor de ambos mundos)

### Arquitectura

```
┌─────────────────────────────────────────────────┐
│ 1. Generar clave AES aleatoria (única)          │
│    aesKey = SecureRandom(256 bits)              │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 2. Cifrar mensaje con AES-GCM (1 vez)           │
│    encryptedMessage = AES-GCM(aesKey, mensaje)  │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ 3. Para cada usuario, cifrar aesKey con ECIES:  │
│                                                 │
│ Usuario A:                                      │
│   - Generar efímera (rA, RA)                    │
│   - S = rA × pubKeyA                            │
│   - keyEncKey = KDF(S)                          │
│   - encAesKeyA = AES(keyEncKey, aesKey)         │
│                                                 │
│ Usuario B:                                      │
│   - Similar...                                  │
└─────────────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────────────┐
│ Base de datos:                                  │
│ ┌─────────────────────────────────────────────┐ │
│ │ Secret {                                    │ │
│ │   encryptedData: "..." (1 copia)            │ │
│ │   iv: "..."                                 │ │
│ │ }                                           │ │
│ │                                             │ │
│ │ SharedWith {                                │ │
│ │   userId: A,                                │ │
│ │   ephemeralPublicKey: RA,                   │ │
│ │   encryptedAesKey: "...",                   │ │
│ │   keyIv: "..."                              │ │
│ │ }                                           │ │
│ │ ... (usuarios B, C, etc.)                   │ │
│ └─────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────┘
```

### Ventajas ✅

1. **Mejor almacenamiento**
   - Mensaje: 1 copia
   - Por usuario: ~123 bytes (ephemeralKey + encAesKey + iv)

2. **Claves EC más pequeñas**
   - ephemeralPublicKey: ~91 bytes
   - vs RSA: ~256 bytes

3. **Descifrado múltiple**
   - Cada usuario puede descifrar N veces

4. **Aislamiento parcial**
   - Cada usuario tiene su propia clave de cifrado de clave

### Desventajas ❌

1. **Complejidad aumentada**
   - Dos niveles de cifrado
   - Más puntos de fallo

2. **Sin Forward Secrecy real**
   - Clave efímera en BD
   - Similar a RSA híbrido

---

## 📊 Comparación Completa

| Característica | RSA Híbrido | ECIES Puro | ECIES Híbrido |
|----------------|-------------|------------|---------------|
| **Mensaje cifrado** | 1 copia | N copias | 1 copia |
| **Tamaño por usuario** | ~256 bytes | ~mensaje + 91 bytes | ~123 bytes |
| **Forward Secrecy** | ❌ No | ❌ No (efímera en BD) | ❌ No |
| **Descifrado múltiple** | ✅ Sí | ✅ Sí | ✅ Sí |
| **Agregar usuario** | Fácil (cifrar 1 clave) | Difícil (recifrar mensaje) | Fácil (cifrar 1 clave) |
| **Aislamiento** | Medio | Alto | Medio-Alto |
| **Rendimiento cifrado** | Lento (RSA) | Rápido (EC) | Rápido (EC) |
| **Estándar** | ✅ PGP, S/MIME | ❌ No para este caso | ⚠️ Custom |
| **100 usuarios** | 1MB + 25KB | ~100MB | 1MB + 12KB |

---

## 🎯 Recomendación

### Para compartir secretos persistentes: **RSA Híbrido (Solución 1)**

**Razones:**

1. ✅ **Eficiencia**: 1 copia del mensaje cifrado
2. ✅ **Estándar**: Patrón probado (PGP, S/MIME)
3. ✅ **Escalabilidad**: Agregar 1000 usuarios = solo 256KB extra
4. ✅ **Simplicidad**: Implementación directa

**Cuándo usar ECIES:**
- ✅ Si necesitas claves más pequeñas (móviles, IoT)
- ✅ Si tienes pocos usuarios (< 10)
- ✅ Si el mensaje es pequeño (< 1KB)

---

## 🔐 Sobre Forward Secrecy

### ⚠️ Importante: Forward Secrecy NO es posible con datos persistentes

```
Forward Secrecy requiere:
1. Clave efímera generada
2. Usar la clave
3. DESTRUIR la clave

Pero si necesitas descifrar N veces:
- No puedes destruir la clave
- Debe guardarse en BD
- Forward Secrecy se pierde
```

### Soluciones alternativas para mejor seguridad:

1. **Re-cifrado periódico**
   ```
   - Cada mes: generar nueva clave AES
   - Recifrar mensaje
   - Destruir clave antigua
   - Limita ventana de compromiso
   ```

2. **Separación de claves**
   ```
   - Clave de datos: solo en memoria del usuario
   - Clave de wrapping: en servidor
   - Requiere ambas para descifrar
   ```

3. **HSM (Hardware Security Module)**
   ```
   - Claves nunca salen del hardware
   - Descifrado dentro del HSM
   - Protección física
   ```

---

## 💾 Ejemplo de Esquema de Base de Datos

### Opción Recomendada (RSA Híbrido)

```sql
-- Tabla de secretos
CREATE TABLE secrets (
    id UUID PRIMARY KEY,
    owner_user_id UUID NOT NULL,
    encrypted_data BYTEA NOT NULL,  -- Mensaje cifrado con AES (1 copia)
    iv BYTEA NOT NULL,               -- IV para AES-GCM
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Tabla de compartidos
CREATE TABLE secret_shares (
    id UUID PRIMARY KEY,
    secret_id UUID REFERENCES secrets(id),
    shared_with_user_id UUID NOT NULL,
    encrypted_aes_key BYTEA NOT NULL,  -- Clave AES cifrada con RSA/EC pública
    algorithm VARCHAR(20),              -- 'RSA-OAEP' o 'ECIES'
    
    -- Si usas ECIES para cifrar la clave AES:
    ephemeral_public_key BYTEA,        -- Solo si algorithm='ECIES'
    key_iv BYTEA,                      -- IV para cifrar clave AES
    
    shared_at TIMESTAMP,
    UNIQUE(secret_id, shared_with_user_id)
);

-- Índices
CREATE INDEX idx_secret_shares_user ON secret_shares(shared_with_user_id);
CREATE INDEX idx_secret_shares_secret ON secret_shares(secret_id);
```

### Query para descifrar (Usuario B)

```sql
-- 1. Obtener secreto y clave cifrada
SELECT 
    s.encrypted_data,
    s.iv,
    ss.encrypted_aes_key,
    ss.algorithm,
    ss.ephemeral_public_key,
    ss.key_iv
FROM secrets s
JOIN secret_shares ss ON s.id = ss.secret_id
WHERE ss.shared_with_user_id = :userId
  AND s.id = :secretId;

-- 2. En aplicación:
if (algorithm == 'RSA-OAEP') {
    aesKey = RSA.decrypt(encrypted_aes_key, userPrivateKey);
} else if (algorithm == 'ECIES') {
    sharedSecret = ECDH(userPrivateKey, ephemeral_public_key);
    keyEncKey = KDF(sharedSecret);
    aesKey = AES.decrypt(encrypted_aes_key, keyEncKey, key_iv);
}
message = AES.decrypt(encrypted_data, aesKey, iv);
```

---

## 🔄 Comparación con sistemas reales

### PGP/GPG (Email cifrado)
```
✅ Usa RSA Híbrido
- 1 copia del mensaje cifrado
- N claves de sesión cifradas
- Estándar: RFC 4880
```

### Signal Protocol
```
✅ Usa ECIES con Double Ratchet
- Pero para mensajes EFÍMEROS
- Claves se destruyen tras lectura
- Forward Secrecy real
```

### Google Drive Cifrado
```
✅ Usa AES con wrapping keys
- Similar a RSA Híbrido
- DEK (Data Encryption Key) cifrada
- KEK (Key Encryption Key) por usuario
```

---

## 🎓 Conclusión

Para compartir secretos **persistentes** con múltiples usuarios:

**🏆 Ganador: RSA Híbrido (o EC Híbrido)**

**Por qué:**
1. ✅ 1 copia del mensaje (eficiente)
2. ✅ N claves pequeñas cifradas
3. ✅ Estándar probado
4. ✅ Fácil agregar/remover usuarios
5. ✅ Forward Secrecy no es posible de todas formas (datos persistentes)

**ECIES puro solo si:**
- Pocos usuarios (< 5)
- Mensajes pequeños (< 10KB)
- Necesitas aislamiento total entre usuarios

**La diferencia de seguridad es mínima** porque:
- Ninguno tiene Forward Secrecy real (datos persistentes)
- Ambos dependen de proteger claves privadas
- Ambos usan AES-GCM para datos

**La diferencia de eficiencia es ENORME** con muchos usuarios:
- RSA híbrido: O(N) en claves, O(1) en datos
- ECIES puro: O(N) en todo


