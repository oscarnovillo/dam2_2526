# Guía Completa de NavOptions en Jetpack Compose Navigation

## 🧭 ¿Qué son las NavOptions?

Las `navOptions` te permiten controlar cómo se comporta la navegación: limpiar la pila, evitar duplicados, animaciones personalizadas, etc.

---

## 📝 Sintaxis básica

```kotlin
navController.navigate(DestinoRoute) {
    // Aquí van las navOptions
    popUpTo(RutaDestino) { inclusive = true }
    launchSingleTop = true
    restoreState = true
}
```

---

## 🔧 Opciones disponibles

### 1. **popUpTo** - Limpiar la pila de navegación

Elimina pantallas de la pila hasta llegar a la ruta especificada.

```kotlin
navController.navigate(Login) {
    popUpTo(Home) {
        inclusive = true  // true: elimina también Home | false: mantiene Home
    }
}
```

**Ejemplos prácticos:**

#### a) Logout - Limpiar todo y volver a Login
```kotlin
// En un botón de "Cerrar Sesión"
navController.navigate(Login) {
    popUpTo(0) {  // 0 = inicio del grafo
        inclusive = true
    }
    launchSingleTop = true
}
// Resultado: Pila = [Login]
```

#### b) Volver a Home desde cualquier pantalla
```kotlin
navController.navigate(Home) {
    popUpTo(Home) {
        inclusive = true  // Elimina la instancia anterior de Home
    }
    launchSingleTop = true
}
// Resultado: Pila = [Home] (sin duplicados)
```

#### c) Mantener Login pero limpiar el resto
```kotlin
navController.navigate(Detail) {
    popUpTo(Login) {
        inclusive = false  // Mantiene Login en la pila
    }
}
// Resultado: Pila = [Login, Detail]
```

---

### 2. **launchSingleTop** - Evitar duplicados

Si la pantalla ya está en el tope de la pila, reutilízala en lugar de crear una nueva.

```kotlin
navController.navigate(Profile) {
    launchSingleTop = true
}
```

**Ejemplo:**
```
Pila antes: [Home, Profile]
navigate(Profile) con launchSingleTop = true
Pila después: [Home, Profile] (reutiliza la existente)

Sin launchSingleTop:
Pila después: [Home, Profile, Profile] ❌
```

---

### 3. **restoreState** - Restaurar estado previo

Si navegas a una pantalla que ya visitaste, restaura su estado anterior (scroll, campos de texto, etc.).

```kotlin
navController.navigate(Settings) {
    restoreState = true
}
```

---

### 4. **saveState** - Guardar estado al salir

Guarda el estado de la pantalla actual cuando navegas a otra (útil con BottomNavigation).

```kotlin
navController.navigate(Profile) {
    popUpTo(Home)
    launchSingleTop = true
    restoreState = true
}

// Al volver desde Profile
navController.navigate(Home) {
    popUpTo(Home)
    launchSingleTop = true
    restoreState = true  // Restaura el scroll, etc.
}
```

---

## 🎯 Casos de uso comunes

### **Caso 1: Login → Home (limpiar Login de la pila)**

```kotlin
// En LoginScreen después de autenticar
LoginScreen(
    onLoginSuccess = {
        navController.navigate(Home) {
            popUpTo(Login) {
                inclusive = true  // Elimina Login
            }
            launchSingleTop = true
        }
    }
)
// Resultado: El usuario NO puede volver a Login con el botón back
// Pila: [Home]
```

---

### **Caso 2: Navegación con BottomNavigationBar**

```kotlin
BottomNavigationItem(
    selected = currentRoute == Home,
    onClick = {
        navController.navigate(Home) {
            // Vuelve al inicio del grafo (normalmente el primer tab)
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true  // Guarda el estado
            }
            launchSingleTop = true
            restoreState = true  // Restaura el estado al volver
        }
    }
)
```

**Comportamiento:**
- Al cambiar de tab, guarda el estado (scroll, input, etc.)
- Al volver, restaura el estado anterior
- No crea duplicados de pantallas

---

### **Caso 3: Detail → Login (cerrar sesión desde cualquier pantalla)**

```kotlin
DetailScreen(
    navigateBack = {
        navController.navigate(Login) {
            popUpTo(0) {  // Limpia TODA la pila
                inclusive = true
            }
            launchSingleTop = true
        }
    }
)
// Resultado: Pila completamente limpia, solo queda Login
// Pila: [Login]
```

---

### **Caso 4: Wizard/Formulario multipaso - No permitir volver atrás**

```kotlin
// Paso 1 → Paso 2
navController.navigate(WizardStep2) {
    popUpTo(WizardStep1) {
        inclusive = true  // Elimina Step1
    }
}

// Paso 2 → Paso 3
navController.navigate(WizardStep3) {
    popUpTo(WizardStep2) {
        inclusive = true  // Elimina Step2
    }
}

// Al final: Success
navController.navigate(Success) {
    popUpTo(0) {
        inclusive = true  // Limpia todo el wizard
    }
}
// Resultado: No puede volver atrás en el wizard
```

---

### **Caso 5: DeepLink con pila personalizada**

```kotlin
// Simular que el usuario llegó desde Home
navController.navigate(Detail(id = 123)) {
    popUpTo(Home) {
        inclusive = false  // Mantiene Home
    }
}
// Resultado: Pila = [Home, Detail]
// Al presionar back, va a Home
```

---

## 📊 Comparación de estrategias

| Escenario | Código | Pila resultante |
|-----------|--------|-----------------|
| **Login simple** | `navigate(Home)` | `[Login, Home]` |
| **Login sin retorno** | `navigate(Home) { popUpTo(Login) { inclusive = true } }` | `[Home]` |
| **Tab navigation** | `navigate(Tab) { popUpTo(start) { saveState = true }; restoreState = true }` | `[Tab]` (con estado) |
| **Logout completo** | `navigate(Login) { popUpTo(0) { inclusive = true } }` | `[Login]` |
| **Volver a raíz** | `navigate(Home) { popUpTo(Home) { inclusive = true } }` | `[Home]` |

---

## 🔍 Debugging - Ver la pila de navegación

```kotlin
// En tu Activity/Screen
val backStackEntry = navController.currentBackStackEntry
val backStack = navController.backQueue

Log.d("NavStack", "Current: ${backStackEntry?.destination?.route}")
backStack.forEach { entry ->
    Log.d("NavStack", "Stack: ${entry.destination.route}")
}
```

---

## ⚠️ Errores comunes

### ❌ **Error 1: No usar launchSingleTop**
```kotlin
// Problema: Cada click crea una nueva instancia
bottomNavItem.onClick = {
    navController.navigate(Home)
}
// Pila después de 3 clicks: [Home, Home, Home] ❌
```

✅ **Solución:**
```kotlin
bottomNavItem.onClick = {
    navController.navigate(Home) {
        popUpTo(Home) { inclusive = true }
        launchSingleTop = true
    }
}
// Pila: [Home] ✅
```

---

### ❌ **Error 2: No limpiar Login después de autenticar**
```kotlin
// Problema: El usuario puede volver a Login con back
navController.navigate(Home)
```

✅ **Solución:**
```kotlin
navController.navigate(Home) {
    popUpTo(Login) { inclusive = true }
}
// El botón back desde Home cierra la app, no vuelve a Login ✅
```

---

### ❌ **Error 3: Perder estado en tabs**
```kotlin
// Problema: Cada vez que cambias de tab, pierdes el scroll
navController.navigate(Tab2)
```

✅ **Solución:**
```kotlin
navController.navigate(Tab2) {
    popUpTo(navController.graph.startDestinationId) {
        saveState = true
    }
    launchSingleTop = true
    restoreState = true
}
// Mantiene scroll, input, etc. ✅
```

---

## 🎓 Ejemplo completo - App con Login y Tabs

```kotlin
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = Login) {
        // Login sin back
        composable<Login> {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(MainTabs) {
                        popUpTo(Login) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
        
        // Tabs con estado guardado
        composable<MainTabs> {
            MainTabsScreen(
                onTabClick = { tab ->
                    navController.navigate(tab) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                onLogout = {
                    navController.navigate(Login) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        
        // Detail con back a tabs
        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            DetailScreen(
                name = detail.name,
                navigateBack = {
                    navController.popBackStack()  // Simple back
                }
            )
        }
    }
}
```

---

## 📚 Recursos

- [Documentación oficial](https://developer.android.com/guide/navigation/navigation-navigate)
- [NavOptions API](https://developer.android.com/reference/kotlin/androidx/navigation/NavOptions)
- [Navigation Compose Codelab](https://developer.android.com/codelabs/jetpack-compose-navigation)

---

## 🎯 Resumen

| NavOption | Uso principal |
|-----------|---------------|
| **popUpTo** | Limpiar pantallas de la pila |
| **inclusive** | Incluir o no la pantalla objetivo al limpiar |
| **launchSingleTop** | Evitar duplicados |
| **restoreState** | Restaurar estado previo |
| **saveState** | Guardar estado al salir |

¡Ya sabes cómo controlar la navegación como un profesional! 🚀

