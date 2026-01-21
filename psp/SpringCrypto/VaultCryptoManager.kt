// ============================================================================
// CLIENTE ANDROID - Vault Crypto Manager
// ============================================================================
// Este archivo muestra cómo implementar el cifrado en un cliente Android
// usando las mismas técnicas que el cliente web (vault-demo.html)
//
// IMPORTANTE: Este es el MISMO concepto pero en Kotlin para Android
// Los datos se cifran en el CLIENTE antes de enviarlos al servidor
// ============================================================================

package com.example.vault.crypto

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Manager de criptografía para el Vault
 *
 * Implementa:
 * - PBKDF2 para derivación de claves
 * - AES-256-GCM para cifrado autenticado
 * - Generación segura de salts e IVs
 */
class VaultCryptoManager {

    companion object {
        private const val ALGORITHM = "AES/GCM/NoPadding"
        private const val KDF_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val PBKDF2_ITERATIONS = 100_000
        private const val KEY_SIZE = 256 // AES-256
        private const val IV_SIZE = 12 // GCM usa 12 bytes
        private const val SALT_SIZE = 16
        private const val TAG_SIZE = 128 // Tag de autenticación GCM
    }

    /**
     * Resultado del cifrado
     */
    data class EncryptionResult(
        val ciphertext: ByteArray,
        val iv: ByteArray,
        val salt: ByteArray
    ) {
        // Para enviar al servidor
        fun toBase64(): EncryptionResultBase64 {
            return EncryptionResultBase64(
                ciphertext = ciphertext.toBase64(),
                iv = iv.toBase64(),
                salt = salt.toBase64()
            )
        }
    }

    data class EncryptionResultBase64(
        val ciphertext: String,
        val iv: String,
        val salt: String
    )

    /**
     * Deriva una clave AES-256 desde una password usando PBKDF2
     *
     * @param password Password del usuario
     * @param salt Salt único (16 bytes)
     * @return Clave AES-256
     */
    private fun deriveKey(password: String, salt: ByteArray): SecretKey {
        val spec = PBEKeySpec(
            password.toCharArray(),
            salt,
            PBKDF2_ITERATIONS,
            KEY_SIZE
        )

        val factory = SecretKeyFactory.getInstance(KDF_ALGORITHM)
        val tmp = factory.generateSecret(spec)

        return SecretKeySpec(tmp.encoded, "AES")
    }

    /**
     * Cifra datos con AES-256-GCM
     *
     * @param plaintext Datos a cifrar (String)
     * @param password Password del usuario
     * @return EncryptionResult con ciphertext, IV y salt
     */
    fun encrypt(plaintext: String, password: String): EncryptionResult {
        // 1. Generar salt e IV aleatorios
        val salt = generateRandomBytes(SALT_SIZE)
        val iv = generateRandomBytes(IV_SIZE)

        // 2. Derivar clave desde password
        val key = deriveKey(password, salt)

        // 3. Cifrar con AES-GCM
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec)

        val ciphertext = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))

        return EncryptionResult(ciphertext, iv, salt)
    }

    /**
     * Descifra datos con AES-256-GCM
     *
     * @param ciphertext Datos cifrados
     * @param password Password del usuario
     * @param iv Vector de inicialización
     * @param salt Salt usado en el cifrado
     * @return String descifrado
     * @throws Exception si la password es incorrecta o datos corruptos
     */
    fun decrypt(
        ciphertext: ByteArray,
        password: String,
        iv: ByteArray,
        salt: ByteArray
    ): String {
        // 1. Derivar la misma clave
        val key = deriveKey(password, salt)

        // 2. Descifrar con AES-GCM
        val cipher = Cipher.getInstance(ALGORITHM)
        val gcmSpec = GCMParameterSpec(TAG_SIZE, iv)
        cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec)

        try {
            val plaintext = cipher.doFinal(ciphertext)
            return String(plaintext, Charsets.UTF_8)
        } catch (e: Exception) {
            throw Exception("Password incorrecta o datos corruptos", e)
        }
    }

    /**
     * Descifra desde Base64 (útil para respuestas del servidor)
     */
    fun decrypt(
        ciphertextBase64: String,
        password: String,
        ivBase64: String,
        saltBase64: String
    ): String {
        return decrypt(
            ciphertext = ciphertextBase64.fromBase64(),
            password = password,
            iv = ivBase64.fromBase64(),
            salt = saltBase64.fromBase64()
        )
    }

    /**
     * Genera bytes aleatorios seguros
     */
    private fun generateRandomBytes(size: Int): ByteArray {
        val bytes = ByteArray(size)
        SecureRandom().nextBytes(bytes)
        return bytes
    }

    // Extensiones para Base64
    private fun ByteArray.toBase64(): String {
        return Base64.encodeToString(this, Base64.NO_WRAP)
    }

    private fun String.fromBase64(): ByteArray {
        return Base64.decode(this, Base64.NO_WRAP)
    }
}

// ============================================================================
// EJEMPLO DE USO - Activity o ViewModel
// ============================================================================

class VaultExampleUsage {

    private val cryptoManager = VaultCryptoManager()

    /**
     * Ejemplo 1: Guardar un secreto
     */
    suspend fun saveSecret(data: String, password: String, title: String) {
        // 1. Cifrar datos LOCALMENTE
        val encrypted = cryptoManager.encrypt(data, password)

        // 2. Cifrar título (opcional)
        val titleEncrypted = cryptoManager.encrypt(
            "{\"title\":\"$title\"}",
            password
        )

        // 3. Preparar request para el servidor
        val request = SaveSecretRequest(
            encryptedData = encrypted.toBase64().ciphertext,
            iv = encrypted.toBase64().iv,
            salt = encrypted.toBase64().salt,
            metadata = titleEncrypted.toBase64().ciphertext // JSON cifrado
        )

        // 4. Enviar al servidor (Retrofit, OkHttp, etc.)
        val response = vaultApiService.saveSecret(request)

        // 5. Guardar el ID devuelto
        println("Secreto guardado con ID: ${response.secretId}")
    }

    /**
     * Ejemplo 2: Recuperar un secreto
     */
    suspend fun retrieveSecret(secretId: Long, password: String): String {
        // 1. Obtener datos cifrados del servidor
        val response = vaultApiService.getSecret(secretId)

        // 2. Descifrar LOCALMENTE
        try {
            val decrypted = cryptoManager.decrypt(
                ciphertextBase64 = response.encryptedData,
                password = password,
                ivBase64 = response.iv,
                saltBase64 = response.salt
            )

            return decrypted

        } catch (e: Exception) {
            throw Exception("Password incorrecta o datos corruptos")
        }
    }

    /**
     * Ejemplo 3: Listar secretos
     */
    suspend fun listSecrets(): List<SecretListItem> {
        return vaultApiService.listSecrets()
        // Los títulos vienen cifrados, descifrar si es necesario:
        // item.metadata -> descifrar con password
    }
}

// ============================================================================
// DTOs para Retrofit
// ============================================================================

data class SaveSecretRequest(
    val encryptedData: String,
    val iv: String,
    val salt: String,
    val metadata: String?
)

data class SaveSecretResponse(
    val secretId: Long,
    val createdAt: String
)

data class SecretDetailResponse(
    val id: Long,
    val encryptedData: String,
    val iv: String,
    val salt: String,
    val metadata: String?
)

data class SecretListItem(
    val id: Long,
    val metadata: String?,
    val createdAt: String,
    val updatedAt: String
)

// ============================================================================
// API Service (Retrofit)
// ============================================================================

interface VaultApiService {

    @POST("/api/vault/secrets")
    suspend fun saveSecret(
        @Body request: SaveSecretRequest
    ): SaveSecretResponse

    @GET("/api/vault/secrets/{id}")
    suspend fun getSecret(
        @Path("id") secretId: Long
    ): SecretDetailResponse

    @GET("/api/vault/secrets")
    suspend fun listSecrets(): List<SecretListItem>

    @DELETE("/api/vault/secrets/{id}")
    suspend fun deleteSecret(
        @Path("id") secretId: Long
    )

    @PUT("/api/vault/secrets/{id}")
    suspend fun updateSecret(
        @Path("id") secretId: Long,
        @Body request: SaveSecretRequest
    )
}

// ============================================================================
// CONFIGURACIÓN DE RETROFIT
// ============================================================================

/*
val retrofit = Retrofit.Builder()
    .baseUrl("https://tu-servidor.com")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val vaultApiService = retrofit.create(VaultApiService::class.java)
*/

// ============================================================================
// UI EJEMPLO - Jetpack Compose
// ============================================================================

/*
@Composable
fun SaveSecretScreen(viewModel: VaultViewModel) {
    var password by remember { mutableStateOf("") }
    var secretData by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "🔐 Guardar Secreto",
            style = MaterialTheme.typography.h5
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = secretData,
            onValueChange = { secretData = it },
            label = { Text("Datos secretos") },
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.saveSecret(secretData, password, title)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🔒 Cifrar y Guardar")
        }
    }
}
*/

// ============================================================================
// SEGURIDAD ADICIONAL: Android Keystore
// ============================================================================

/**
 * Para mejorar la seguridad, puedes usar Android Keystore para:
 * 1. Proteger la clave derivada con biometría
 * 2. Evitar que el usuario escriba password cada vez
 */

/*
class SecureKeyStorage(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Guarda la clave derivada en Keystore (protegida por biometría)
     */
    fun saveKey(alias: String, key: SecretKey) {
        val entry = KeyStore.SecretKeyEntry(key)
        keyStore.setEntry(
            alias,
            entry,
            KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setUserAuthenticationRequired(true) // Requiere biometría
                .setUserAuthenticationValidityDurationSeconds(30)
                .build()
        )
    }

    /**
     * Recupera la clave (solo con autenticación biométrica)
     */
    fun getKey(alias: String): SecretKey? {
        return (keyStore.getEntry(alias, null) as? KeyStore.SecretKeyEntry)?.secretKey
    }
}
*/

// ============================================================================
// EJEMPLO COMPLETO: ViewModel con Coroutines
// ============================================================================

/*
class VaultViewModel(
    private val vaultApiService: VaultApiService,
    private val cryptoManager: VaultCryptoManager
) : ViewModel() {

    private val _saveState = MutableStateFlow<UiState<Long>>(UiState.Idle)
    val saveState: StateFlow<UiState<Long>> = _saveState.asStateFlow()

    private val _retrieveState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val retrieveState: StateFlow<UiState<String>> = _retrieveState.asStateFlow()

    fun saveSecret(data: String, password: String, title: String) {
        viewModelScope.launch {
            _saveState.value = UiState.Loading

            try {
                // Cifrar localmente
                val encrypted = cryptoManager.encrypt(data, password)
                val titleEncrypted = if (title.isNotEmpty()) {
                    cryptoManager.encrypt("{\"title\":\"$title\"}", password)
                } else null

                // Enviar al servidor
                val response = vaultApiService.saveSecret(
                    SaveSecretRequest(
                        encryptedData = encrypted.toBase64().ciphertext,
                        iv = encrypted.toBase64().iv,
                        salt = encrypted.toBase64().salt,
                        metadata = titleEncrypted?.toBase64()?.ciphertext
                    )
                )

                _saveState.value = UiState.Success(response.secretId)

            } catch (e: Exception) {
                _saveState.value = UiState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun retrieveSecret(secretId: Long, password: String) {
        viewModelScope.launch {
            _retrieveState.value = UiState.Loading

            try {
                // Obtener del servidor
                val response = vaultApiService.getSecret(secretId)

                // Descifrar localmente
                val decrypted = cryptoManager.decrypt(
                    ciphertextBase64 = response.encryptedData,
                    password = password,
                    ivBase64 = response.iv,
                    saltBase64 = response.salt
                )

                _retrieveState.value = UiState.Success(decrypted)

            } catch (e: Exception) {
                _retrieveState.value = UiState.Error("Password incorrecta o error de conexión")
            }
        }
    }
}

sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
*/

// ============================================================================
// NOTAS IMPORTANTES
// ============================================================================

/*
1. NUNCA guardes passwords en SharedPreferences o en claro
2. Usa BiometricPrompt para autenticación biométrica
3. Limpia las variables con datos sensibles después de usarlas:
   password.toCharArray().fill('0')
4. Usa ProGuard/R8 para ofuscar el código en producción
5. Habilita FLAG_SECURE en Activities sensibles:
   window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, ...)
6. Considera usar Android Backup exclusions para datos cifrados
7. Implementa rate limiting en el servidor
8. Usa certificado pinning (Certificate Pinning) para HTTPS
*/

