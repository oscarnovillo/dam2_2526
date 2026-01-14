# Guía: ComposeHotReload y WindowSizeClass

## ComposeHotReload

### ¿Qué es?
**ComposeHotReload** es una librería que permite ver cambios en tus composables en tiempo real sin necesidad de recompilar toda la aplicación ni reiniciarla. Es similar al hot reload de Flutter.

### Características principales:
- ✅ **Actualización instantánea**: Los cambios en los composables se reflejan inmediatamente
- ✅ **Mantiene el estado**: No pierdes el estado de la aplicación al hacer cambios
- ✅ **Mejora la productividad**: Reduce drásticamente el tiempo de desarrollo
- ✅ **Compatible con Jetpack Compose**: Funciona perfectamente con Material3

### Cómo agregar ComposeHotReload:

1. **Agregar al `gradle/libs.versions.toml`:**
```toml
[versions]
composeHotReload = "1.0.0-alpha01"

[libraries]
compose-hotreload = { module = "com.github.takahirom.compose-hot-reload:compose-hot-reload", version.ref = "composeHotReload" }
```

2. **Agregar al `app/build.gradle.kts`:**
```kotlin
dependencies {
    debugImplementation(libs.compose.hotreload)
}
```

3. **Inicializar en tu aplicación:**
```kotlin
@Composable
fun App() {
    if (BuildConfig.DEBUG) {
        ComposeHotReload()
    }
    // Tu contenido aquí
}
```

---

## WindowSizeClass

### ¿Qué es?
**WindowSizeClass** es una API de Material3 Adaptive que te permite crear UIs adaptativas según el tamaño de la pantalla del dispositivo. Es esencial para crear apps que se vean bien en teléfonos, tablets, plegables y diferentes orientaciones.

### Clasificaciones de tamaño:

| Clase | Ancho | Dispositivos típicos |
|-------|-------|---------------------|
| **Compact** | < 600dp | Teléfonos en vertical |
| **Medium** | 600dp - 840dp | Teléfonos en horizontal, tablets pequeñas |
| **Expanded** | > 840dp | Tablets grandes, plegables abiertos |

### Cómo usar WindowSizeClass:

1. **Ya tienes la dependencia agregada:**
```kotlin
implementation(libs.material3.adaptive)
```

2. **Obtener WindowSizeClass en tu Activity:**
```kotlin
@Composable
fun HomeScreen() {
    val windowSizeClass = calculateWindowSizeClass(activity = LocalContext.current as Activity)
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Layout para móviles (lista vertical)
            CompactLayout()
        }
        WindowWidthSizeClass.Medium -> {
            // Layout para tablets pequeñas (2 columnas)
            MediumLayout()
        }
        WindowWidthSizeClass.Expanded -> {
            // Layout para tablets grandes (navegación lateral + contenido)
            ExpandedLayout()
        }
    }
}
```

3. **Ejemplo práctico con tu lista de Dragon Ball:**
```kotlin
@Composable
fun HomeScreen(
    navigateToDetail: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val windowSizeClass = calculateWindowSizeClass(LocalContext.current as Activity)
    
    when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Compact -> {
            // Móvil: Lista de 1 columna
            CharacterListCompact(uiState, navigateToDetail)
        }
        WindowWidthSizeClass.Medium -> {
            // Tablet pequeña: Grid de 2 columnas
            CharacterGrid(columns = 2, uiState, navigateToDetail)
        }
        WindowWidthSizeClass.Expanded -> {
            // Tablet grande: Grid de 3 columnas o Master-Detail
            CharacterMasterDetail(uiState, navigateToDetail)
        }
    }
}

@Composable
fun CharacterGrid(
    columns: Int,
    uiState: HomeUiState,
    navigateToDetail: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(uiState.characters) { character ->
            CharacterCard(character, navigateToDetail)
        }
    }
}
```

### Ventajas de usar WindowSizeClass:

✅ **Experiencia adaptativa**: Tu app se ve bien en cualquier dispositivo
✅ **Mejor UX en tablets**: Aprovecha el espacio disponible
✅ **Soporte para plegables**: Se adapta cuando el usuario despliega el dispositivo
✅ **Material Design 3**: Sigue las guías oficiales de Google

### Ejemplo completo de uso adaptativo:

```kotlin
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.window.core.layout.WindowWidthSizeClass

@Composable
fun AdaptiveHomeScreen() {
    val adaptiveInfo = currentWindowAdaptiveInfo()
    val windowSizeClass = adaptiveInfo.windowSizeClass
    
    Row(modifier = Modifier.fillMaxSize()) {
        // Navegación lateral solo en pantallas grandes
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.EXPANDED) {
            NavigationRail()
        }
        
        // Contenido principal
        when (windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.COMPACT -> SinglePaneContent()
            WindowWidthSizeClass.MEDIUM -> TwoPaneContent()
            WindowWidthSizeClass.EXPANDED -> ThreePaneContent()
        }
    }
}
```

---

## Resumen

- **ComposeHotReload**: Para desarrollo rápido con actualizaciones instantáneas
- **WindowSizeClass**: Para crear UIs adaptativas según el tamaño de pantalla

Ambas herramientas son esenciales para desarrollar aplicaciones modernas de Android con Jetpack Compose.

