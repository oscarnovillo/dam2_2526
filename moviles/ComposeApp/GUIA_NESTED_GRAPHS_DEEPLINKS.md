# 📚 Guía Completa: Nested Graphs y DeepLinks en Jetpack Compose Navigation

## 📖 Índice

### 1. [Nested Graphs (Grafos Anidados)](#-nested-graphs-grafos-anidados)
   - ¿Qué son?
   - ¿Por qué usarlos?
   - Estructura básica
   - Ejemplo con BottomNavigation
   - Navegación entre grafos

### 2. [Múltiples Scaffolds por Grafo](#-múltiples-scaffolds-por-grafo)
   - ¿Se puede tener un Scaffold diferente para cada grafo?
   - Ejemplo: App con diferentes BottomBars por grafo
   - Diagrama visual de la estructura
   - Ejemplo 2: App de E-commerce
   - Ventajas de usar múltiples Scaffolds
   - Navegación entre grafos con diferentes Scaffolds
   - Ejemplo completo: App de Redes Sociales
   - Resumen de patrones

### 3. [Separar Navigation Graphs en diferentes archivos](#-separar-navigation-graphs-en-diferentes-archivos)
   - ¿Se pueden definir los Navigation Graphs en archivos separados?
   - Estructura recomendada del proyecto
   - Paso 1: Definir las rutas por módulo
   - Paso 2: Crear los grafos en archivos separados
   - Paso 3: NavHost principal (AppNavigation.kt)
   - Ejemplo avanzado: E-commerce modular
   - Patrón con ViewModel compartido entre grafos
   - Comparación: Archivo único vs Archivos separados
   - Buenas prácticas (DO's y DON'Ts)
   - Estructura completa de ejemplo
   - Diagrama de flujo

### 4. [Dialog Destinations en Navigation](#-dialog-destinations-en-navigation)
   - ¿Qué son los Dialog Destinations?
   - Ventajas sobre BottomSheet en la Screen
   - Sintaxis básica
   - Ejemplo 1: Diálogo de confirmación simple
   - Ejemplo 2: Recibir resultados del diálogo
   - Ejemplo 3: BottomSheet como Dialog Destination
   - Ejemplo 4: Wizard con múltiples diálogos
   - Ejemplo 5: Diálogo de selección con resultado
   - Ejemplo 6: Diálogo con validación
   - Ejemplo 7: Custom Dialog con animaciones
   - DialogProperties: Opciones avanzadas
   - Patrón: Helper para manejar resultados
   - Mejores prácticas (DO's y DON'Ts)
   - Casos de uso comunes
   - Comparación con enfoques tradicionales

### 5. [DeepLinks (Enlaces Profundos)](#-deeplinks-enlaces-profundos)
   - ¿Qué son?
   - Tipos de DeepLinks
     - Web DeepLinks (http/https)
     - App Links (Android - Verificados)
     - Custom Schemes
   - Ejemplo completo con parámetros
   - Parámetros opcionales y query params
   - Manejar DeepLinks en la Activity

### 6. [Combinación: Nested Graphs + DeepLinks](#-combinación-nested-graphs--deeplinks)
   - Ejemplo avanzado: App de Comercio Electrónico
   - Flujo con DeepLink
   - AndroidManifest.xml completo

### 7. [Casos de Uso Reales](#-casos-de-uso-reales)
   - Notificación Push → Pantalla específica
   - Email Marketing
   - QR Code Scanner
   - Compartir en Redes Sociales
   - Universal Links (iOS/Android)

### 8. [Mejores Prácticas](#-mejores-prácticas)
   - DO's (Hazlo así)
   - DON'Ts (Evita esto)
   - Seguridad con DeepLinks

### 9. [Testing DeepLinks](#-testing-deeplinks)
   - Test de navegación
   - Probar DeepLinks en desarrollo

### 10. [Recursos adicionales](#-recursos-adicionales)
   - Comparación de features
   - Diagrama Visual Completo
   - Links a documentación oficial

---

## 🔗 Nested Graphs (Grafos Anidados)

### ¿Qué son?

Los **Nested Graphs** permiten **agrupar pantallas relacionadas** en un sub-grafo de navegación. Es como crear "mini-aplicaciones" dentro de tu app principal.

### ¿Por qué usarlos?

✅ **Organización**: Agrupa pantallas por feature/módulo  
✅ **Encapsulación**: Cada grafo maneja su propia navegación  
✅ **Reutilización**: Puedes usar el mismo grafo en diferentes partes  
✅ **Scope**: Variables/estados compartidos solo en el grafo  
✅ **Seguridad**: Limita el acceso entre features  

### Estructura básica

```kotlin
// Define los grafos como objetos serializables
@Serializable
object AuthGraph

@Serializable
object MainGraph

@Serializable
object Login

@Serializable
object Register

@Serializable
object Home

@Serializable
object Profile

// Implementación
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = AuthGraph
    ) {
        // 🔐 GRAFO DE AUTENTICACIÓN
        navigation<AuthGraph>(
            startDestination = Login
        ) {
            composable<Login> {
                LoginScreen(
                    onLoginSuccess = {
                        // Navega al grafo principal
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Register)
                    }
                )
            }
            
            composable<Register> {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }
        
        // 🏠 GRAFO PRINCIPAL
        navigation<MainGraph>(
            startDestination = Home
        ) {
            composable<Home> {
                HomeScreen(
                    onProfileClick = {
                        navController.navigate(Profile)
                    }
                )
            }
            
            composable<Profile> {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(AuthGraph) {
                            popUpTo(MainGraph) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
```

### Ejemplo visual de la estructura

```
NavHost (startDestination = AuthGraph)
│
├── 🔐 AuthGraph (Grafo de Autenticación)
│   ├── Login (startDestination)
│   └── Register
│
└── 🏠 MainGraph (Grafo Principal)
    ├── Home (startDestination)
    ├── Profile
    └── Settings
```

### Ejemplo con BottomNavigation

```kotlin
@Serializable object ShopGraph
@Serializable object CartGraph
@Serializable object ProductList
@Serializable data class ProductDetail(val id: String)
@Serializable object Cart
@Serializable object Checkout

@Composable
fun ShopNavigation() {
    val navController = rememberNavController()
    
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ShopGraph) },
                    icon = { Icon(Icons.Default.ShoppingBag, null) },
                    label = { Text("Tienda") }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(CartGraph) },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Carrito") }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = ShopGraph,
            modifier = Modifier.padding(padding)
        ) {
            // 🛍️ GRAFO DE TIENDA
            navigation<ShopGraph>(startDestination = ProductList) {
                composable<ProductList> {
                    ProductListScreen { productId ->
                        navController.navigate(ProductDetail(productId))
                    }
                }
                
                composable<ProductDetail> { backStackEntry ->
                    val detail: ProductDetail = backStackEntry.toRoute()
                    ProductDetailScreen(
                        productId = detail.id,
                        onAddToCart = {
                            navController.navigate(CartGraph)
                        }
                    )
                }
            }
            
            // 🛒 GRAFO DE CARRITO
            navigation<CartGraph>(startDestination = Cart) {
                composable<Cart> {
                    CartScreen {
                        navController.navigate(Checkout)
                    }
                }
                
                composable<Checkout> {
                    CheckoutScreen()
                }
            }
        }
    }
}
```

### Navegación entre grafos

```kotlin
// Desde Login (AuthGraph) → MainGraph
navController.navigate(MainGraph) {
    popUpTo(AuthGraph) { 
        inclusive = true  // Elimina todo el AuthGraph
    }
    launchSingleTop = true
}

// Desde MainGraph → AuthGraph (Logout)
navController.navigate(AuthGraph) {
    popUpTo(MainGraph) { 
        inclusive = true  // Elimina todo el MainGraph
    }
    launchSingleTop = true
}

// Navegar dentro del mismo grafo
navController.navigate(Register)  // Simple
```

---

## 🎨 Múltiples Scaffolds por Grafo

### ¿Se puede tener un Scaffold diferente para cada grafo?

**¡SÍ!** Es una práctica **muy común y profesional** tener diferentes Scaffolds (y diferentes BottomBars) para cada grafo. Esto te permite:

✅ **UI específica por módulo**: Cada feature tiene su propia interfaz  
✅ **Mejor UX**: Navegación contextual según el área de la app  
✅ **Separación de responsabilidades**: Cada Scaffold maneja su lógica  
✅ **Escalabilidad**: Fácil agregar nuevos módulos  
✅ **Testing**: Cada Scaffold se puede probar independientemente  

### Ejemplo: App con diferentes BottomBars por grafo

```kotlin
@Serializable object AuthGraph
@Serializable object MainGraph
@Serializable object AdminGraph

@Serializable object Login
@Serializable object Register

@Serializable object Home
@Serializable object Profile
@Serializable object Settings

@Serializable object AdminPanel
@Serializable object Users
@Serializable object Reports

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = AuthGraph
    ) {
        // 🔐 AUTH GRAPH - SIN BottomBar
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login> {
                // Scaffold simple, sin BottomBar
                Scaffold { padding ->
                    LoginScreen(
                        modifier = Modifier.padding(padding),
                        onLoginSuccess = { isAdmin ->
                            if (isAdmin) {
                                navController.navigate(AdminGraph) {
                                    popUpTo(AuthGraph) { inclusive = true }
                                }
                            } else {
                                navController.navigate(MainGraph) {
                                    popUpTo(AuthGraph) { inclusive = true }
                                }
                            }
                        },
                        onRegisterClick = {
                            navController.navigate(Register)
                        }
                    )
                }
            }
            
            composable<Register> {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Registro") },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.Default.ArrowBack, null)
                                }
                            }
                        )
                    }
                ) { padding ->
                    RegisterScreen(Modifier.padding(padding))
                }
            }
        }
        
        // 🏠 MAIN GRAPH - BottomBar con Home/Profile/Settings
        navigation<MainGraph>(startDestination = Home) {
            composable<Home> {
                MainScaffold(
                    navController = navController,
                    currentRoute = Home::class
                )
            }
            
            composable<Profile> {
                MainScaffold(
                    navController = navController,
                    currentRoute = Profile::class
                )
            }
            
            composable<Settings> {
                MainScaffold(
                    navController = navController,
                    currentRoute = Settings::class
                )
            }
        }
        
        // ⚙️ ADMIN GRAPH - BottomBar diferente (estilo admin)
        navigation<AdminGraph>(startDestination = AdminPanel) {
            composable<AdminPanel> {
                AdminScaffold(
                    navController = navController,
                    currentRoute = AdminPanel::class
                )
            }
            
            composable<Users> {
                AdminScaffold(
                    navController = navController,
                    currentRoute = Users::class
                )
            }
            
            composable<Reports> {
                AdminScaffold(
                    navController = navController,
                    currentRoute = Reports::class
                )
            }
        }
    }
}

// 👤 Scaffold para usuarios normales
@Composable
fun MainScaffold(
    navController: NavController,
    currentRoute: KClass<*>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi App") },
                actions = {
                    IconButton(onClick = { /* Notificaciones */ }) {
                        Icon(Icons.Default.Notifications, null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Home::class,
                    onClick = { 
                        navController.navigate(Home) {
                            popUpTo(Home) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Inicio") }
                )
                NavigationBarItem(
                    selected = currentRoute == Profile::class,
                    onClick = { 
                        navController.navigate(Profile) {
                            popUpTo(Home)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Perfil") }
                )
                NavigationBarItem(
                    selected = currentRoute == Settings::class,
                    onClick = { 
                        navController.navigate(Settings) {
                            popUpTo(Home)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, null) },
                    label = { Text("Ajustes") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                Home::class -> HomeScreen()
                Profile::class -> ProfileScreen(
                    onLogout = {
                        navController.navigate(AuthGraph) {
                            popUpTo(MainGraph) { inclusive = true }
                        }
                    }
                )
                Settings::class -> SettingsScreen()
            }
        }
    }
}

// 🛡️ Scaffold para administradores (diseño diferente)
@Composable
fun AdminScaffold(
    navController: NavController,
    currentRoute: KClass<*>
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, null, tint = Color.Yellow)
                        Spacer(Modifier.width(8.dp))
                        Text("Panel Administrador")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        navController.navigate(AuthGraph) {
                            popUpTo(AdminGraph) { inclusive = true }
                        }
                    }) {
                        Icon(Icons.Default.Logout, null, tint = Color.White)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ) {
                NavigationBarItem(
                    selected = currentRoute == AdminPanel::class,
                    onClick = { 
                        navController.navigate(AdminPanel) {
                            popUpTo(AdminPanel) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Dashboard, null) },
                    label = { Text("Panel") }
                )
                NavigationBarItem(
                    selected = currentRoute == Users::class,
                    onClick = { 
                        navController.navigate(Users) {
                            popUpTo(AdminPanel)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.People, null) },
                    label = { Text("Usuarios") }
                )
                NavigationBarItem(
                    selected = currentRoute == Reports::class,
                    onClick = { 
                        navController.navigate(Reports) {
                            popUpTo(AdminPanel)
                            launchSingleTop = true
                        }
                    },
                    icon = { Icon(Icons.Default.Assessment, null) },
                    label = { Text("Reportes") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                AdminPanel::class -> AdminPanelScreen()
                Users::class -> UsersManagementScreen()
                Reports::class -> ReportsScreen()
            }
        }
    }
}
```

### Diagrama visual de la estructura

```
App con Múltiples Scaffolds
│
├── AuthGraph (Sin BottomBar)
│   ├── Login ──────► Scaffold simple
│   └── Register ───► Scaffold con TopBar
│
├── MainGraph (BottomBar: Home/Profile/Settings)
│   ├── Home ───────► MainScaffold (azul)
│   ├── Profile ────► MainScaffold (azul)
│   └── Settings ───► MainScaffold (azul)
│
└── AdminGraph (BottomBar: Panel/Users/Reports)
    ├── AdminPanel ─► AdminScaffold (rojo)
    ├── Users ──────► AdminScaffold (rojo)
    └── Reports ────► AdminScaffold (rojo)
```

### Ejemplo 2: App de E-commerce

```kotlin
@Serializable object ShopGraph
@Serializable object CartGraph
@Serializable object ProfileGraph

@Composable
fun EcommerceApp() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = ShopGraph) {
        // 🛍️ SHOP GRAPH - BottomBar: Tienda/Categorías
        navigation<ShopGraph>(startDestination = ProductList) {
            composable<ProductList> {
                ShopScaffold(navController, ProductList::class)
            }
            composable<Categories> {
                ShopScaffold(navController, Categories::class)
            }
        }
        
        // 🛒 CART GRAPH - BottomBar: Carrito/Favoritos
        navigation<CartGraph>(startDestination = Cart) {
            composable<Cart> {
                CartScaffold(navController, Cart::class)
            }
            composable<Favorites> {
                CartScaffold(navController, Favorites::class)
            }
        }
        
        // 👤 PROFILE GRAPH - BottomBar: Perfil/Pedidos/Direcciones
        navigation<ProfileGraph>(startDestination = Profile) {
            composable<Profile> {
                ProfileScaffold(navController, Profile::class)
            }
            composable<Orders> {
                ProfileScaffold(navController, Orders::class)
            }
            composable<Addresses> {
                ProfileScaffold(navController, Addresses::class)
            }
        }
    }
}

@Composable
fun ShopScaffold(navController: NavController, currentRoute: KClass<*>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tienda") },
                actions = {
                    IconButton(onClick = { navController.navigate(CartGraph) }) {
                        Badge(badgeContent = { Text("3") }) {
                            Icon(Icons.Default.ShoppingCart, null)
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == ProductList::class,
                    onClick = { navController.navigate(ProductList) },
                    icon = { Icon(Icons.Default.Store, null) },
                    label = { Text("Productos") }
                )
                NavigationBarItem(
                    selected = currentRoute == Categories::class,
                    onClick = { navController.navigate(Categories) },
                    icon = { Icon(Icons.Default.Category, null) },
                    label = { Text("Categorías") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate(CartGraph) }) {
                Icon(Icons.Default.ShoppingCart, null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                ProductList::class -> ProductListScreen()
                Categories::class -> CategoriesScreen()
            }
        }
    }
}

@Composable
fun CartScaffold(navController: NavController, currentRoute: KClass<*>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Carrito") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(ShopGraph) }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Cart::class,
                    onClick = { navController.navigate(Cart) },
                    icon = { Icon(Icons.Default.ShoppingCart, null) },
                    label = { Text("Carrito") }
                )
                NavigationBarItem(
                    selected = currentRoute == Favorites::class,
                    onClick = { navController.navigate(Favorites) },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Favoritos") }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                Cart::class -> CartScreen()
                Favorites::class -> FavoritesScreen()
            }
        }
    }
}
```

### Ventajas de usar múltiples Scaffolds

| Ventaja | Descripción | Ejemplo |
|---------|-------------|---------|
| **Contexto específico** | Cada módulo tiene su UI apropiada | Admin con colores de alerta (rojo) |
| **BottomBars diferentes** | Tabs relevantes por sección | Shop: Productos/Categorías, Cart: Carrito/Favoritos |
| **TopBars personalizados** | Títulos y acciones contextuales | Tienda con icono de carrito, Admin con logout |
| **FABs condicionales** | FloatingActionButton solo donde se necesita | FAB de "Añadir al carrito" solo en ShopGraph |
| **Temas diferentes** | Colores distintos por rol | Usuario normal (azul), Admin (rojo) |

### Navegación entre grafos con diferentes Scaffolds

```kotlin
// Usuario normal → Panel Admin
Button(onClick = {
    navController.navigate(AdminGraph) {
        popUpTo(MainGraph) { inclusive = true }
    }
}) {
    Text("Ir a Panel Admin")
}
// Resultado: Cambia de MainScaffold (azul) a AdminScaffold (rojo)

// Admin → Vista de usuario
IconButton(onClick = {
    navController.navigate(MainGraph) {
        popUpTo(AdminGraph) { inclusive = true }
    }
}) {
    Icon(Icons.Default.Person, null)
}
// Resultado: Cambia de AdminScaffold (rojo) a MainScaffold (azul)

// Cualquier grafo → Login
Button(onClick = {
    navController.navigate(AuthGraph) {
        popUpTo(0) { inclusive = true }
    }
}) {
    Text("Cerrar Sesión")
}
// Resultado: Vuelve a AuthGraph (sin BottomBar)
```

### Ejemplo completo: App de Redes Sociales

```kotlin
@Composable
fun SocialMediaApp() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = FeedGraph) {
        // 📱 FEED GRAPH - BottomBar: Feed/Search/Notifications/Profile
        navigation<FeedGraph>(startDestination = Feed) {
            composable<Feed> {
                FeedScaffold(navController, Feed::class)
            }
            composable<Search> {
                FeedScaffold(navController, Search::class)
            }
            composable<Notifications> {
                FeedScaffold(navController, Notifications::class)
            }
            composable<Profile> {
                FeedScaffold(navController, Profile::class)
            }
        }
        
        // 💬 MESSAGES GRAPH - BottomBar: Chats/Calls/Stories
        navigation<MessagesGraph>(startDestination = Chats) {
            composable<Chats> {
                MessagesScaffold(navController, Chats::class)
            }
            composable<Calls> {
                MessagesScaffold(navController, Calls::class)
            }
            composable<Stories> {
                MessagesScaffold(navController, Stories::class)
            }
        }
        
        // 📹 REELS GRAPH - Sin BottomBar (pantalla completa)
        navigation<ReelsGraph>(startDestination = ReelsList) {
            composable<ReelsList> {
                Scaffold { padding ->
                    ReelsScreen(
                        modifier = Modifier.padding(padding),
                        onClose = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
fun FeedScaffold(navController: NavController, currentRoute: KClass<*>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Instagram") },
                actions = {
                    IconButton(onClick = { navController.navigate(MessagesGraph) }) {
                        Icon(Icons.Default.Message, null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Feed::class,
                    onClick = { navController.navigate(Feed) },
                    icon = { Icon(Icons.Default.Home, null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Search::class,
                    onClick = { navController.navigate(Search) },
                    icon = { Icon(Icons.Default.Search, null) }
                )
                NavigationBarItem(
                    selected = false,
                    onClick = { navController.navigate(ReelsGraph) },
                    icon = { Icon(Icons.Default.VideoLibrary, null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Notifications::class,
                    onClick = { navController.navigate(Notifications) },
                    icon = { Icon(Icons.Default.Favorite, null) }
                )
                NavigationBarItem(
                    selected = currentRoute == Profile::class,
                    onClick = { navController.navigate(Profile) },
                    icon = { Icon(Icons.Default.Person, null) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                Feed::class -> FeedScreen()
                Search::class -> SearchScreen()
                Notifications::class -> NotificationsScreen()
                Profile::class -> ProfileScreen()
            }
        }
    }
}

@Composable
fun MessagesScaffold(navController: NavController, currentRoute: KClass<*>) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mensajes") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigate(FeedGraph) }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentRoute == Chats::class,
                    onClick = { navController.navigate(Chats) },
                    icon = { Icon(Icons.Default.Chat, null) },
                    label = { Text("Chats") }
                )
                NavigationBarItem(
                    selected = currentRoute == Calls::class,
                    onClick = { navController.navigate(Calls) },
                    icon = { Icon(Icons.Default.Call, null) },
                    label = { Text("Llamadas") }
                )
                NavigationBarItem(
                    selected = currentRoute == Stories::class,
                    onClick = { navController.navigate(Stories) },
                    icon = { Icon(Icons.Default.Movie, null) },
                    label = { Text("Historias") }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Nuevo chat */ }) {
                Icon(Icons.Default.Edit, null)
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (currentRoute) {
                Chats::class -> ChatsScreen()
                Calls::class -> CallsScreen()
                Stories::class -> StoriesScreen()
            }
        }
    }
}
```

### Resumen de patrones

```kotlin
// Patrón 1: Grafo SIN UI extra (solo contenido)
navigation<AuthGraph>(startDestination = Login) {
    composable<Login> {
        Scaffold { padding ->
            LoginScreen(Modifier.padding(padding))
        }
    }
}

// Patrón 2: Grafo CON Scaffold compartido
navigation<MainGraph>(startDestination = Home) {
    composable<Home> {
        MainScaffold(navController, Home::class)
    }
    composable<Profile> {
        MainScaffold(navController, Profile::class)
    }
}

// Patrón 3: Grafo CON Scaffold único (pantalla completa)
navigation<DetailGraph>(startDestination = Detail) {
    composable<Detail> {
        Scaffold(
            topBar = { DetailTopBar() }
        ) { padding ->
            DetailContent(Modifier.padding(padding))
        }
    }
}
```

---

## 📂 Separar Navigation Graphs en diferentes archivos

### ¿Se pueden definir los Navigation Graphs en archivos separados?

**¡SÍ! Y es una EXCELENTE PRÁCTICA.** Separar cada grafo de navegación en archivos diferentes mejora significativamente:

✅ **Organización**: Código estructurado por features/módulos  
✅ **Escalabilidad**: Fácil agregar nuevos módulos sin tocar código existente  
✅ **Mantenibilidad**: Cambios localizados sin afectar otros grafos  
✅ **Trabajo en equipo**: Sin conflictos de merge entre desarrolladores  
✅ **Testing**: Cada grafo se puede testear independientemente  
✅ **Reutilización**: Grafos pueden usarse en diferentes apps  
✅ **Legibilidad**: Archivos más pequeños y enfocados  

### Estructura recomendada del proyecto

```
app/src/main/java/com/example/composeapp/
└── ui/
    └── navigation/
        ├── AppNavigation.kt              // NavHost principal (punto de entrada)
        ├── graphs/                       // Grafos de navegación
        │   ├── AuthNavGraph.kt          // Grafo de autenticación
        │   ├── MainNavGraph.kt          // Grafo principal (Home, Profile, Settings)
        │   ├── AdminNavGraph.kt         // Grafo de administración
        │   └── ShopNavGraph.kt          // Grafo de tienda (E-commerce)
        ├── routes/                       // Definición de rutas
        │   ├── AuthRoutes.kt            // Routes: Login, Register, etc.
        │   ├── MainRoutes.kt            // Routes: Home, Profile, Settings
        │   ├── AdminRoutes.kt           // Routes: AdminPanel, Users, etc.
        │   └── ShopRoutes.kt            // Routes: ProductList, Cart, etc.
        └── BottomNavItem.kt             // Items del BottomNavigationBar
```

---

### Paso 1: Definir las rutas por módulo

```kotlin
// routes/AuthRoutes.kt
package com.example.composeapp.ui.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
object AuthGraph

@Serializable
object Login

@Serializable
object Register

@Serializable
object ForgotPassword
```

```kotlin
// routes/MainRoutes.kt
package com.example.composeapp.ui.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
object MainGraph

@Serializable
object Home

@Serializable
object Profile

@Serializable
object Settings

@Serializable
data class Detail(val name: String)
```

```kotlin
// routes/AdminRoutes.kt
package com.example.composeapp.ui.navigation.routes

import kotlinx.serialization.Serializable

@Serializable
object AdminGraph

@Serializable
object AdminPanel

@Serializable
object UsersManagement

@Serializable
object Reports
```

---

### Paso 2: Crear los grafos en archivos separados

```kotlin
// graphs/AuthNavGraph.kt
package com.example.composeapp.ui.navigation.graphs

import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.composeapp.ui.navigation.routes.*
import com.example.composeapp.ui.screens.*

/**
 * Grafo de autenticación
 * Contiene: Login, Register, ForgotPassword
 */
fun NavGraphBuilder.authNavGraph(
    navController: NavHostController
) {
    navigation<AuthGraph>(
        startDestination = Login
    ) {
        composable<Login> {
            Scaffold { padding ->
                LoginScreen(
                    modifier = Modifier.padding(padding),
                    onLoginSuccess = { isAdmin ->
                        val destination = if (isAdmin) AdminGraph else MainGraph
                        navController.navigate(destination) {
                            popUpTo(AuthGraph) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(Register)
                    },
                    onForgotPasswordClick = {
                        navController.navigate(ForgotPassword)
                    }
                )
            }
        }

        composable<Register> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Crear Cuenta") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { padding ->
                RegisterScreen(
                    modifier = Modifier.padding(padding),
                    onRegisterSuccess = {
                        navController.navigate(MainGraph) {
                            popUpTo(AuthGraph) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
        }

        composable<ForgotPassword> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Recuperar Contraseña") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { padding ->
                ForgotPasswordScreen(
                    modifier = Modifier.padding(padding),
                    onPasswordReset = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
```

```kotlin
// graphs/MainNavGraph.kt
package com.example.composeapp.ui.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.composeapp.ui.navigation.routes.*
import com.example.composeapp.ui.screens.*
import com.example.composeapp.ui.navigation.MainScaffold

/**
 * Grafo principal de la aplicación
 * Contiene: Home, Profile, Settings, Detail
 * Usa MainScaffold con BottomNavigationBar
 */
fun NavGraphBuilder.mainNavGraph(
    navController: NavHostController
) {
    navigation<MainGraph>(
        startDestination = Home
    ) {
        composable<Home> {
            MainScaffold(
                navController = navController,
                currentRoute = Home::class
            ) {
                HomeScreen(
                    onPersonClick = { name ->
                        navController.navigate(Detail(name))
                    }
                )
            }
        }

        composable<Profile> {
            MainScaffold(
                navController = navController,
                currentRoute = Profile::class
            ) {
                ProfileScreen(
                    onLogoutClick = {
                        navController.navigate(AuthGraph) {
                            popUpTo(MainGraph) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable<Settings> {
            MainScaffold(
                navController = navController,
                currentRoute = Settings::class
            ) {
                SettingsScreen()
            }
        }

        // Detail no usa BottomBar
        composable<Detail> { backStackEntry ->
            val detail: Detail = backStackEntry.toRoute()
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(detail.name) },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { padding ->
                DetailScreen(
                    name = detail.name,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
```

```kotlin
// graphs/AdminNavGraph.kt
package com.example.composeapp.ui.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.composeapp.ui.navigation.routes.*
import com.example.composeapp.ui.screens.admin.*
import com.example.composeapp.ui.navigation.AdminScaffold

/**
 * Grafo de administración
 * Contiene: AdminPanel, UsersManagement, Reports
 * Usa AdminScaffold con BottomBar personalizado (rojo)
 */
fun NavGraphBuilder.adminNavGraph(
    navController: NavHostController
) {
    navigation<AdminGraph>(
        startDestination = AdminPanel
    ) {
        composable<AdminPanel> {
            AdminScaffold(
                navController = navController,
                currentRoute = AdminPanel::class
            ) {
                AdminPanelScreen(
                    onUsersClick = {
                        navController.navigate(UsersManagement)
                    },
                    onReportsClick = {
                        navController.navigate(Reports)
                    }
                )
            }
        }

        composable<UsersManagement> {
            AdminScaffold(
                navController = navController,
                currentRoute = UsersManagement::class
            ) {
                UsersManagementScreen()
            }
        }

        composable<Reports> {
            AdminScaffold(
                navController = navController,
                currentRoute = Reports::class
            ) {
                ReportsScreen()
            }
        }
    }
}
```

---

### Paso 3: NavHost principal (AppNavigation.kt)

```kotlin
// AppNavigation.kt
package com.example.composeapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.composeapp.ui.navigation.graphs.*
import com.example.composeapp.ui.navigation.routes.AuthGraph

/**
 * Punto de entrada principal de la navegación
 * Coordina todos los grafos de la aplicación
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AuthGraph
    ) {
        // 🔐 Grafo de autenticación (Login, Register, ForgotPassword)
        authNavGraph(navController)

        // 🏠 Grafo principal (Home, Profile, Settings, Detail)
        mainNavGraph(navController)

        // ⚙️ Grafo de administración (AdminPanel, Users, Reports)
        adminNavGraph(navController)
    }
}
```

**Ventajas de este archivo:**
- ✅ **Punto único de entrada**: Fácil de encontrar y entender
- ✅ **Vista global**: Se ve toda la navegación de la app
- ✅ **Mínimo código**: Solo coordina, no implementa
- ✅ **Fácil de testear**: Mock de los grafos individuales

---

### Ejemplo avanzado: E-commerce modular

```kotlin
// graphs/ShopNavGraph.kt
package com.example.composeapp.ui.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import com.example.composeapp.ui.navigation.routes.*
import com.example.composeapp.ui.screens.shop.*

/**
 * Grafo de tienda
 * Contiene: ProductList, ProductDetail, Categories
 */
fun NavGraphBuilder.shopNavGraph(
    navController: NavHostController
) {
    navigation<ShopGraph>(startDestination = ProductList) {
        composable<ProductList> {
            ShopScaffold(
                navController = navController,
                currentRoute = ProductList::class
            ) {
                ProductListScreen(
                    onProductClick = { productId ->
                        navController.navigate(ProductDetail(productId))
                    },
                    onCategoriesClick = {
                        navController.navigate(Categories)
                    }
                )
            }
        }

        composable<ProductDetail> { backStackEntry ->
            val detail: ProductDetail = backStackEntry.toRoute()
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Producto") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { padding ->
                ProductDetailScreen(
                    productId = detail.productId,
                    modifier = Modifier.padding(padding),
                    onAddToCart = {
                        navController.navigate(CartGraph)
                    }
                )
            }
        }

        composable<Categories> {
            ShopScaffold(
                navController = navController,
                currentRoute = Categories::class
            ) {
                CategoriesScreen()
            }
        }
    }
}
```

```kotlin
// graphs/CartNavGraph.kt
package com.example.composeapp.ui.navigation.graphs

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.composeapp.ui.navigation.routes.*
import com.example.composeapp.ui.screens.cart.*

/**
 * Grafo de carrito de compras
 * Contiene: Cart, Checkout, OrderConfirmation
 */
fun NavGraphBuilder.cartNavGraph(
    navController: NavHostController
) {
    navigation<CartGraph>(startDestination = Cart) {
        composable<Cart> {
            CartScaffold(
                navController = navController,
                currentRoute = Cart::class
            ) {
                CartScreen(
                    onCheckoutClick = {
                        navController.navigate(Checkout)
                    }
                )
            }
        }

        composable<Checkout> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Checkout") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(Icons.Default.ArrowBack, null)
                            }
                        }
                    )
                }
            ) { padding ->
                CheckoutScreen(
                    modifier = Modifier.padding(padding),
                    onPaymentSuccess = {
                        navController.navigate(OrderConfirmation) {
                            popUpTo(Cart) { inclusive = true }
                        }
                    }
                )
            }
        }

        composable<OrderConfirmation> {
            OrderConfirmationScreen(
                onContinueShopping = {
                    navController.navigate(ShopGraph) {
                        popUpTo(CartGraph) { inclusive = true }
                    }
                }
            )
        }
    }
}
```

```kotlin
// AppNavigation.kt - E-commerce
@Composable
fun EcommerceAppNavigation() {
    val navController = rememberNavController()

    NavHost(navController, startDestination = ShopGraph) {
        authNavGraph(navController)      // 🔐 Login, Register
        shopNavGraph(navController)      // 🛍️ Products, Categories
        cartNavGraph(navController)      // 🛒 Cart, Checkout
        profileNavGraph(navController)   // 👤 Profile, Orders
    }
}
```

---

### Patrón con ViewModel compartido entre grafos

A veces necesitas compartir estado entre pantallas del mismo grafo:

```kotlin
// graphs/ShopNavGraph.kt
fun NavGraphBuilder.shopNavGraph(
    navController: NavHostController,
    sharedViewModel: ShopViewModel // ViewModel compartido en el grafo
) {
    navigation<ShopGraph>(startDestination = ProductList) {
        composable<ProductList> {
            ProductListScreen(
                viewModel = sharedViewModel, // Mismo ViewModel
                onProductClick = { id ->
                    navController.navigate(ProductDetail(id))
                }
            )
        }

        composable<ProductDetail> { backStackEntry ->
            val detail: ProductDetail = backStackEntry.toRoute()
            ProductDetailScreen(
                productId = detail.productId,
                viewModel = sharedViewModel, // Mismo ViewModel
                onAddToCart = { product ->
                    sharedViewModel.addToCart(product)
                    navController.popBackStack()
                }
            )
        }
    }
}

// AppNavigation.kt
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val shopViewModel: ShopViewModel = hiltViewModel() // Shared ViewModel
    
    NavHost(navController, startDestination = ShopGraph) {
        shopNavGraph(navController, shopViewModel)
        cartNavGraph(navController)
    }
}
```

---

### Comparación: Archivo único vs Archivos separados

| Aspecto | Archivo único | Archivos separados |
|---------|--------------|-------------------|
| **Organización** | 😐 Todo mezclado | ✅ Por features/módulos |
| **Tamaño del archivo** | ❌ 500+ líneas | ✅ 50-100 líneas cada uno |
| **Escalabilidad** | ❌ Difícil agregar features | ✅ Solo crea nuevo archivo |
| **Trabajo en equipo** | ❌ Muchos conflictos de merge | ✅ Sin conflictos |
| **Mantenibilidad** | ❌ Difícil encontrar código | ✅ Fácil localizar |
| **Testing** | ❌ Test acoplados | ✅ Tests independientes |
| **Reutilización** | ❌ No se puede | ✅ Copiar archivo completo |
| **Legibilidad** | ❌ Difícil de leer | ✅ Claro y conciso |
| **Responsabilidades** | ❌ Mezcladas | ✅ Separadas (SRP) |

---

### Buenas prácticas

#### ✅ DO's (Hazlo así)

```kotlin
// ✅ Cada grafo en su archivo
fun NavGraphBuilder.authNavGraph(navController: NavHostController) { }
fun NavGraphBuilder.mainNavGraph(navController: NavHostController) { }
fun NavGraphBuilder.adminNavGraph(navController: NavHostController) { }

// ✅ Rutas agrupadas por módulo
// routes/AuthRoutes.kt
@Serializable object Login
@Serializable object Register

// ✅ Nombres descriptivos
authNavGraph() // ✅ Claro
mainNavGraph() // ✅ Claro

// ✅ Documentación en cada grafo
/**
 * Grafo de autenticación
 * Contiene: Login, Register, ForgotPassword
 */
fun NavGraphBuilder.authNavGraph(...) { }

// ✅ Un solo NavHost principal
@Composable
fun AppNavigation() {
    NavHost(...) {
        authNavGraph(navController)
        mainNavGraph(navController)
    }
}
```

#### ❌ DON'Ts (Evita esto)

```kotlin
// ❌ Todo en un solo archivo
fun AppNavigation() {
    NavHost(...) {
        // 500 líneas de navegación...
    }
}

// ❌ Nombres genéricos
fun NavGraphBuilder.graph1(...) { }
fun NavGraphBuilder.graph2(...) { }

// ❌ Rutas mezcladas
// Screens.kt con todas las rutas
@Serializable object Login
@Serializable object Home
@Serializable object AdminPanel
// etc... (difícil de encontrar)

// ❌ Múltiples NavHost
@Composable
fun Feature1Navigation() {
    NavHost(...) { } // ❌
}
@Composable
fun Feature2Navigation() {
    NavHost(...) { } // ❌
}
```

---

### Estructura completa de ejemplo

```
app/src/main/java/com/example/composeapp/
│
├── ui/
│   ├── navigation/
│   │   ├── AppNavigation.kt                 // NavHost principal
│   │   │
│   │   ├── graphs/                          // Grafos separados
│   │   │   ├── AuthNavGraph.kt             // Auth: Login, Register
│   │   │   ├── MainNavGraph.kt             // Main: Home, Profile, Settings
│   │   │   ├── AdminNavGraph.kt            // Admin: Panel, Users
│   │   │   ├── ShopNavGraph.kt             // Shop: Products, Categories
│   │   │   └── CartNavGraph.kt             // Cart: Cart, Checkout
│   │   │
│   │   ├── routes/                          // Rutas por módulo
│   │   │   ├── AuthRoutes.kt               // @Serializable Routes
│   │   │   ├── MainRoutes.kt
│   │   │   ├── AdminRoutes.kt
│   │   │   ├── ShopRoutes.kt
│   │   │   └── CartRoutes.kt
│   │   │
│   │   ├── scaffolds/                       // Scaffolds reutilizables
│   │   │   ├── MainScaffold.kt             // Scaffold usuario normal
│   │   │   ├── AdminScaffold.kt            // Scaffold admin
│   │   │   ├── ShopScaffold.kt             // Scaffold tienda
│   │   │   └── CartScaffold.kt             // Scaffold carrito
│   │   │
│   │   └── BottomNavItem.kt                 // Items BottomBar
│   │
│   └── screens/                             // Pantallas agrupadas
│       ├── auth/
│       │   ├── LoginScreen.kt
│       │   ├── RegisterScreen.kt
│       │   └── ForgotPasswordScreen.kt
│       ├── main/
│       │   ├── HomeScreen.kt
│       │   ├── ProfileScreen.kt
│       │   └── SettingsScreen.kt
│       ├── admin/
│       │   ├── AdminPanelScreen.kt
│       │   └── UsersManagementScreen.kt
│       └── shop/
│           ├── ProductListScreen.kt
│           └── ProductDetailScreen.kt
│
└── ...resto de la app
```

---

### Diagrama de flujo

```
MainActivity
    ↓
AppNavigation.kt (NavHost)
    ↓
    ├── authNavGraph(navController)      → graphs/AuthNavGraph.kt
    │   ├── Login                         → screens/auth/LoginScreen.kt
    │   ├── Register                      → screens/auth/RegisterScreen.kt
    │   └── ForgotPassword                → screens/auth/ForgotPasswordScreen.kt
    │
    ├── mainNavGraph(navController)      → graphs/MainNavGraph.kt
    │   ├── Home                          → screens/main/HomeScreen.kt
    │   ├── Profile                       → screens/main/ProfileScreen.kt
    │   └── Settings                      → screens/main/SettingsScreen.kt
    │
    ├── adminNavGraph(navController)     → graphs/AdminNavGraph.kt
    │   ├── AdminPanel                    → screens/admin/AdminPanelScreen.kt
    │   └── UsersManagement               → screens/admin/UsersManagementScreen.kt
    │
    └── shopNavGraph(navController)      → graphs/ShopNavGraph.kt
        ├── ProductList                   → screens/shop/ProductListScreen.kt
        └── ProductDetail                 → screens/shop/ProductDetailScreen.kt
```

---

### Resumen

**Separar los Navigation Graphs es una práctica profesional que:**

✅ Mejora la organización del código  
✅ Facilita el trabajo en equipo  
✅ Hace el código más mantenible  
✅ Permite testing independiente  
✅ Facilita la reutilización  
✅ Escala mejor con el crecimiento de la app  

**Estructura recomendada:**
- 📁 `graphs/` - Un archivo por grafo
- 📁 `routes/` - Rutas agrupadas por módulo
- 📁 `scaffolds/` - Scaffolds reutilizables
- 📄 `AppNavigation.kt` - Coordinador principal

**¡Es la forma correcta de estructurar navegación en apps grandes!** 🚀

---

## 🔔 Dialog Destinations en Navigation

### ¿Qué son los Dialog Destinations?

Los **Dialog Destinations** permiten usar **diálogos como destinos de navegación**, en lugar de pantallas completas. Esto es muy útil para:

✅ **Confirmaciones**: "¿Estás seguro de eliminar?"  
✅ **Formularios simples**: Agregar nota, cambiar nombre  
✅ **Selecciones**: Elegir opciones de una lista  
✅ **Resultados**: Recibir datos del diálogo de vuelta  
✅ **Flujos complejos**: Wizards con múltiples pasos en diálogos  
✅ **Bottom Sheets**: Modales desde abajo  

### Ventajas sobre BottomSheet en la Screen

| Aspecto | BottomSheet en Screen | Dialog Destination |
|---------|----------------------|-------------------|
| **Gestión de estado** | ❌ Manual en la Screen | ✅ Navigation lo maneja |
| **Navegación atrás** | ❌ Código custom | ✅ Automático |
| **Resultados** | ❌ Callbacks manuales | ✅ SavedStateHandle |
| **Deep Links** | ❌ No soportado | ✅ Sí soportado |
| **Animaciones** | ❌ Custom | ✅ Material3 built-in |
| **Backstack** | ❌ No en pila | ✅ En la pila de navegación |
| **Testing** | 😐 Más complejo | ✅ Más simple |
| **Reutilización** | ❌ Acoplado a Screen | ✅ Independiente |

---

### Sintaxis básica

```kotlin
@Serializable
object ConfirmDialog

NavHost(navController, startDestination = Home) {
    composable<Home> {
        HomeScreen(
            onDeleteClick = {
                navController.navigate(ConfirmDialog)
            }
        )
    }
    
    // Dialog como destino de navegación
    dialog<ConfirmDialog> {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            title = { Text("Confirmar") },
            text = { Text("¿Estás seguro de eliminar este elemento?") },
            confirmButton = {
                TextButton(onClick = {
                    // Realizar acción
                    viewModel.deleteItem()
                    navController.popBackStack()
                }) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
```

---

### Ejemplo 1: Diálogo de confirmación simple

```kotlin
// Rutas
@Serializable
object Home

@Serializable
data class ConfirmDeleteDialog(val itemId: String)

// Implementación
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(navController, startDestination = Home) {
        composable<Home> {
            val viewModel: HomeViewModel = hiltViewModel()
            
            HomeScreen(
                items = viewModel.items,
                onDeleteClick = { itemId ->
                    navController.navigate(ConfirmDeleteDialog(itemId))
                }
            )
        }
        
        dialog<ConfirmDeleteDialog> { backStackEntry ->
            val dialog: ConfirmDeleteDialog = backStackEntry.toRoute()
            val viewModel: HomeViewModel = hiltViewModel(
                remember { navController.getBackStackEntry<Home>() }
            )
            
            AlertDialog(
                onDismissRequest = { navController.popBackStack() },
                icon = { Icon(Icons.Default.Warning, null) },
                title = { Text("Confirmar eliminación") },
                text = { Text("¿Estás seguro de eliminar el elemento #${dialog.itemId}?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteItem(dialog.itemId)
                            navController.popBackStack()
                        }
                    ) {
                        Text("Eliminar", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { navController.popBackStack() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
```

---

### Ejemplo 2: Recibir resultados del diálogo

```kotlin
@Serializable
object Home

@Serializable
object AddNoteDialog

// Screen que recibe el resultado
composable<Home> {
    val viewModel: HomeViewModel = hiltViewModel()
    val navController = rememberNavController()
    
    // Observar el resultado del diálogo
    val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<String?>("note_result", null)?.collect { note ->
            if (note != null) {
                viewModel.addNote(note)
                savedStateHandle.remove<String>("note_result") // Limpiar
            }
        }
    }
    
    HomeScreen(
        notes = viewModel.notes,
        onAddNoteClick = {
            navController.navigate(AddNoteDialog)
        }
    )
}

// Dialog que devuelve resultado
dialog<AddNoteDialog> {
    var noteText by remember { mutableStateOf("") }
    val navController = rememberNavController()
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Agregar Nota") },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Escribe tu nota") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Devolver resultado
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("note_result", noteText)
                    navController.popBackStack()
                },
                enabled = noteText.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancelar")
            }
        }
    )
}
```

---

### Ejemplo 3: BottomSheet como Dialog Destination

```kotlin
@Serializable
data class OptionsBottomSheet(val itemId: String)

@OptIn(ExperimentalMaterial3Api::class)
dialog<OptionsBottomSheet>(
    dialogProperties = DialogProperties(
        usePlatformDefaultWidth = false // Importante para BottomSheet
    )
) { backStackEntry ->
    val sheet: OptionsBottomSheet = backStackEntry.toRoute()
    val sheetState = rememberModalBottomSheetState()
    val navController = rememberNavController()
    
    ModalBottomSheet(
        onDismissRequest = { navController.popBackStack() },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Opciones para item #${sheet.itemId}",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            ListItem(
                headlineContent = { Text("Editar") },
                leadingContent = { Icon(Icons.Default.Edit, null) },
                modifier = Modifier.clickable {
                    navController.popBackStack()
                    navController.navigate(EditScreen(sheet.itemId))
                }
            )
            
            ListItem(
                headlineContent = { Text("Compartir") },
                leadingContent = { Icon(Icons.Default.Share, null) },
                modifier = Modifier.clickable {
                    // Acción de compartir
                    navController.popBackStack()
                }
            )
            
            Divider()
            
            ListItem(
                headlineContent = { 
                    Text("Eliminar", color = MaterialTheme.colorScheme.error) 
                },
                leadingContent = { 
                    Icon(Icons.Default.Delete, null, 
                         tint = MaterialTheme.colorScheme.error) 
                },
                modifier = Modifier.clickable {
                    navController.popBackStack()
                    navController.navigate(ConfirmDeleteDialog(sheet.itemId))
                }
            )
        }
    }
}
```

---

### Ejemplo 4: Wizard con múltiples diálogos

```kotlin
@Serializable
object WizardStep1Dialog

@Serializable
object WizardStep2Dialog

@Serializable
object WizardStep3Dialog

// Paso 1
dialog<WizardStep1Dialog> {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Paso 1: Nombre") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Tu nombre") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wizard_name", name)
                    navController.navigate(WizardStep2Dialog) {
                        popUpTo(WizardStep1Dialog) { inclusive = true }
                    }
                }
            ) {
                Text("Siguiente")
            }
        }
    )
}

// Paso 2
dialog<WizardStep2Dialog> {
    var email by remember { mutableStateOf("") }
    val name = navController.previousBackStackEntry
        ?.savedStateHandle
        ?.get<String>("wizard_name") ?: ""
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Paso 2: Email") },
        text = {
            Column {
                Text("Hola, $name!")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Tu email") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("wizard_email", email)
                    navController.navigate(WizardStep3Dialog) {
                        popUpTo(WizardStep2Dialog) { inclusive = true }
                    }
                }
            ) {
                Text("Siguiente")
            }
        },
        dismissButton = {
            TextButton(onClick = { 
                navController.navigate(WizardStep1Dialog) {
                    popUpTo(WizardStep2Dialog) { inclusive = true }
                }
            }) {
                Text("Atrás")
            }
        }
    )
}

// Paso 3 (final)
dialog<WizardStep3Dialog> {
    val name = navController.getBackStackEntry<Home>()
        .savedStateHandle.get<String>("wizard_name") ?: ""
    val email = navController.getBackStackEntry<Home>()
        .savedStateHandle.get<String>("wizard_email") ?: ""
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Resumen") },
        text = {
            Column {
                Text("Nombre: $name")
                Text("Email: $email")
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    // Guardar datos
                    viewModel.saveUser(name, email)
                    navController.popBackStack(Home, inclusive = false)
                }
            ) {
                Text("Finalizar")
            }
        }
    )
}
```

---

### Ejemplo 5: Diálogo de selección con resultado

```kotlin
@Serializable
object SelectColorDialog

data class ColorOption(val name: String, val color: Color)

dialog<SelectColorDialog> {
    val colors = listOf(
        ColorOption("Rojo", Color.Red),
        ColorOption("Verde", Color.Green),
        ColorOption("Azul", Color.Blue),
        ColorOption("Amarillo", Color.Yellow)
    )
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Seleccionar Color") },
        text = {
            LazyColumn {
                items(colors) { colorOption ->
                    ListItem(
                        headlineContent = { Text(colorOption.name) },
                        leadingContent = {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(colorOption.color, CircleShape)
                            )
                        },
                        modifier = Modifier.clickable {
                            // Devolver resultado
                            navController.previousBackStackEntry
                                ?.savedStateHandle
                                ?.set("selected_color", colorOption.name)
                            navController.popBackStack()
                        }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancelar")
            }
        }
    )
}

// En la Screen que lo llama
composable<Home> {
    val selectedColor by navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getStateFlow("selected_color", "")
        ?.collectAsState() ?: remember { mutableStateOf("") }
    
    HomeScreen(
        selectedColor = selectedColor,
        onSelectColorClick = {
            navController.navigate(SelectColorDialog)
        }
    )
}
```

---

### Ejemplo 6: Diálogo con validación

```kotlin
@Serializable
object CreateUserDialog

dialog<CreateUserDialog> {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    fun validate(): Boolean {
        var isValid = true
        
        if (name.isBlank()) {
            nameError = "El nombre es obligatorio"
            isValid = false
        } else {
            nameError = null
        }
        
        if (!email.contains("@")) {
            emailError = "Email inválido"
            isValid = false
        } else {
            emailError = null
        }
        
        return isValid
    }
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Crear Usuario") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { 
                        name = it
                        nameError = null
                    },
                    label = { Text("Nombre") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        emailError = null
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    supportingText = emailError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validate()) {
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("new_user", User(name, email))
                        navController.popBackStack()
                    }
                }
            ) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancelar")
            }
        }
    )
}
```

---

### Ejemplo 7: Custom Dialog con animaciones

```kotlin
@Serializable
object CustomDialog

dialog<CustomDialog>(
    dialogProperties = DialogProperties(
        dismissOnBackPress = true,
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = false
    )
) {
    var scale by remember { mutableStateOf(0.8f) }
    
    LaunchedEffect(Unit) {
        animate(
            initialValue = 0.8f,
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        ) { value, _ ->
            scale = value
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable { navController.popBackStack() },
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .scale(scale)
                .clickable(enabled = false) { } // No cerrar al click en la card
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color.Green,
                    modifier = Modifier.size(64.dp)
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "¡Éxito!",
                    style = MaterialTheme.typography.headlineMedium
                )
                
                Text(
                    "La operación se completó correctamente",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                
                Spacer(Modifier.height(16.dp))
                
                Button(onClick = { navController.popBackStack() }) {
                    Text("Aceptar")
                }
            }
        }
    }
}
```

---

### DialogProperties: Opciones avanzadas

```kotlin
dialog<MyDialog>(
    dialogProperties = DialogProperties(
        // Cerrar al presionar back
        dismissOnBackPress = true,
        
        // Cerrar al tocar fuera del diálogo
        dismissOnClickOutside = true,
        
        // Secure flag (no screenshots)
        securePolicy = SecureFlagPolicy.SecureOn,
        
        // Usar ancho de pantalla completa (para BottomSheets)
        usePlatformDefaultWidth = false,
        
        // Decorar con bordes del sistema
        decorFitsSystemWindows = true
    )
) {
    // Contenido del diálogo
}
```

---

### Patrón: Helper para manejar resultados

```kotlin
// Extension function para facilitar obtener resultados
@Composable
inline fun <reified T> NavController.observeResult(
    key: String,
    crossinline onResult: (T) -> Unit
) {
    val savedStateHandle = currentBackStackEntry?.savedStateHandle
    
    LaunchedEffect(savedStateHandle) {
        savedStateHandle?.getStateFlow<T?>(key, null)?.collect { result ->
            if (result != null) {
                onResult(result)
                savedStateHandle.remove<T>(key)
            }
        }
    }
}

// Uso
composable<Home> {
    val navController = rememberNavController()
    val viewModel: HomeViewModel = hiltViewModel()
    
    // Observar resultado del diálogo
    navController.observeResult<String>("note_result") { note ->
        viewModel.addNote(note)
    }
    
    navController.observeResult<User>("new_user") { user ->
        viewModel.addUser(user)
    }
    
    HomeScreen(
        onAddNoteClick = { navController.navigate(AddNoteDialog) },
        onAddUserClick = { navController.navigate(CreateUserDialog) }
    )
}
```

---

### Mejores prácticas

#### ✅ DO's

```kotlin
// ✅ Usa dialog() para modales
dialog<ConfirmDialog> { 
    AlertDialog(...) 
}

// ✅ Devuelve resultados con SavedStateHandle
navController.previousBackStackEntry
    ?.savedStateHandle
    ?.set("result_key", result)

// ✅ Observa resultados con LaunchedEffect
LaunchedEffect(savedStateHandle) {
    savedStateHandle?.getStateFlow<String?>("result", null)?.collect { ... }
}

// ✅ Usa DialogProperties para BottomSheets
dialog<MySheet>(
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) { ModalBottomSheet(...) }

// ✅ Limpia resultados después de usarlos
savedStateHandle.remove<String>("result_key")

// ✅ Valida datos antes de devolver
if (validate()) {
    navController.previousBackStackEntry?.savedStateHandle?.set(...)
    navController.popBackStack()
}
```

#### ❌ DON'Ts

```kotlin
// ❌ No uses composable() para diálogos
composable<ConfirmDialog> { 
    AlertDialog(...) // ❌ Usa dialog() en su lugar
}

// ❌ No uses callbacks manuales
var showDialog by remember { mutableStateOf(false) }
if (showDialog) {
    AlertDialog(...) // ❌ Usa dialog destination
}

// ❌ No olvides popBackStack()
confirmButton = {
    viewModel.save()
    // ❌ Falta: navController.popBackStack()
}

// ❌ No uses lambdas para resultados
onResult: (String) -> Unit // ❌ Usa SavedStateHandle

// ❌ No navegues dentro del diálogo sin cerrar
confirmButton = {
    navController.navigate(OtherScreen) // ❌ Cierra primero
}
```

---

### Casos de uso comunes

#### 1️⃣ Confirmación de eliminación
```kotlin
dialog<ConfirmDeleteDialog> {
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Confirmar") },
        text = { Text("¿Eliminar este elemento?") },
        confirmButton = {
            TextButton(onClick = {
                viewModel.delete()
                navController.popBackStack()
            }) { Text("Eliminar") }
        },
        dismissButton = {
            TextButton(onClick = { navController.popBackStack() }) {
                Text("Cancelar")
            }
        }
    )
}
```

#### 2️⃣ Formulario rápido
```kotlin
dialog<QuickFormDialog> {
    var text by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = { navController.popBackStack() },
        title = { Text("Agregar") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("result", text)
                    navController.popBackStack()
                }
            ) { Text("Guardar") }
        }
    )
}
```

#### 3️⃣ Opciones con BottomSheet
```kotlin
dialog<OptionsDialog>(
    dialogProperties = DialogProperties(usePlatformDefaultWidth = false)
) {
    ModalBottomSheet(
        onDismissRequest = { navController.popBackStack() }
    ) {
        Column {
            ListItem(
                headlineContent = { Text("Editar") },
                modifier = Modifier.clickable {
                    navController.popBackStack()
                    navController.navigate(EditScreen)
                }
            )
            ListItem(
                headlineContent = { Text("Compartir") },
                modifier = Modifier.clickable { /* acción */ }
            )
        }
    }
}
```

---

### Comparación con enfoques tradicionales

| Enfoque | Estado en Screen | Dialog Destination |
|---------|-----------------|-------------------|
| **Código** | `var show by remember { mutableStateOf(false) }` | `navController.navigate(Dialog)` |
| **Gestión** | Manual | Automático (Navigation) |
| **Backstack** | No | Sí |
| **Deep Link** | No | Sí |
| **Resultado** | Callback lambda | SavedStateHandle |
| **Testing** | Más complejo | Más simple |
| **Escalabilidad** | Limitada | Excelente |

---

### Resumen

**Dialog Destinations son superiores a estados locales porque:**

✅ **Navigation gestiona el ciclo de vida** automáticamente  
✅ **Backstack correcto**: Back button funciona naturalmente  
✅ **SavedStateHandle**: Manera elegante de devolver resultados  
✅ **DeepLinks**: Puedes abrir diálogos desde URLs  
✅ **Testing**: Más fácil de testear  
✅ **Separación de responsabilidades**: Diálogo independiente de la Screen  
✅ **Reutilización**: Mismo diálogo en múltiples pantallas  
✅ **Menos boilerplate**: No necesitas gestionar estados manualmente  

**Úsalos para:**
- Confirmaciones
- Formularios simples
- Selecciones
- Bottom Sheets
- Wizards multi-paso
- Cualquier modal que devuelva resultados

**¡Es la forma moderna y recomendada de manejar diálogos en Compose!** 🎉

---

## 🔗 DeepLinks (Enlaces Profundos)

### ¿Qué son?

Los **DeepLinks** permiten abrir **una pantalla específica** de tu app desde:
- 🌐 URLs externas (navegador web)
- 📧 Emails
- 📱 Notificaciones push
- 💬 SMS/WhatsApp
- 🔗 Otras apps

### Tipos de DeepLinks

#### 1️⃣ **Web DeepLinks (http/https)**
```kotlin
@Serializable
data class ProductDetail(val productId: String)

composable<ProductDetail>(
    deepLinks = listOf(
        navDeepLink<ProductDetail>(
            basePath = "https://mitienda.com/producto"
        )
    )
) { backStackEntry ->
    val detail: ProductDetail = backStackEntry.toRoute()
    ProductDetailScreen(productId = detail.productId)
}

// URL: https://mitienda.com/producto/123
// → Abre ProductDetail(productId = "123")
```

#### 2️⃣ **App Links (Android - Verificados)**
```kotlin
// AndroidManifest.xml
<activity android:name=".MainActivity" android:exported="true">
    <intent-filter android:autoVerify="true">
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        
        <data android:scheme="https" />
        <data android:host="mitienda.com" />
    </intent-filter>
</activity>
```

**Ventajas de App Links:**
- ✅ Se abren DIRECTAMENTE en tu app (sin mostrar selector)
- ✅ Google verifica que eres dueño del dominio
- ✅ Mejor experiencia de usuario

**Verificación:**
Necesitas un archivo `.well-known/assetlinks.json` en tu servidor:
```json
[{
  "relation": ["delegate_permission/common.handle_all_urls"],
  "target": {
    "namespace": "android_app",
    "package_name": "com.example.composeapp",
    "sha256_cert_fingerprints": ["YOUR_SHA256_FINGERPRINT"]
  }
}]
```

#### 3️⃣ **Custom Schemes**
```kotlin
composable<Home>(
    deepLinks = listOf(
        navDeepLink { uriPattern = "myapp://home" }
    )
) {
    HomeScreen()
}

// URI: myapp://home
// AndroidManifest.xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="myapp" />
</intent-filter>
```

### Ejemplo completo con parámetros

```kotlin
@Serializable
data class ArticleDetail(val id: String, val category: String)

composable<ArticleDetail>(
    deepLinks = listOf(
        navDeepLink<ArticleDetail>(
            basePath = "https://blog.com/articulos/{category}"
        )
    )
) { backStackEntry ->
    val detail: ArticleDetail = backStackEntry.toRoute()
    ArticleScreen(
        articleId = detail.id,
        category = detail.category
    )
}

// URL: https://blog.com/articulos/tecnologia/123
// → ArticleDetail(id = "123", category = "tecnologia")
```

### Parámetros opcionales y query params

```kotlin
@Serializable
data class SearchScreen(
    val query: String = "",
    val filter: String? = null
)

composable<SearchScreen>(
    deepLinks = listOf(
        navDeepLink<SearchScreen>(
            basePath = "https://app.com/search"
        )
    )
) { backStackEntry ->
    // URL: https://app.com/search?query=kotlin&filter=recent
    val search: SearchScreen = backStackEntry.toRoute()
    SearchResultsScreen(
        query = search.query,      // "kotlin"
        filter = search.filter     // "recent"
    )
}
```

### Manejar DeepLinks en la Activity

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = rememberNavController()
            
            // El NavController maneja automáticamente los DeepLinks
            NavHost(navController, startDestination = Home) {
                composable<Home>(
                    deepLinks = listOf(
                        navDeepLink { uriPattern = "myapp://home" }
                    )
                ) { HomeScreen() }
                
                composable<Profile>(
                    deepLinks = listOf(
                        navDeepLink<Profile>(
                            basePath = "myapp://profile"
                        )
                    )
                ) { backStackEntry ->
                    val profile: Profile = backStackEntry.toRoute()
                    ProfileScreen(userId = profile.userId)
                }
            }
        }
    }
}
```

---

## 🎯 Combinación: Nested Graphs + DeepLinks

### Ejemplo avanzado: App de Comercio Electrónico

```kotlin
@Serializable object AppGraph
@Serializable object AuthGraph
@Serializable object ShopGraph

@Serializable object Login
@Serializable object Register

@Serializable object ProductList
@Serializable data class ProductDetail(val productId: String)
@Serializable data class UserProfile(val userId: String)

@Composable
fun EcommerceApp() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = ShopGraph
    ) {
        // 🛍️ GRAFO DE TIENDA (con DeepLinks)
        navigation<ShopGraph>(startDestination = ProductList) {
            composable<ProductList>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "myapp://shop" },
                    navDeepLink<ProductList>(
                        basePath = "https://mitienda.com/productos"
                    )
                )
            ) {
                ProductListScreen { productId ->
                    navController.navigate(ProductDetail(productId))
                }
            }
            
            composable<ProductDetail>(
                deepLinks = listOf(
                    navDeepLink<ProductDetail>(
                        basePath = "https://mitienda.com/producto"
                    )
                )
            ) { backStackEntry ->
                val detail: ProductDetail = backStackEntry.toRoute()
                ProductDetailScreen(
                    productId = detail.productId,
                    onBuyClick = {
                        // Verificar si está logueado
                        // Si no, navegar a AuthGraph
                        navController.navigate(AuthGraph)
                    }
                )
            }
        }
        
        // 🔐 GRAFO DE AUTENTICACIÓN
        navigation<AuthGraph>(startDestination = Login) {
            composable<Login>(
                deepLinks = listOf(
                    navDeepLink { uriPattern = "myapp://login" }
                )
            ) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.popBackStack()
                    }
                )
            }
            
            composable<Register> {
                RegisterScreen()
            }
        }
        
        // 👤 PERFIL (DeepLink directo, fuera de grafos anidados)
        composable<UserProfile>(
            deepLinks = listOf(
                navDeepLink<UserProfile>(
                    basePath = "https://mitienda.com/usuario"
                )
            )
        ) { backStackEntry ->
            val profile: UserProfile = backStackEntry.toRoute()
            UserProfileScreen(userId = profile.userId)
        }
    }
}
```

### Flujo con DeepLink

```
Usuario hace click en: https://mitienda.com/producto/123
    ↓
Android abre MainActivity con Intent
    ↓
NavController detecta el DeepLink automáticamente
    ↓
Navega a ProductDetail(productId = "123")
    ↓
Pila: [ProductList, ProductDetail]
```

### AndroidManifest.xml completo

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />

    <application
        android:name=".ComposeApplication"
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.ComposeApp">
        
        <activity
            android:name=".ui.MainActivity"
            android:exported="true"
            android:theme="@style/Theme.ComposeApp">
            
            <!-- Launcher (icono en el drawer) -->
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
            
            <!-- DeepLinks Web con autoVerify (App Links) -->
            <intent-filter android:autoVerify="true">
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <data android:scheme="https" />
                <data android:host="mitienda.com" />
                <data android:pathPrefix="/productos" />
                <data android:pathPrefix="/producto" />
                <data android:pathPrefix="/usuario" />
            </intent-filter>
            
            <!-- Custom Scheme (myapp://) -->
            <intent-filter>
                <action android:name="android.intent.action.VIEW" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
                
                <data android:scheme="myapp" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

---

## 💡 Casos de Uso Reales

### 1️⃣ **Notificación Push → Pantalla específica**

```kotlin
// Firebase Cloud Messaging - Datos de la notificación
data class NotificationData(
    val type: String,
    val productId: String?
)

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val type = message.data["type"]
        val productId = message.data["productId"]
        
        val deepLink = when (type) {
            "new_product" -> "https://mitienda.com/producto/$productId"
            "sale" -> "myapp://shop"
            "profile" -> "myapp://profile"
            else -> "myapp://home"
        }
        
        // Crear notificación con DeepLink
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deepLink)).apply {
            setPackage(packageName)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("¡Nueva oferta!")
            .setContentText("Ver producto")
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(1, notification)
    }
}
```

### 2️⃣ **Email Marketing**

```html
<!-- Email HTML -->
<a href="https://mitienda.com/producto/123?utm_source=email&utm_campaign=summer">
    Ver Producto en Oferta
</a>
```

```kotlin
composable<ProductDetail>(
    deepLinks = listOf(
        navDeepLink<ProductDetail>(
            basePath = "https://mitienda.com/producto"
        )
    )
) { backStackEntry ->
    val detail: ProductDetail = backStackEntry.toRoute()
    
    // Capturar parámetros UTM para analytics
    val savedStateHandle = backStackEntry.savedStateHandle
    val utmSource = savedStateHandle.get<String>("utm_source")
    val utmCampaign = savedStateHandle.get<String>("utm_campaign")
    
    ProductDetailScreen(
        productId = detail.productId,
        analyticsSource = utmSource,      // "email"
        analyticsCampaign = utmCampaign   // "summer"
    )
}
```

### 3️⃣ **QR Code Scanner**

```kotlin
// Resultado del escaneo QR
fun handleQRCodeResult(qrContent: String) {
    // QR contiene: myapp://producto/123
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(qrContent))
        intent.setPackage(packageName)
        startActivity(intent)
        // Navigation maneja el DeepLink automáticamente
    } catch (e: Exception) {
        Toast.makeText(this, "QR inválido", Toast.LENGTH_SHORT).show()
    }
}

// En el NavHost
composable<ProductDetail>(
    deepLinks = listOf(
        navDeepLink<ProductDetail>(
            basePath = "myapp://producto"
        )
    )
) { backStackEntry ->
    val detail: ProductDetail = backStackEntry.toRoute()
    ProductDetailScreen(productId = detail.productId)
}
```

### 4️⃣ **Compartir en Redes Sociales**

```kotlin
fun shareProduct(context: Context, productId: String, productName: String) {
    val shareUrl = "https://mitienda.com/producto/$productId"
    
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, "Mira este producto!")
        putExtra(Intent.EXTRA_TEXT, "¡$productName está increíble! $shareUrl")
    }
    
    context.startActivity(
        Intent.createChooser(shareIntent, "Compartir producto")
    )
}

// Cuando alguien hace click en el link compartido
// → Abre directamente ProductDetail(productId = "...")
```

### 5️⃣ **Universal Links (iOS/Android)**

```kotlin
// Mismo código funciona en ambas plataformas
composable<Article>(
    deepLinks = listOf(
        navDeepLink<Article>(
            basePath = "https://blog.com/articulo"
        )
    )
) { backStackEntry ->
    val article: Article = backStackEntry.toRoute()
    ArticleScreen(articleId = article.id)
}

// URL: https://blog.com/articulo/kotlin-tips
// Android: Abre la app si está instalada
// iOS: Abre Safari (con Universal Links configurados)
```

---

## 🚀 Mejores Prácticas

### ✅ DO's (Hazlo así)

```kotlin
// ✅ Usa Type-Safe Navigation
@Serializable
data class ProductDetail(val productId: String)

composable<ProductDetail>(
    deepLinks = listOf(
        navDeepLink<ProductDetail>(basePath = "https://app.com/producto")
    )
)

// ✅ Maneja estados de carga al abrir por DeepLink
composable<Profile>(
    deepLinks = listOf(navDeepLink<Profile>(...))
) {
    val viewModel: ProfileViewModel = hiltViewModel()
    val state by viewModel.state.collectAsState()
    
    when (state) {
        is Loading -> LoadingScreen()
        is Success -> ProfileContent(state.user)
        is Error -> ErrorScreen(onRetry = { viewModel.retry() })
    }
}

// ✅ Valida datos del DeepLink
composable<ProductDetail>(
    deepLinks = listOf(navDeepLink<ProductDetail>(...))
) { backStackEntry ->
    val detail: ProductDetail = backStackEntry.toRoute()
    
    if (detail.productId.isBlank()) {
        ErrorScreen("Producto no encontrado")
    } else {
        ProductDetailScreen(productId = detail.productId)
    }
}

// ✅ Usa grafos para organizar features
navigation<ShopGraph>(startDestination = ProductList) {
    composable<ProductList>(deepLinks = [...]) { }
    composable<ProductDetail>(deepLinks = [...]) { }
}

// ✅ Limpia backstack correctamente al cambiar de grafo
navController.navigate(MainGraph) {
    popUpTo(AuthGraph) { inclusive = true }
}
```

### ❌ DON'Ts (Evita esto)

```kotlin
// ❌ No uses Strings manuales (no type-safe)
composable(
    route = "product/{id}",
    deepLinks = listOf(navDeepLink { uriPattern = "https://app.com/product/{id}" })
) { }

// ❌ No olvides manejar errores del DeepLink
composable<Detail>(deepLinks = [...]) {
    val detail: Detail = backStackEntry.toRoute()
    // ❌ ¿Qué pasa si el ID no existe en la BD?
    DetailScreen(detail.id)
}

// ❌ No anides demasiado (máx 2-3 niveles)
navigation<Level1> {
    navigation<Level2> {
        navigation<Level3> {  // Muy profundo, confuso
            composable<Screen> { }
        }
    }
}

// ❌ No uses DeepLinks sin validación
composable<Payment>(
    deepLinks = listOf(navDeepLink<Payment>(...))
) {
    // ❌ PELIGRO: Cualquiera puede abrir esta pantalla con un link
    // Deberías verificar autenticación
    PaymentScreen()
}

// ❌ No olvides el AndroidManifest
composable<Home>(
    deepLinks = listOf(navDeepLink { uriPattern = "myapp://home" })
) { }
// ❌ Si no está en el Manifest, el DeepLink NO funcionará
```

### 🔒 Seguridad con DeepLinks

```kotlin
// ✅ Verifica autenticación antes de navegar
composable<PrivateScreen>(
    deepLinks = listOf(navDeepLink<PrivateScreen>(...))
) {
    val authViewModel: AuthViewModel = hiltViewModel()
    val isAuthenticated by authViewModel.isAuthenticated.collectAsState()
    
    if (isAuthenticated) {
        PrivateScreenContent()
    } else {
        // Redirigir a login
        LaunchedEffect(Unit) {
            navController.navigate(Login) {
                popUpTo(PrivateScreen) { inclusive = true }
            }
        }
    }
}

// ✅ Valida permisos
composable<AdminPanel>(
    deepLinks = listOf(navDeepLink<AdminPanel>(...))
) {
    val userViewModel: UserViewModel = hiltViewModel()
    val isAdmin by userViewModel.isAdmin.collectAsState()
    
    if (isAdmin) {
        AdminPanelContent()
    } else {
        ErrorScreen("No tienes permisos")
    }
}
```

---

## 🧪 Testing DeepLinks

### Test de navegación

```kotlin
@Test
fun testDeepLink_opensProductDetail() {
    // Crear DeepLink
    val deepLinkUri = Uri.parse("https://mitienda.com/producto/123")
    val deepLinkIntent = Intent(Intent.ACTION_VIEW, deepLinkUri)
    
    // Lanzar Activity con el DeepLink
    val scenario = ActivityScenario.launch<MainActivity>(deepLinkIntent)
    
    // Verificar que se abrió la pantalla correcta
    onNodeWithText("Producto 123").assertIsDisplayed()
}
```

### Probar DeepLinks en desarrollo

```bash
# Usando ADB (Android Debug Bridge)

# DeepLink web
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://mitienda.com/producto/123" \
  com.example.composeapp

# Custom scheme
adb shell am start -W -a android.intent.action.VIEW \
  -d "myapp://home" \
  com.example.composeapp

# Con parámetros query
adb shell am start -W -a android.intent.action.VIEW \
  -d "https://mitienda.com/search?query=kotlin&filter=recent" \
  com.example.composeapp
```

---

## 📊 Comparación

| Feature | Nested Graphs | DeepLinks |
|---------|--------------|-----------|
| **Propósito** | Organizar navegación interna | Abrir desde fuera de la app |
| **Alcance** | Interno (dentro de la app) | Externo + Interno |
| **Uso principal** | Modularización por features | Marketing, Push, Links externos |
| **Complejidad** | Media | Alta |
| **Android Manifest** | No requerido | **Requerido** |
| **Requiere verificación** | No | Sí (para App Links) |
| **Ejemplos** | AuthGraph, ShopGraph | URLs, Push, QR |

---

## 🎓 Diagrama Visual Completo

```
App con Nested Graphs + DeepLinks
│
├── NavHost (root)
│   │
│   ├── 🔐 AuthGraph
│   │   ├── Login
│   │   │   └── DeepLink: "myapp://login"
│   │   └── Register
│   │
│   ├── 🛍️ ShopGraph
│   │   ├── ProductList
│   │   │   └── DeepLink: "https://mitienda.com/productos"
│   │   └── ProductDetail
│   │       └── DeepLink: "https://mitienda.com/producto/{id}"
│   │
│   ├── 🛒 CartGraph
│   │   ├── Cart
│   │   └── Checkout
│   │
│   └── 👤 Profile (standalone)
│       └── DeepLink: "https://mitienda.com/usuario/{id}"
│
└── AndroidManifest.xml
    ├── MAIN/LAUNCHER (app icon)
    ├── https://mitienda.com/* (App Links)
    └── myapp://* (Custom Scheme)
```

---

## 📚 Recursos adicionales

- [Documentación oficial - Nested Graphs](https://developer.android.com/guide/navigation/design/nested-graphs)
- [Documentación oficial - Deep Links](https://developer.android.com/training/app-links/deep-linking)
- [App Links Verification](https://developer.android.com/training/app-links/verify-android-applinks)
- [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
- [Type-Safe Navigation](https://developer.android.com/guide/navigation/design/type-safety)

---

## 📝 Resumen Final

### Nested Graphs
✅ Organiza tu app en módulos/features  
✅ Cada grafo tiene su propia navegación  
✅ Facilita el trabajo en equipo  
✅ Mejora la arquitectura  

### DeepLinks
✅ Abre pantallas desde fuera de la app  
✅ Marketing, notificaciones, QR codes  
✅ Mejora la experiencia del usuario  
✅ Incrementa conversiones  

### Juntos
🚀 **Arquitectura profesional + Marketing efectivo**

---

**¡Ahora eres un experto en Nested Graphs y DeepLinks!** 🎉🚀

