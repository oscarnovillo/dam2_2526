# 🔐 Guía Completa de Criptografía

## Índice
1. [Conceptos Fundamentales](#conceptos-fundamentales)
2. [Encriptación Simétrica](#encriptación-simétrica)
3. [Modos de Operación AES](#modos-de-operación-aes)
4. [Encriptación Asimétrica](#encriptación-asimétrica)
5. [Firma Digital](#firma-digital)
6. [Encriptación Híbrida](#encriptación-híbrida)
7. [Curvas Elípticas (EC)](#curvas-elípticas-ec)
8. [Mejores Prácticas](#mejores-prácticas)

---

## Conceptos Fundamentales

### ¿Qué es la Criptografía?

La **criptografía** es la ciencia de proteger información mediante técnicas de codificación, transformando datos legibles (texto plano) en datos ilegibles (texto cifrado) que solo pueden ser descifrados por quien posea la clave correcta.

### Terminología Básica

- **Texto Plano (Plaintext)**: Información original sin cifrar
- **Texto Cifrado (Ciphertext)**: Información después de aplicar encriptación
- **Clave (Key)**: Secreto usado para cifrar y descifrar datos
- **Algoritmo**: Procedimiento matemático para cifrar/descifrar
- **IV (Initialization Vector)**: Valor aleatorio usado en algunos modos de cifrado
- **Padding**: Relleno añadido para completar bloques de tamaño fijo

### Objetivos de la Criptografía

1. **Confidencialidad**: Solo el destinatario autorizado puede leer el mensaje
2. **Integridad**: Detectar si el mensaje ha sido modificado
3. **Autenticación**: Verificar la identidad del emisor
4. **No repudio**: El emisor no puede negar haber enviado el mensaje

---

## Encriptación Simétrica

### Definición

La **encriptación simétrica** (o de clave secreta) usa la **misma clave** para cifrar y descifrar datos. Es como tener una caja fuerte donde la misma llave abre y cierra.

### Características

✅ **Ventajas:**
- Muy rápida (ideal para grandes volúmenes de datos)
- Menor complejidad computacional
- Eficiente en recursos

❌ **Desventajas:**
- Problema de distribución de claves (¿cómo compartir la clave de forma segura?)
- Requiere tantas claves como pares de comunicación (n*(n-1)/2 para n usuarios)

### AES (Advanced Encryption Standard)

**AES** es el estándar de encriptación simétrica más utilizado actualmente.

#### Especificaciones Técnicas

- **Tipo**: Cifrado de bloque
- **Tamaño de bloque**: 128 bits (16 bytes)
- **Tamaños de clave**: 128, 192 o 256 bits
- **Adoptado**: 2001 por NIST (National Institute of Standards and Technology)
- **Algoritmo base**: Rijndael

#### Funcionamiento

AES opera en bloques de 128 bits y aplica múltiples rondas de transformaciones:

1. **SubBytes**: Sustitución no lineal usando S-box
2. **ShiftRows**: Permutación de filas
3. **MixColumns**: Mezcla de datos en cada columna
4. **AddRoundKey**: XOR con una subclave derivada

Número de rondas según tamaño de clave:
- AES-128: 10 rondas
- AES-192: 12 rondas
- AES-256: 14 rondas

#### Ejemplo de Uso

```java
// Generar clave AES de 256 bits
KeyGenerator keyGen = KeyGenerator.getInstance("AES");
keyGen.init(256);
SecretKey key = keyGen.generateKey();

// Cifrar
Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
cipher.init(Cipher.ENCRYPT_MODE, key);
byte[] encrypted = cipher.doFinal(plainText.getBytes());
```

---

## Modos de Operación AES

Los modos de operación determinan cómo se aplica el cifrado de bloque a datos de longitud arbitraria.

### 1. ECB (Electronic Codebook)

#### Descripción
El modo más simple. Cada bloque se cifra independientemente con la misma clave.

#### Funcionamiento
```
Bloque 1 → [Cifrado AES] → Cifrado 1
Bloque 2 → [Cifrado AES] → Cifrado 2
Bloque 3 → [Cifrado AES] → Cifrado 3
```

#### Características

❌ **NO RECOMENDADO PARA PRODUCCIÓN**

**Ventajas:**
- Simple de implementar
- Paralelizable (cifrado y descifrado)
- No propaga errores

**Desventajas:**
- ⚠️ **GRAVE**: Bloques idénticos producen cifrados idénticos
- Revela patrones en los datos
- Vulnerable a análisis de frecuencia
- No usa IV

#### Ejemplo Práctico del Problema

```
Texto original:  "HOLA HOLA"
Cifrado ECB:     "XY12 XY12"  ← ¡Se repite el patrón!
Cifrado CBC:     "XY12 AB34"  ← Patrones ocultos
```

#### Cuándo Usar (raramente)
- Datos aleatorios sin patrones
- Cifrado de claves individuales muy cortas

---

### 2. CBC (Cipher Block Chaining)

#### Descripción
Cada bloque se hace XOR con el bloque cifrado anterior antes de cifrarse. El primer bloque usa un IV.

#### Funcionamiento
```
Bloque 1 ⊕ IV        → [Cifrado AES] → Cifrado 1
Bloque 2 ⊕ Cifrado 1 → [Cifrado AES] → Cifrado 2
Bloque 3 ⊕ Cifrado 2 → [Cifrado AES] → Cifrado 3
```

#### Características

✅ **Recomendado para muchos casos**

**Ventajas:**
- Oculta patrones efectivamente
- Cada bloque cifrado depende de todos los anteriores
- Ampliamente soportado

**Desventajas:**
- Cifrado secuencial (no paralelizable)
- Errores se propagan al siguiente bloque
- Requiere padding
- Vulnerable a ataques de padding oracle si no se implementa correctamente

#### Requisitos
- **IV**: Vector de inicialización aleatorio de 128 bits
- **Padding**: PKCS5/PKCS7 para completar el último bloque

#### Ejemplo de Código
```java
// Generar IV aleatorio
byte[] iv = new byte[16];
new SecureRandom().nextBytes(iv);

// Cifrar
Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
IvParameterSpec ivSpec = new IvParameterSpec(iv);
cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
byte[] encrypted = cipher.doFinal(plainText.getBytes());
```

---

### 3. CTR (Counter Mode)

#### Descripción
Convierte un cifrado de bloque en un cifrado de flujo. Cifra un contador incremental y hace XOR con el texto plano.

#### Funcionamiento
```
Counter 1 → [Cifrado AES] → Keystream 1 ⊕ Bloque 1 → Cifrado 1
Counter 2 → [Cifrado AES] → Keystream 2 ⊕ Bloque 2 → Cifrado 2
Counter 3 → [Cifrado AES] → Keystream 3 ⊕ Bloque 3 → Cifrado 3
```

#### Características

✅ **Excelente para ciertas aplicaciones**

**Ventajas:**
- Totalmente paralelizable (cifrado y descifrado)
- No requiere padding
- Acceso aleatorio a bloques
- Mismo proceso para cifrar y descifrar
- Errores no se propagan

**Desventajas:**
- ⚠️ Reutilizar un IV/contador con la misma clave es catastrófico
- No proporciona autenticación

#### Estructura del Contador
```
| Nonce (IV) |  Counter  |
|  64 bits   |  64 bits  |
```

#### Ejemplo de Código
```java
Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
IvParameterSpec ivSpec = new IvParameterSpec(iv);
cipher.init(Cipher.ENCRYPT_MODE, key, ivSpec);
byte[] encrypted = cipher.doFinal(plainText.getBytes());
```

---

### 4. GCM (Galois/Counter Mode)

#### Descripción
Modo AEAD (Authenticated Encryption with Associated Data). Combina encriptación CTR con autenticación GMAC.

#### Funcionamiento
```
[Cifrado CTR] + [Autenticación GMAC] → Cifrado + Tag de Autenticación
```

#### Características

✅ **MÁS RECOMENDADO ACTUALMENTE**

**Ventajas:**
- Proporciona confidencialidad + integridad + autenticación
- Muy eficiente (paralelizable)
- No requiere padding
- Detecta modificaciones maliciosas
- Puede autenticar datos adicionales sin cifrarlos (AAD)
- Estándar en TLS 1.3, IPsec, SSH

**Desventajas:**
- Más complejo de implementar
- ⚠️ **CRÍTICO**: Nunca reutilizar IV con la misma clave

#### Componentes
- **IV**: 12 bytes (96 bits) recomendado para GCM
- **Tag de Autenticación**: 128 bits (16 bytes) típicamente
- **AAD**: Datos adicionales autenticados pero no cifrados (opcional)

#### Ejemplo de Código
```java
byte[] iv = new byte[12]; // 12 bytes para GCM
new SecureRandom().nextBytes(iv);

Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
GCMParameterSpec gcmSpec = new GCMParameterSpec(128, iv); // Tag de 128 bits
cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

// Opcional: añadir datos autenticados no cifrados
cipher.updateAAD("metadata".getBytes());

byte[] encrypted = cipher.doFinal(plainText.getBytes());
```

#### Seguridad del IV en GCM

⚠️ **MUY IMPORTANTE**: Si reutilizas un IV con la misma clave en GCM:
- Se puede recuperar la clave de autenticación
- Se compromete completamente la seguridad

Estrategias para generar IV:
1. **Aleatorio**: Usar `SecureRandom` (requiere IV de 96 bits)
2. **Contador**: Incrementar un contador (requiere estado)
3. **Derivado**: Usar función hash del mensaje

---

### Comparación de Modos AES

| Modo | Seguridad | Velocidad | Paralelizable | Autenticación | Uso Típico |
|------|-----------|-----------|---------------|---------------|------------|
| **ECB** | ⚠️ Baja | ⚡⚡⚡ | ✅ Sí | ❌ No | ⛔ Evitar |
| **CBC** | ✅ Buena | ⚡⚡ | ❌ No (cifrado) | ❌ No | Archivos, SSL/TLS antiguo |
| **CTR** | ✅ Buena | ⚡⚡⚡ | ✅ Sí | ❌ No | IPsec, streaming |
| **GCM** | ⭐ Excelente | ⚡⚡⚡ | ✅ Sí | ✅ Sí | **TLS 1.3, aplicaciones modernas** |

---

*Continúa en la siguiente parte...*

