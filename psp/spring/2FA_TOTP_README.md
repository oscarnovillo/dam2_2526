# 🔐 Autenticación de Dos Factores (2FA) con TOTP

## ¿Qué es TOTP?

**TOTP (Time-based One-Time Password)** es un algoritmo que genera códigos de verificación de 6 dígitos que cambian cada 30 segundos. Es el estándar usado por aplicaciones como Google Authenticator, Microsoft Authenticator, Authy, etc.

## 🔧 ¿Cómo funciona?

### Principio básico:

1. **Secreto compartido**: Cuando activas 2FA, el servidor genera un secreto único que se comparte con tu app autenticadora (mediante un QR code)
2. **Sincronización temporal**: Tanto el servidor como tu app usan el tiempo actual (dividido en intervalos de 30 segundos) 
3. **Generación del código**: Combinando el secreto + tiempo actual, ambos generan el mismo código de 6 dígitos
4. **Validación**: El servidor verifica que el código que introduces coincida con el que él generó

### Fórmula matemática simplificada:
```
código = HMAC-SHA1(secreto, tiempo_actual / 30 segundos) % 1,000,000
```

## 📱 Flujo de uso

### 1️⃣ **Activar 2FA (primera vez)**

**Endpoint:** `POST /api/auth/2fa/enable`

**Requisito:** Usuario debe estar autenticado (tener sesión activa)

**Respuesta:**
```json
{
  "success": true,
  "data": {
    "secret": "JBSWY3DPEHPK3PXP",
    "qrCodeUri": "data:image/png;base64,iVBORw0KGgoAAAANS...",
    "message": "Escanea el código QR con tu aplicación autenticadora..."
  }
}
```

**Pasos:**
1. Usuario hace login normal
2. Llama al endpoint `/2fa/enable`
3. El servidor genera un **secreto único** y lo guarda temporalmente
4. Devuelve un **QR code** (imagen en base64)
5. Usuario escanea el QR con Google Authenticator/Authy
6. La app autenticadora empieza a generar códigos de 6 dígitos

### 2️⃣ **Confirmar activación de 2FA**

**Endpoint:** `POST /api/auth/2fa/confirm`

**Body:**
```json
{
  "code": "123456"
}
```

**¿Por qué este paso?** Para verificar que el usuario escaneó correctamente el QR y que los códigos coinciden.

**Respuesta exitosa:**
```json
{
  "success": true,
  "message": "Autenticación de dos factores activada correctamente"
}
```

Ahora el usuario tiene 2FA **permanentemente activado**.

### 3️⃣ **Login con 2FA (dos pasos)**

#### Paso 1: Username + Password

**Endpoint:** `POST /api/auth/login`

**Body:**
```json
{
  "username": "usuario",
  "password": "contraseña"
}
```

**Respuesta si tiene 2FA:**
```json
{
  "success": false,
  "requiresTwoFactor": true,
  "message": "Se requiere código de autenticación de dos factores"
}
```

El servidor guarda temporalmente en sesión que el usuario pasó la primera fase.

#### Paso 2: Código TOTP

**Endpoint:** `POST /api/auth/2fa/verify`

**Body:**
```json
{
  "username": "usuario",
  "code": "123456"
}
```

**Respuesta exitosa:**
```json
{
  "success": true,
  "message": "Login completado exitosamente",
  "usuario": {
    "id": 1,
    "username": "usuario",
    "email": "user@example.com",
    "nombre": "Usuario Test",
    "rol": "USER",
    "twoFactorEnabled": true
  }
}
```

Ahora el login está completo y el usuario tiene sesión activa.

### 4️⃣ **Consultar estado de 2FA**

**Endpoint:** `GET /api/auth/2fa/status`

**Respuesta:**
```json
{
  "success": true,
  "twoFactorEnabled": true
}
```

### 5️⃣ **Desactivar 2FA**

**Endpoint:** `POST /api/auth/2fa/disable`

**Respuesta:**
```json
{
  "success": true,
  "message": "Autenticación de dos factores desactivada"
}
```

El secreto se elimina y el usuario vuelve a login normal.

## 🗄️ Cambios en la base de datos

Se agregaron dos campos a la tabla `usuarios`:

```sql
ALTER TABLE usuarios 
ADD COLUMN two_factor_enabled BOOLEAN NOT NULL DEFAULT FALSE,
ADD COLUMN two_factor_secret VARCHAR(100);
```

- **two_factor_enabled**: Indica si el usuario tiene 2FA activo
- **two_factor_secret**: El secreto TOTP en formato Base32 (ej: "JBSWY3DPEHPK3PXP")

## 📦 Dependencia Maven

```xml
<dependency>
    <groupId>dev.samstevens.totp</groupId>
    <artifactId>totp</artifactId>
    <version>1.7.1</version>
</dependency>
```

Esta librería proporciona:
- Generación de secretos
- Generación de códigos QR
- Validación de códigos TOTP
- Algoritmo HMAC-SHA1 estándar

## 🔒 Seguridad

### ✅ Buenas prácticas implementadas:

1. **Ventana de tolerancia**: Se acepta el código actual ± 30 segundos (para compensar desfase de relojes)
2. **Secreto único por usuario**: Cada usuario tiene su propio secreto, no se reutiliza
3. **Activación en dos pasos**: No se activa 2FA hasta confirmar con un código válido
4. **No se guarda el código**: El servidor nunca guarda los códigos, solo el secreto
5. **Sesión temporal**: Durante el login 2FA, se usa un atributo de sesión temporal que se borra tras verificar

### ⚠️ Consideraciones adicionales (para producción):

1. **Rate limiting**: Limitar intentos de verificación (ej: 5 intentos/minuto)
2. **Códigos de respaldo**: Generar 10 códigos de un solo uso por si el usuario pierde el móvil
3. **Notificaciones**: Enviar email al activar/desactivar 2FA
4. **Auditoría**: Registrar intentos fallidos de 2FA
5. **Recuperación de cuenta**: Proceso para recuperar acceso si se pierde el móvil

## 🧪 Probar en Postman/Insomnia

### 1. Login normal y activar 2FA

```http
### Login
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

### Habilitar 2FA (guardar la cookie de sesión del login anterior)
POST http://localhost:8080/api/auth/2fa/enable
Cookie: JSESSIONID=xxx

### La respuesta incluirá un QR code en base64
### Copia la URL del qrCodeUri y ábrela en el navegador
### O decodifica el secreto y mételo manualmente en Google Authenticator

### Confirmar 2FA con código de Google Authenticator
POST http://localhost:8080/api/auth/2fa/confirm
Content-Type: application/json
Cookie: JSESSIONID=xxx

{
  "code": "123456"
}
```

### 2. Hacer logout y probar login con 2FA

```http
### Logout
POST http://localhost:8080/api/auth/logout
Cookie: JSESSIONID=xxx

### Login paso 1 (username + password)
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}

### Respuesta: { "requiresTwoFactor": true }

### Login paso 2 (código TOTP) - usar nueva cookie de sesión
POST http://localhost:8080/api/auth/2fa/verify
Content-Type: application/json
Cookie: JSESSIONID=yyy

{
  "username": "admin",
  "code": "789012"
}

### Respuesta: Login completado con datos del usuario
```

## 📱 Apps autenticadoras recomendadas

- **Google Authenticator** (iOS/Android)
- **Microsoft Authenticator** (iOS/Android)
- **Authy** (iOS/Android/Desktop)
- **1Password** (con suscripción)
- **Bitwarden** (con suscripción)

Todas son compatibles con el estándar TOTP.

## 🎯 Casos de uso

### ¿Cuándo usar 2FA?

✅ **Recomendado para:**
- Cuentas de administradores
- Aplicaciones financieras
- Acceso a datos sensibles
- Servicios expuestos a Internet

❌ **No necesario para:**
- Aplicaciones internas sin datos críticos
- Entornos de desarrollo/testing
- Apps con otros mecanismos de seguridad robustos (certificados cliente, VPN, etc.)

## 🐛 Troubleshooting

### "Código inválido"

**Causas comunes:**
1. **Reloj desincronizado**: El servidor y el móvil deben tener la hora correcta
   - Solución: Activar sincronización automática de hora en el móvil
   
2. **Código expirado**: Los códigos duran 30 segundos
   - Solución: Esperar a que cambie el código e introducir el nuevo
   
3. **Secreto incorrecto**: Se escaneó mal el QR
   - Solución: Borrar la cuenta en la app autenticadora y volver a escanear

### "No hay un login pendiente de verificación 2FA"

- La sesión expiró entre el paso 1 y 2 del login
- Solución: Volver a hacer el paso 1 (POST /login con username+password)

## 🔄 Migrar usuarios existentes

Los usuarios que ya existen seguirán haciendo login normal. Para activar 2FA deben:

1. Hacer login normal (solo username + password)
2. Llamar a `/2fa/enable` estando autenticados
3. Escanear el QR
4. Confirmar con `/2fa/confirm`

No es necesario migrar todos los usuarios a la vez, es opcional por usuario.

## 💡 Alternativas a TOTP

Otros métodos de 2FA (no implementados aquí):

1. **SMS**: Enviar código por mensaje de texto
   - ❌ Menos seguro (SIM swapping)
   - ✅ Más familiar para usuarios no técnicos

2. **Email**: Enviar código por correo
   - ❌ Depende de la seguridad del email
   - ✅ No requiere app adicional

3. **WebAuthn/FIDO2**: Llaves de seguridad físicas (YubiKey)
   - ✅ Más seguro
   - ❌ Requiere hardware adicional

4. **Push notifications**: Notificaciones en app móvil propia
   - ✅ Experiencia de usuario excelente
   - ❌ Requiere desarrollar app móvil

TOTP es un buen balance entre seguridad, facilidad de implementación y experiencia de usuario.

## 📚 Referencias

- [RFC 6238 - TOTP Specification](https://tools.ietf.org/html/rfc6238)
- [RFC 4226 - HOTP Specification](https://tools.ietf.org/html/rfc4226)
- [dev.samstevens.totp Documentation](https://github.com/samdjstevens/java-totp)
- [Google Authenticator Protocol](https://github.com/google/google-authenticator/wiki/Key-Uri-Format)

