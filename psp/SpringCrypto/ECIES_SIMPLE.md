# 🎯 ECIES - Ejemplo Simple de Uso

## ¿Qué es?

**ECIES** = Cifrado con Curvas Elípticas que combina:
- **ECDH**: Para intercambio seguro de claves
- **AES-GCM**: Para cifrar los datos

## 🚀 Uso en 3 pasos

### 1️⃣ Generar claves EC

**Petición:**
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

**💡 Guarda:**
- `publicKey` → Se puede compartir
- `privateKey` → Mantén en secreto

---

### 2️⃣ Cifrar con ECIES

**Petición:**
```http
POST http://localhost:8080/api/asymmetric/encrypt-ecies
Content-Type: application/json

{
  "plainText": "Mi mensaje secreto 🔐",
  "publicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE..."
}
```

**Respuesta:**
```json
{
  "ephemeralPublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
  "encryptedData": "xK9mN2pL8vR3...",
  "iv": "vR3K8pL9mN2x...",
  "algorithm": "ECIES (ECDH + AES-GCM)"
}
```

**💡 Guarda todo:** Necesitarás `ephemeralPublicKey`, `encryptedData` e `iv` para descifrar

---

### 3️⃣ Descifrar con ECIES

**Petición:**
```http
POST http://localhost:8080/api/asymmetric/decrypt-ecies
Content-Type: application/json

{
  "privateKey": "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCA...",
  "ephemeralPublicKey": "MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAE...",
  "encryptedData": "xK9mN2pL8vR3...",
  "iv": "vR3K8pL9mN2x..."
}
```

**Respuesta:**
```json
{
  "plainText": "Mi mensaje secreto 🔐",
  "algorithm": "ECIES"
}
```

---

## 📝 Ejemplo Completo en IntelliJ

Abre el archivo `api-tests-ecies.http` y ejecuta las peticiones en orden:

```http
### 1. Generar claves EC
GET http://localhost:8080/api/asymmetric/generate-keypair?algorithm=EC
# Las claves se guardan automáticamente en variables {{ecPublicKey}} y {{ecPrivateKey}}

###

### 2. Cifrar mensaje
POST http://localhost:8080/api/asymmetric/encrypt-ecies
Content-Type: application/json

{
  "plainText": "Este es mi mensaje secreto",
  "publicKey": "{{ecPublicKey}}"
}
# Los datos cifrados se guardan automáticamente

###

### 3. Descifrar mensaje
POST http://localhost:8080/api/asymmetric/decrypt-ecies
Content-Type: application/json

{
  "privateKey": "{{ecPrivateKey}}",
  "ephemeralPublicKey": "{{eciesEphemeralPublicKey}}",
  "encryptedData": "{{eciesEncryptedData}}",
  "iv": "{{eciesIv}}"
}
```

---

## 💡 Conceptos Clave

### Claves Efímeras
- **Efímera** = Temporal, solo para un mensaje
- Se genera automáticamente en cada cifrado
- Proporciona **Forward Secrecy**

### ¿Cómo funciona?
```
Alice (cifra)                         Bob (descifra)
━━━━━━━━━━━                          ━━━━━━━━━━━

1. Genera claves efímeras (r, R)
                                      Tiene su clave privada (b)
                                      y pública (B)

2. Secreto = r × B  ─────┐
                          │
                          ├──► ¡Mismo secreto!
                          │
3. Secreto = b × R  ◄─────┘

4. Deriva clave AES del secreto

5. Cifra con AES-GCM

6. Envía: R, datos cifrados, IV ───────►

                                      7. Calcula mismo secreto
                                      8. Deriva misma clave AES
                                      9. Descifra
```

---

## 🆚 ECIES vs RSA

| Característica | RSA-2048 | ECIES (EC-256) |
|----------------|----------|----------------|
| **Claves** | Grandes (~294 bytes) | Pequeñas (~91 bytes) |
| **Límite tamaño** | 214 bytes | ∞ (sin límite) |
| **Velocidad** | Lento | Rápido |
| **Forward Secrecy** | ❌ | ✅ |

---

## 🎓 Documentación Completa

- **[ECIES_QUICKSTART.md](ECIES_QUICKSTART.md)** - Guía detallada paso a paso
- **[ECIES_EXPLAINED.md](ECIES_EXPLAINED.md)** - Explicación técnica completa
- **[api-tests-ecies.http](api-tests-ecies.http)** - Todos los ejemplos

---

## ✅ Checklist

- [ ] Iniciar servidor: `mvn spring-boot:run`
- [ ] Abrir `api-tests-ecies.http` en IntelliJ
- [ ] Ejecutar petición 1: Generar claves EC
- [ ] Ejecutar petición 2: Cifrar mensaje
- [ ] Ejecutar petición 3: Descifrar mensaje
- [ ] Verificar que el mensaje descifrado es el original ✓

---

**¡Listo! Ya sabes usar ECIES 🎉**

