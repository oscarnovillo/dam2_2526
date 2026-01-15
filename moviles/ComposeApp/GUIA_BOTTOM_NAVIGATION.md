# 📱 Implementación de BottomNavigationBar

## 🎯 Resumen

Se ha implementado un **BottomNavigationBar** con 3 tabs:
- 🔐 **Login** - Pantalla de inicio de sesión
- 👤 **Usuario** - Formulario de usuario
- 🏠 **Home** - Lista de personajes de Dragon Ball

La pantalla de **Detail** se mantiene fuera de los tabs y muestra/oculta el BottomBar automáticamente.

---

## 📁 Archivos creados

### 1. **BottomNavItem.kt** - Definición de los items del Bottom Nav

```kotlin
sealed class BottomNavItem(
    val route: Any,
    val title: String,
    val icon: ImageVector
) {
    data object LoginTab : BottomNavItem(
        route = Login,
        title = "Login",
        icon = Icons.Default.Login
    )
    
    data object UserTab : BottomNavItem(
        route = User,
        title = "Usuario",
        icon = Icons.Default.Person
    )
    
    data object HomeTab : BottomNavItem(
        route = Home,
        title = "Home",
        icon = Icons.Default.Home
    )
    
    companion object {
        val items = listOf(LoginTab, UserTab, HomeTab)
    }
}
```

**Características:**
- Sealed class para type-safety
- Cada tab tiene su ruta, título e icono
- Companion object con la lista de todos los items

---

### 2. **MainScaffold.kt** - Scaffold principal con BottomNavigationBar

Este archivo es el corazón de la navegación:

#### **a) Scaffold con BottomBar**

```kotlin
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    
    Scaffold(
        bottomBar = {
            // Solo mostrar el BottomNav si NO estamos en DetailScreen
            val shouldShowBottomBar = currentDestination?.hierarchy?.any { 
                it.hasRoute(Detail::class)
            } != true
            
            if (shouldShowBottomBar) {
                BottomNavigationBar(
                    navController = navController,
                    currentDestination = currentDestination
                )
            }
        }
    ) { paddingValues ->
        NavHost(...) // Contenido
    }
}
```

**Características clave:**
- ✅ **Oculta el BottomBar** automáticamente cuando navegas a Detail
- ✅ **Muestra el BottomBar** en Login, User y Home
- ✅ Usa `paddingValues` para evitar que el contenido quede detrás del BottomBar

---

#### **b) NavHost con las 4 pantallas**

```kotlin
NavHost(
    navController = navController,
    startDestination = Login,
    modifier = Modifier.padding(paddingValues)
) {
    // Tab 1: Login
    composable<Login> {
        LoginScreen(
            navigateToHome = {
                navController.navigate(Home) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        )
    }

    // Tab 2: User
    composable<User> {
        UserFormScreenViewModel()
    }

    // Tab 3: Home
    composable<Home> {
        HomeScreen { name ->
            navController.navigate(Detail(name = name))
        }
    }

    // Pantalla Detail (fuera de tabs)
    composable<Detail> { backStackEntry ->
        val detail: Detail = backStackEntry.toRoute()
        DetailScreen(
            name = detail.name,
            navigateBack = { navController.popBackStack() }
        )
    }
}
```

---

#### **c) BottomNavigationBar Component**

```kotlin
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    currentDestination: androidx.navigation.NavDestination?
) {
    NavigationBar {
        BottomNavItem.items.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any {
                it.hasRoute(item.route::class)
            } == true
            
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        // Pop hasta el inicio del grafo
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        // Evitar múltiples copias del mismo destino
                        launchSingleTop = true
                        // Restaurar el estado al volver a un tab
                        restoreState = true
                    }
                }
            )
        }
    }
}
```

**NavOptions explicadas:**
- **`popUpTo(findStartDestination)`**: Elimina pantallas intermedias al cambiar de tab
- **`saveState = true`**: Guarda el estado del tab al salir (scroll, input, etc.)
- **`launchSingleTop = true`**: No crea duplicados si ya estás en ese tab
- **`restoreState = true`**: Restaura el estado al volver a un tab visitado

---

### 3. **NavigationWrapper.kt** - Actualizado

```kotlin
@Composable
fun NavigationWrapper() {
    MainScaffold()
}
```

Ahora simplemente delega todo a `MainScaffold`.

---

## 🎨 Comportamiento de la navegación

### **Flujo 1: Navegación entre tabs**

```
Login Tab clicked:
  Pila: [Login]
  
User Tab clicked:
  Pila: [Login, User]  // Guarda estado de Login
  
Home Tab clicked:
  Pila: [Login, User, Home]  // Guarda estado de User
  
Login Tab clicked:
  Pila: [Login]  // Restaura estado de Login guardado
```

---

### **Flujo 2: Navegación a Detail (fuera de tabs)**

```
Home → Click en personaje → Detail:
  Pila: [Login, User, Home, Detail]
  BottomBar: OCULTO ❌
  
Detail → Back:
  Pila: [Login, User, Home]
  BottomBar: VISIBLE ✅
```

---

### **Flujo 3: Login → Home**

```
Login → Click "Navegar a la home":
  Pila: [Login, Home]
  Tab actual: Home
  
Home → Back del sistema:
  Pila: [Login]
  Tab actual: Login
```

---

## 🔧 Personalización

### **Cambiar el tab inicial**

```kotlin
NavHost(
    navController = navController,
    startDestination = Home,  // Cambia a Home
    modifier = Modifier.padding(paddingValues)
) { ... }
```

---

### **Agregar más tabs**

1. **Agrega el icono en BottomNavItem.kt:**
```kotlin
data object SettingsTab : BottomNavItem(
    route = Settings,
    title = "Ajustes",
    icon = Icons.Default.Settings
)

companion object {
    val items = listOf(LoginTab, UserTab, HomeTab, SettingsTab)
}
```

2. **Agrega la ruta en Screens.kt:**
```kotlin
@Serializable
object Settings
```

3. **Agrega el composable en MainScaffold.kt:**
```kotlin
composable<Settings> {
    SettingsScreen()
}
```

---

### **Cambiar iconos**

Puedes usar diferentes iconos de Material Icons:

```kotlin
import androidx.compose.material.icons.filled.*

Icons.Default.Home
Icons.Default.Person
Icons.Default.Login
Icons.Default.Settings
Icons.Default.Search
Icons.Default.Favorite
Icons.Default.ShoppingCart
// ... etc
```

O iconos extendidos:

```kotlin
import androidx.compose.material.icons.outlined.*

Icons.Outlined.Home
Icons.Outlined.Person
```

---

### **Personalizar colores del BottomBar**

```kotlin
NavigationBar(
    containerColor = MaterialTheme.colorScheme.primaryContainer,
    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
) {
    // items...
}
```

---

### **Personalizar colores del item seleccionado**

```kotlin
NavigationBarItem(
    icon = { Icon(...) },
    label = { Text(...) },
    selected = isSelected,
    colors = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    ),
    onClick = { ... }
)
```

---

## 🎯 Pantallas con/sin BottomBar

### **Pantallas CON BottomBar:**
- ✅ Login
- ✅ User
- ✅ Home

### **Pantallas SIN BottomBar:**
- ❌ Detail (se oculta automáticamente)

Para ocultar el BottomBar en más pantallas, modifica la condición:

```kotlin
val shouldShowBottomBar = currentDestination?.hierarchy?.any { 
    it.hasRoute(Detail::class) || 
    it.hasRoute(OtraPantalla::class)
} != true
```

---

## 🐛 Solución de problemas comunes

### **Problema 1: El BottomBar no se oculta en Detail**

**Solución:** Verifica que la condición `hasRoute(Detail::class)` esté correcta.

---

### **Problema 2: Se pierde el estado al cambiar de tab**

**Solución:** Asegúrate de tener estas 3 líneas en el onClick:
```kotlin
popUpTo(...) { saveState = true }
launchSingleTop = true
restoreState = true
```

---

### **Problema 3: Se crean múltiples copias del mismo tab**

**Solución:** Agrega `launchSingleTop = true` en las navOptions.

---

### **Problema 4: El contenido queda detrás del BottomBar**

**Solución:** Usa `Modifier.padding(paddingValues)` en el NavHost:
```kotlin
NavHost(
    ...,
    modifier = Modifier.padding(paddingValues)
)
```

---

## 📚 Recursos adicionales

- [BottomNavigation - Material3](https://developer.android.com/reference/kotlin/androidx/compose/material3/package-summary#NavigationBar(androidx.compose.ui.Modifier,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color,androidx.compose.ui.unit.Dp,androidx.compose.foundation.layout.WindowInsets,kotlin.Function1))
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Material Icons](https://fonts.google.com/icons)

---

## ✅ Resumen

Has implementado con éxito un **BottomNavigationBar** con:
- ✅ 3 tabs (Login, User, Home)
- ✅ Navegación a Detail fuera de tabs
- ✅ Ocultar/mostrar BottomBar automáticamente
- ✅ Guardar/restaurar estado de los tabs
- ✅ Evitar duplicados de pantallas
- ✅ Material3 Design

¡Tu app ahora tiene una navegación profesional! 🚀

