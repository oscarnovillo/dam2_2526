# Implementación Dragon Ball API con Retrofit + MVVM + State

## 🎯 Resumen de la implementación

Se ha implementado una lista de personajes de Dragon Ball consumiendo la API `https://dragonball-api.com/api/characters` utilizando:

- ✅ **Retrofit 3.0** para las peticiones HTTP
- ✅ **Arquitectura MVVM** (Model-View-ViewModel)
- ✅ **State Management** con StateFlow
- ✅ **Hilt** para inyección de dependencias
- ✅ **Coil** para carga de imágenes
- ✅ **Jetpack Compose** para la UI

---

## 📁 Estructura del proyecto

```
app/src/main/java/com/example/composeapp/
│
├── data/
│   ├── remote/
│   │   ├── DragonBallApi.kt           # Interface de Retrofit
│   │   └── dto/
│   │       └── CharacterDto.kt         # DTOs de la respuesta API
│   ├── mapper/
│   │   └── CharacterMapper.kt          # Mapeo de DTO a Model
│   └── repository/
│       └── CharacterRepository.kt      # Repository pattern
│
├── domain/
│   └── model/
│       └── Character.kt                # Modelo de dominio
│
├── ui/
│   └── screens/
│       ├── HomeScreen.kt               # Pantalla con la lista
│       ├── HomeViewModel.kt            # ViewModel con lógica
│       └── HomeUiState.kt              # Estado de la UI
│
└── di/
    └── NetworkModule.kt                # Módulo de Hilt para Retrofit
```

---

## 🔧 Dependencias agregadas

### `gradle/libs.versions.toml`:
```toml
[versions]
retrofit = "2.11.0"
okhttp = "4.12.0"
coil = "2.7.0"

[libraries]
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp = { group = "com.squareup.okhttp3", name = "okhttp", version.ref = "okhttp" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }
coil-compose = { group = "io.coil-kt", name = "coil-compose", version.ref = "coil" }
```

### `app/build.gradle.kts`:
```kotlin
// Retrofit
implementation(libs.retrofit)
implementation(libs.retrofit.converter.gson)
implementation(libs.okhttp)
implementation(libs.okhttp.logging.interceptor)

// Coil for image loading
implementation(libs.coil.compose)
```

---

## 📝 Archivos principales

### 1. **Character.kt** - Modelo de dominio
```kotlin
data class Character(
    val id: Int,
    val name: String,
    val ki: String,
    val maxKi: String,
    val race: String,
    val gender: String,
    val description: String,
    val image: String,
    val affiliation: String
)
```

### 2. **DragonBallApi.kt** - Interface de Retrofit
```kotlin
interface DragonBallApi {
    @GET("characters")
    suspend fun getCharacters(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): CharactersResponse
}
```

### 3. **NetworkModule.kt** - Configuración de Retrofit con Hilt
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://dragonball-api.com/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    // ... más configuraciones
}
```

### 4. **HomeUiState.kt** - Estado de la UI
```kotlin
data class HomeUiState(
    val isLoading: Boolean = false,
    val characters: List<Character> = emptyList(),
    val error: String? = null
)
```

### 5. **HomeViewModel.kt** - ViewModel con StateFlow
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CharacterRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadCharacters()
    }
    
    fun loadCharacters() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            repository.getCharacters()
                .onSuccess { characters ->
                    _uiState.update { 
                        it.copy(isLoading = false, characters = characters)
                    }
                }
                .onFailure { exception ->
                    _uiState.update { 
                        it.copy(isLoading = false, error = exception.message)
                    }
                }
        }
    }
}
```

### 6. **HomeScreen.kt** - UI con Compose
```kotlin
@Composable
fun HomeScreen(
    navigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    when {
        uiState.isLoading -> {
            CircularProgressIndicator()
        }
        uiState.error != null -> {
            ErrorView(error = uiState.error)
        }
        else -> {
            LazyColumn {
                items(uiState.characters) { character ->
                    CharacterItem(character)
                }
            }
        }
    }
}
```

---

## 🎨 Características de la UI

- **Cabecera** con título "Dragon Ball Characters"
- **TextField** para navegación (funcionalidad existente)
- **Lista scrolleable** con LazyColumn
- **Cards** con diseño Material3:
  - Imagen circular del personaje (con Coil)
  - Nombre en negrita
  - Raza, Ki y Afiliación
- **Estados de carga**:
  - Loading spinner
  - Mensaje de error con botón "Reintentar"
  - Lista de personajes

---

## 🔄 Flujo de datos (MVVM)

```
UI (HomeScreen)
    ↓ observa
StateFlow<HomeUiState>
    ↑ actualiza
ViewModel (HomeViewModel)
    ↓ llama
Repository (CharacterRepository)
    ↓ usa
Retrofit (DragonBallApi)
    ↓ hace petición HTTP
API de Dragon Ball
```

---

## 🚀 Cómo funciona

1. **Al iniciar la app**, el `HomeViewModel` se crea automáticamente con Hilt
2. En el `init{}`, se llama a `loadCharacters()`
3. El estado se actualiza a `isLoading = true`
4. El `Repository` hace la petición a la API con Retrofit
5. Los DTOs se mapean a modelos de dominio
6. El estado se actualiza con la lista de personajes o el error
7. La UI reacciona automáticamente a los cambios del StateFlow
8. Se muestra la lista con imágenes cargadas por Coil

---

## 📱 Permisos necesarios

Ya agregado en `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

---

## ✅ Próximos pasos sugeridos

1. **Paginación**: Implementar carga infinita con Paging3
2. **Caché**: Agregar Room para almacenamiento local
3. **Búsqueda**: Filtrar personajes por nombre
4. **Detalle**: Pantalla de detalle al hacer click en un personaje
5. **Adaptativo**: Usar WindowSizeClass para tablets
6. **Offline-first**: Patrón offline-first con NetworkBoundResource

---

## 🐛 Debugging

Para ver los logs de Retrofit, el `LoggingInterceptor` está configurado en modo `BODY`:
- Verás todas las peticiones y respuestas en Logcat
- Busca por el tag "OkHttp"

---

## 📚 Recursos adicionales

- [Documentación de Retrofit](https://square.github.io/retrofit/)
- [Documentación de Coil](https://coil-kt.github.io/coil/)
- [Material3 Adaptive](https://developer.android.com/develop/ui/compose/layouts/adaptive)
- [Ver guía de ComposeHotReload y WindowSizeClass](./GUIA_COMPOSE_ADAPTATIVO.md)

---

¡Listo! Tu app ahora consume la API de Dragon Ball con una arquitectura limpia y escalable. 🐉⚡

