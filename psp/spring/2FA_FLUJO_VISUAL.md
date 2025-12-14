# 🔐 Flujo Visual de 2FA con TOTP

## 📊 Diagrama de Secuencia - Activación de 2FA

```
Usuario                 Frontend                Backend                    Base de Datos            Google Authenticator
  |                        |                       |                              |                           |
  |------ Login -----------|                       |                              |                           |
  |                        |------ POST /login --->|                              |                           |
  |                        |                       |---- Verificar usuario ------>|                           |
  |                        |                       |<---- Usuario encontrado -----|                           |
  |                        |<----- Session OK -----|                              |                           |
  |<--- Login exitoso -----|                       |                              |                           |
  |                        |                       |                              |                           |
  |-- Activar 2FA ---------|                       |                              |                           |
  |                        |-- POST /2fa/enable -->|                              |                           |
  |                        |                       |-- Generar secreto aleatorio  |                           |
  |                        |                       |   (ej: "JBSWY3DPEHPK3PXP")  |                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Generar QR con secreto     |                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Guardar secreto (temp) --->|                           |
  |                        |                       |   two_factor_enabled=false   |                           |
  |                        |<-- Secreto + QR ------|                              |                           |
  |<-- Mostrar QR ---------|                       |                              |                           |
  |                        |                       |                              |                           |
  |-- Escanear QR con app ---------------------------------------------------------------------------------------------------------------->|
  |                        |                       |                              |                           |-- Guardar secreto localmente
  |                        |                       |                              |                           |-- Generar código 123456
  |<-- Ver código 123456 --------------------------------------------------------------------------------------------------|
  |                        |                       |                              |                           |
  |-- Introducir 123456 ---|                       |                              |                           |
  |                        |-- POST /2fa/confirm ->|                              |                           |
  |                        |   { code: "123456" }  |                              |                           |
  |                        |                       |-- Verificar código con       |                           |
  |                        |                       |   secreto guardado           |                           |
  |                        |                       |   (algoritmo HMAC-SHA1)      |                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Activar 2FA -------------->|                           |
  |                        |                       |   two_factor_enabled=true    |                           |
  |                        |<-- 2FA activado ------|                              |                           |
  |<-- Confirmación -------|                       |                              |                           |
```

## 📊 Diagrama de Secuencia - Login con 2FA

```
Usuario                 Frontend                Backend                    Base de Datos            Google Authenticator
  |                        |                       |                              |                           |
  |-- Login --------------|                       |                              |                           |
  |                        |-- POST /login ------->|                              |                           |
  |                        |   { user, pass }      |                              |                           |
  |                        |                       |---- Verificar credenciales ->|                           |
  |                        |                       |<---- Usuario + 2FA=true -----|                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Guardar en sesión temp:    |                           |
  |                        |                       |   pendingTwoFactorUsername   |                           |
  |                        |<-- requiresTwoFactor --|                              |                           |
  |                        |    = true             |                              |                           |
  |<-- Pedir código 2FA ---|                       |                              |                           |
  |                        |                       |                              |                           |
  |-- Ver código actual ------------------------------------------------------------------------------------------------------------------>|
  |                        |                       |                              |                           |-- Calcular código actual
  |                        |                       |                              |                           |   tiempo_actual / 30 = bucket
  |                        |                       |                              |                           |   HMAC-SHA1(secreto, bucket)
  |<-- Código 789012 ---------------------------------------------------------------------------------------------------------|
  |                        |                       |                              |                           |
  |-- Introducir 789012 ---|                       |                              |                           |
  |                        |-- POST /2fa/verify -->|                              |                           |
  |                        |   { user, code }      |                              |                           |
  |                        |                       |-- Verificar sesión temp      |                           |
  |                        |                       |   pendingTwoFactorUsername   |                           |
  |                        |                       |                              |                           |
  |                        |                       |---- Obtener secreto -------->|                           |
  |                        |                       |<---- secreto "JBSWY3..." ----|                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Calcular código esperado   |                           |
  |                        |                       |   con mismo algoritmo:       |                           |
  |                        |                       |   tiempo_actual / 30 = bucket|                           |
  |                        |                       |   HMAC-SHA1(secreto, bucket) |                           |
  |                        |                       |   = 789012 ✓                 |                           |
  |                        |                       |                              |                           |
  |                        |                       |-- Código válido! Crear sesión|                           |
  |                        |                       |   completa con usuarioId     |                           |
  |                        |<-- Login exitoso -----|                              |                           |
  |<-- Dashboard ----------|                       |                              |                           |
```

## 🔐 ¿Cómo se genera el código TOTP?

### Paso a paso del algoritmo:

1. **Obtener tiempo actual**
   ```
   Tiempo actual: 1638360000 segundos (desde Unix epoch)
   ```

2. **Dividir en intervalos de 30 segundos (buckets)**
   ```
   Bucket = floor(1638360000 / 30) = 54612000
   ```

3. **Aplicar HMAC-SHA1**
   ```
   Hash = HMAC-SHA1(secreto_en_base32, bucket_en_bytes)
   Hash = [0x1f, 0x86, 0x98, 0x69, 0x0e, 0x02, 0xca, ...]
   ```

4. **Extraer 6 dígitos dinámicamente (Dynamic Truncation)**
   ```
   offset = último_byte & 0x0F = 5
   código = hash[offset:offset+4] & 0x7FFFFFFF
   código = código % 1,000,000
   código = 123456
   ```

5. **Ventana de tolerancia**
   - Se aceptan códigos de bucket actual ± 1
   - Esto da ~90 segundos de ventana total
   - Compensa desfases de reloj pequeños

## 📱 ¿Qué guarda cada componente?

### Backend (Base de Datos)
```sql
usuarios:
  id: 1
  username: "admin"
  two_factor_enabled: true
  two_factor_secret: "JBSWY3DPEHPK3PXP"  ← Secreto compartido
```

### Google Authenticator
```
Cuenta: admin @ MiAplicacion
Secreto: JBSWY3DPEHPK3PXP  ← Mismo secreto
Algoritmo: SHA1
Dígitos: 6
Periodo: 30 segundos
```

### ¿Por qué funciona?
**Ambos tienen el mismo secreto y el mismo tiempo** → Generan el mismo código

## 🕐 Ejemplo real con tiempos

```
Hora actual: 10:30:15
Bucket: floor(tiempo / 30) = 123456

Backend calcula:  HMAC-SHA1("JBSWY3DPEHPK3PXP", 123456) = 789012
App calcula:      HMAC-SHA1("JBSWY3DPEHPK3PXP", 123456) = 789012
                                                             ✓ Match!

30 segundos después...

Hora actual: 10:30:45
Bucket: floor(tiempo / 30) = 123457  ← Cambió!

Backend calcula:  HMAC-SHA1("JBSWY3DPEHPK3PXP", 123457) = 456789
App calcula:      HMAC-SHA1("JBSWY3DPEHPK3PXP", 123457) = 456789
                                                             ✓ Código nuevo!
```

## 🔒 ¿Por qué es seguro?

### ✅ Fortalezas:

1. **No se transmite el secreto** después de la configuración inicial (QR)
2. **Códigos de un solo uso** - Cada código dura 30 segundos
3. **Imposible adivinar** - 1 millón de combinaciones, 30 segundos por intento
4. **Funciona offline** - No requiere conexión a Internet
5. **Resistente a replay attacks** - Códigos viejos no sirven
6. **Estándar abierto** - RFC 6238, auditado por expertos

### ⚠️ Vulnerabilidades (y mitigaciones):

1. **Phishing del código TOTP**
   - Atacante crea sitio falso que pide username+password+código
   - Mitigación: Educar usuarios, usar WebAuthn/FIDO2 para casos críticos

2. **Pérdida del móvil**
   - Usuario pierde acceso a la app autenticadora
   - Mitigación: Códigos de respaldo, recuperación por email/SMS

3. **Malware en el móvil**
   - Malware podría leer los secretos de la app
   - Mitigación: Usar dispositivos de seguridad dedicados (YubiKey)

4. **Desfase de reloj > 90 segundos**
   - Si el reloj está muy mal, los códigos no coinciden
   - Mitigación: NTP obligatorio, ventanas de tolerancia mayores

## 🆚 Comparación con otros métodos

| Método          | Seguridad | UX  | Offline | Costo |
|-----------------|-----------|-----|---------|-------|
| TOTP            | ⭐⭐⭐⭐    | ⭐⭐⭐ | ✅      | 💰 Gratis |
| SMS             | ⭐⭐       | ⭐⭐⭐⭐ | ❌      | 💰💰 Por SMS |
| Email           | ⭐⭐       | ⭐⭐⭐ | ❌      | 💰 Gratis |
| Push            | ⭐⭐⭐⭐    | ⭐⭐⭐⭐⭐ | ❌      | 💰💰💰 App propia |
| WebAuthn/FIDO2  | ⭐⭐⭐⭐⭐  | ⭐⭐⭐⭐ | ✅      | 💰💰 Hardware |

## 🎯 Casos de uso reales

### ¿Quién usa TOTP?

- **GitHub** - Protege cuentas de desarrolladores
- **Google** - Opcional para cuentas de Gmail
- **AWS** - Requerido para usuarios root
- **Binance** - Obligatorio para retiros de criptomonedas
- **Microsoft 365** - Opcional para empresas
- **Discord** - Opcional para todas las cuentas

Es el estándar de facto para 2FA en la industria tech.

## 📚 Formato del URI del QR

```
otpauth://totp/MiAplicacion:admin?secret=JBSWY3DPEHPK3PXP&issuer=MiAplicacion
```

Partes:
- `otpauth://totp/` - Protocolo TOTP
- `MiAplicacion:admin` - Etiqueta (Issuer:Usuario)
- `?secret=JBSWY3DPEHPK3PXP` - Secreto en Base32
- `&issuer=MiAplicacion` - Emisor (nombre de tu app)

Parámetros opcionales:
- `&algorithm=SHA1` - Algoritmo hash (SHA1, SHA256, SHA512)
- `&digits=6` - Número de dígitos (6 u 8)
- `&period=30` - Periodo en segundos

Este URI se codifica en el QR code que el usuario escanea.

## 🧪 Testing

### Probar en local sin app autenticadora:

1. Llamar a `/2fa/enable` y copiar el `secret`
2. Usar una herramienta online como [https://totp.danhersam.com/](https://totp.danhersam.com/)
3. Pegar el secreto → Ver el código actual
4. Usar ese código en `/2fa/confirm`

⚠️ **Solo para testing en local!** No compartas secretos reales en sitios externos.

### Test unitario del algoritmo:

```java
@Test
void testTotpGeneration() {
    String secret = "JBSWY3DPEHPK3PXP";
    String code = totpService.getCurrentCode(secret);
    
    assertTrue(code.matches("\\d{6}")); // 6 dígitos
    assertTrue(totpService.verifyCode(secret, code)); // Válido
}
```

## 💡 Mejoras futuras

1. **Códigos de respaldo**
   ```sql
   ALTER TABLE usuarios 
   ADD COLUMN backup_codes TEXT; -- JSON array de códigos de 8 dígitos
   ```

2. **Recordar dispositivo**
   ```
   Cookie: remember_2fa=true (30 días)
   → No pedir 2FA en ese dispositivo
   ```

3. **Múltiples dispositivos**
   ```sql
   CREATE TABLE user_2fa_devices (
     id BIGINT PRIMARY KEY,
     user_id BIGINT,
     device_name VARCHAR(100),
     secret VARCHAR(100),
     created_at TIMESTAMP
   );
   ```

4. **Auditoría**
   ```sql
   CREATE TABLE 2fa_audit_log (
     id BIGINT PRIMARY KEY,
     user_id BIGINT,
     action VARCHAR(50), -- 'ENABLED', 'VERIFIED', 'FAILED', 'DISABLED'
     ip_address VARCHAR(50),
     user_agent TEXT,
     timestamp TIMESTAMP
   );
   ```

5. **Rate limiting**
   ```java
   // Máximo 5 intentos fallidos por minuto
   @RateLimit(maxAttempts = 5, windowSeconds = 60)
   public ResponseEntity<?> verify2FA(...) { ... }
   ```

