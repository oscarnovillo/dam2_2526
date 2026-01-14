# 📊 Arquitectura BottomNavigationBar - Diagrama Visual

## 🏗️ Estructura de archivos

```
app/src/main/java/com/example/composeapp/
│
├── ui/
│   ├── navigation/
│   │   ├── NavigationWrapper.kt          ← Punto de entrada
│   │   ├── MainScaffold.kt              ← Scaffold + BottomNav + NavHost
│   │   ├── BottomNavItem.kt             ← Definición de tabs
│   │   └── Screens.kt                    ← Rutas (Login, Home, User, Detail)
│   │
│   └── screens/
│       ├── LoginScreen.kt               ← Tab 1
│       ├── user/
│       │   └── UserScreen.kt            ← Tab 2
│       └── home/
│           └── HomeScreen.kt            ← Tab 3
│           └── DetailScreen.kt          ← Fuera de tabs
```

---

## 🔄 Flujo de navegación

```
┌─────────────────────────────────────────────────────────┐
│                   MainActivity                          │
│                         ↓                               │
│              NavigationWrapper()                        │
│                         ↓                               │
│                  MainScaffold()                         │
└─────────────────────────────────────────────────────────┘
                          │
                          ├─────────────┬─────────────────┐
                          ↓             ↓                 ↓
            ┌──────────────────┐  ┌──────────┐  ┌──────────────┐
            │   NavHost        │  │ Scaffold │  │ BottomNavBar │
            │   (Contenido)    │  │ padding  │  │  (3 tabs)    │
            └──────────────────┘  └──────────┘  └──────────────┘
                    │
        ┌───────────┼───────────┬──────────┐
        ↓           ↓           ↓          ↓
    ┌───────┐  ┌───────┐  ┌────────┐  ┌────────┐
    │ Login │  │ User  │  │  Home  │  │ Detail │
    │ Tab 1 │  │ Tab 2 │  │  Tab 3 │  │ No tab │
    └───────┘  └───────┘  └────────┘  └────────┘
       ↓          ↓           ↓            ↑
    BottomBar  BottomBar  BottomBar   Sin BottomBar
    visible    visible    visible     (oculto)
```

---

## 🎯 Estados del BottomNavigationBar

### **Estado 1: Login (inicial)**
```
┌──────────────────────────────────────┐
│          LOGIN SCREEN                │
│                                      │
│      [Login Interface]               │
│                                      │
├──────────────────────────────────────┤
│ [🔐 Login]  [👤 User]  [🏠 Home]    │ ← BottomBar
└──────────────────────────────────────┘
    ↑ Selected
```

---

### **Estado 2: User (tab 2)**
```
┌──────────────────────────────────────┐
│         USER FORM SCREEN             │
│                                      │
│      [User Form Fields]              │
│                                      │
├──────────────────────────────────────┤
│ [🔐 Login]  [👤 User]  [🏠 Home]    │ ← BottomBar
└──────────────────────────────────────┘
                ↑ Selected
```

---

### **Estado 3: Home (tab 3)**
```
┌──────────────────────────────────────┐
│    DRAGON BALL CHARACTERS            │
│                                      │
│  [Goku]                              │
│  [Vegeta]                            │
│  [Piccolo]                           │
│                                      │
├──────────────────────────────────────┤
│ [🔐 Login]  [👤 User]  [🏠 Home]    │ ← BottomBar
└──────────────────────────────────────┘
                            ↑ Selected
```

---

### **Estado 4: Detail (sin BottomBar)**
```
┌──────────────────────────────────────┐
│    DETAIL SCREEN: Goku               │
│                                      │
│  [← Back]                            │
│                                      │
│  Name: Goku                          │
│  Race: Saiyan                        │
│  Ki: 9000                            │
│                                      │
│                                      │
└──────────────────────────────────────┘
    ↑ BottomBar OCULTO (no se muestra)
```

---

## 🔀 Flujo de navegación completo

```
App Start
   │
   ↓
┌──────┐
│Login │ ← Tab 1 (startDestination)
└──┬───┘
   │
   │ (Click "Navegar a Home")
   ↓
┌──────┐
│Home  │ ← Tab 3
└──┬───┘
   │
   │ (Click en personaje)
   ↓
┌────────┐
│Detail  │ ← Sin BottomBar
└──┬─────┘
   │
   │ (Click Back)
   ↓
┌──────┐
│Home  │ ← Tab 3 (con BottomBar de nuevo)
└──┬───┘
   │
   │ (Click tab User)
   ↓
┌──────┐
│User  │ ← Tab 2
└──────┘
```

---

## 📦 Componentes principales

### **1. MainScaffold.kt**
```kotlin
MainScaffold()
├── Scaffold
│   ├── bottomBar = { BottomNavigationBar() }
│   └── content = {
│       NavHost
│       ├── composable<Login> { LoginScreen() }
│       ├── composable<User> { UserScreen() }
│       ├── composable<Home> { HomeScreen() }
│       └── composable<Detail> { DetailScreen() }
│   }
```

---

### **2. BottomNavigationBar**
```kotlin
NavigationBar
├── NavigationBarItem (Login)
│   ├── icon = Login
│   ├── label = "Login"
│   └── onClick → navigate(Login)
│
├── NavigationBarItem (User)
│   ├── icon = Person
│   ├── label = "Usuario"
│   └── onClick → navigate(User)
│
└── NavigationBarItem (Home)
    ├── icon = Home
    ├── label = "Home"
    └── onClick → navigate(Home)
```

---

### **3. Lógica de visibilidad del BottomBar**
```kotlin
shouldShowBottomBar = 
    currentDestination?.hierarchy?.any { 
        it.hasRoute(Detail::class)
    } != true

Si estás en Detail:
    shouldShowBottomBar = false → Ocultar BottomBar ❌

Si estás en Login/User/Home:
    shouldShowBottomBar = true → Mostrar BottomBar ✅
```

---

## 🎨 NavOptions en cada navegación

### **Click en tab del BottomBar**
```kotlin
navController.navigate(item.route) {
    popUpTo(navController.graph.findStartDestination().id) {
        saveState = true  ← Guarda scroll, inputs, etc.
    }
    launchSingleTop = true  ← No duplicar si ya estás ahí
    restoreState = true     ← Restaura estado guardado
}
```

**Resultado:**
```
Antes: [Login, Home]
Click User tab:
Después: [Login, User]  (Home guardado en memoria)

Click Home tab:
Después: [Login, Home]  (User guardado, Home restaurado)
```

---

### **Navegación a Detail**
```kotlin
navController.navigate(Detail(name = "Goku"))
```

**Resultado:**
```
Antes: [Login, User, Home]
Después: [Login, User, Home, Detail]
BottomBar: Oculto
```

---

### **Back desde Detail**
```kotlin
navController.popBackStack()
```

**Resultado:**
```
Antes: [Login, User, Home, Detail]
Después: [Login, User, Home]
BottomBar: Visible
```

---

## 📝 Tabla de navegación

| Acción | Pila ANTES | Pila DESPUÉS | BottomBar |
|--------|------------|--------------|-----------|
| App inicia | [] | [Login] | ✅ |
| Login → Home | [Login] | [Login, Home] | ✅ |
| Home → Detail | [Login, Home] | [Login, Home, Detail] | ❌ |
| Detail → Back | [Login, Home, Detail] | [Login, Home] | ✅ |
| Home → User (tab) | [Login, Home] | [Login, User] | ✅ |
| User → Login (tab) | [Login, User] | [Login] | ✅ |

---

## 🔍 Debugging - Ver pila de navegación

Agrega este código para ver la pila actual en Logcat:

```kotlin
@Composable
fun MainScaffold() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Debug: Ver la pila actual
    LaunchedEffect(navBackStackEntry) {
        Log.d("Navigation", "═══════════════════════════")
        Log.d("Navigation", "Current: ${navBackStackEntry?.destination?.route}")
        navController.backQueue.forEach { entry ->
            Log.d("Navigation", "Stack: ${entry.destination.route}")
        }
        Log.d("Navigation", "═══════════════════════════")
    }
    
    // ...resto del código
}
```

**Output en Logcat:**
```
D/Navigation: ═══════════════════════════
D/Navigation: Current: Home
D/Navigation: Stack: Login
D/Navigation: Stack: User
D/Navigation: Stack: Home
D/Navigation: ═══════════════════════════
```

---

## 🎯 Comparación: Con vs Sin BottomBar

### **SIN BottomNavigationBar (antes)**
```
NavigationWrapper
└── NavHost
    ├── Login → Home (manual)
    ├── Home → Detail (manual)
    └── User (manual)

Problemas:
❌ No hay navegación visual
❌ No se guarda el estado
❌ Difícil navegar entre pantallas
```

---

### **CON BottomNavigationBar (ahora)**
```
MainScaffold
├── BottomNavigationBar
│   ├── Login tab
│   ├── User tab
│   └── Home tab
└── NavHost
    ├── Login
    ├── User
    ├── Home
    └── Detail (sin tab)

Ventajas:
✅ Navegación visual intuitiva
✅ Estado guardado automáticamente
✅ Material3 Design
✅ Oculta BottomBar en Detail
```

---

¡Ahora tienes una guía visual completa de cómo funciona tu BottomNavigationBar! 🎉

