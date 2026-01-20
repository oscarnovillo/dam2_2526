# SpringCrypto - Script de Inicio Rápido
# Ejecutar: .\start.ps1

Write-Host "🔐 SpringCrypto - Proyecto de Criptografía" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
Write-Host ""

# Verificar Maven
Write-Host "Verificando Maven..." -ForegroundColor Yellow
$mavenVersion = mvn -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Maven encontrado" -ForegroundColor Green
} else {
    Write-Host "❌ Maven no encontrado. Por favor, instala Maven primero." -ForegroundColor Red
    exit 1
}

# Verificar Java
Write-Host "Verificando Java..." -ForegroundColor Yellow
$javaVersion = java -version 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Java encontrado" -ForegroundColor Green
} else {
    Write-Host "❌ Java no encontrado. Por favor, instala Java 25+ primero." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "Opciones disponibles:" -ForegroundColor Cyan
Write-Host "  1. Compilar proyecto" -ForegroundColor White
Write-Host "  2. Ejecutar tests" -ForegroundColor White
Write-Host "  3. Ejecutar aplicación" -ForegroundColor White
Write-Host "  4. Compilar y ejecutar" -ForegroundColor White
Write-Host "  5. Abrir documentación" -ForegroundColor White
Write-Host "  6. Salir" -ForegroundColor White
Write-Host ""

$opcion = Read-Host "Selecciona una opción (1-6)"

switch ($opcion) {
    "1" {
        Write-Host "📦 Compilando proyecto..." -ForegroundColor Yellow
        mvn clean compile
    }
    "2" {
        Write-Host "🧪 Ejecutando tests..." -ForegroundColor Yellow
        mvn test
    }
    "3" {
        Write-Host "🚀 Iniciando aplicación..." -ForegroundColor Yellow
        Write-Host "La aplicación estará disponible en: http://localhost:8080" -ForegroundColor Green
        Write-Host "Prueba los endpoints con el archivo: api-tests.http" -ForegroundColor Green
        Write-Host ""
        mvn spring-boot:run
    }
    "4" {
        Write-Host "📦 Compilando..." -ForegroundColor Yellow
        mvn clean install
        Write-Host ""
        Write-Host "🚀 Iniciando aplicación..." -ForegroundColor Yellow
        Write-Host "La aplicación estará disponible en: http://localhost:8080" -ForegroundColor Green
        Write-Host "Prueba los endpoints con el archivo: api-tests.http" -ForegroundColor Green
        Write-Host ""
        mvn spring-boot:run
    }
    "5" {
        Write-Host "📚 Abriendo documentación..." -ForegroundColor Yellow
        if (Test-Path "README.md") {
            Start-Process "README.md"
        }
        if (Test-Path "CRIPTOGRAFIA.md") {
            Start-Process "CRIPTOGRAFIA.md"
        }
        if (Test-Path "QUICKSTART.md") {
            Start-Process "QUICKSTART.md"
        }
    }
    "6" {
        Write-Host "👋 ¡Hasta luego!" -ForegroundColor Cyan
        exit 0
    }
    default {
        Write-Host "❌ Opción no válida" -ForegroundColor Red
    }
}

Write-Host ""
Write-Host "✅ Proceso completado" -ForegroundColor Green

