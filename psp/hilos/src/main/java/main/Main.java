package main;

import sin_hilos.MainSinHilos;
import hilos_normales.MainHilosNormales;
import hilos_virtuales.MainHilosVirtuales;
import hilos_thread.MainHilosThread;
import hilos_virtuales_thread.MainHilosVirtualesThread;
import hilos_virtuales_sin_sync.MainHilosVirtualesSinSync;
import hilos_virtuales_lock.MainHilosVirtualesLock;
import util.GeneradorNumeros;

public class Main {

    public static void main(String[] args) {
        System.out.println("=== COMPARACIÓN DE RENDIMIENTO Y SINCRONIZACIÓN: HILOS VS SIN HILOS ===");
        System.out.println("Sumando 1000 números aleatorios con pausa de 100ms cada uno");
        System.out.println("NOTA: Todos los métodos suman exactamente los mismos números para una comparación justa");
        System.out.println("Suma esperada: " + GeneradorNumeros.calcularSumaEsperada());
        System.out.println();

        // 1. Ejecutar sin hilos
        System.out.println("1. Ejecutando sin hilos...");
        MainSinHilos.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 2. Ejecutar con hilos normales (ExecutorService)
        System.out.println("2. Ejecutando con hilos normales (ExecutorService)...");
        MainHilosNormales.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 3. Ejecutar con hilos virtuales (ExecutorService)
        System.out.println("3. Ejecutando con hilos virtuales (ExecutorService)...");
        MainHilosVirtuales.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 4. Ejecutar con Thread tradicionales (Thread.start())
        System.out.println("4. Ejecutando con Thread tradicionales (Thread.start())...");
        MainHilosThread.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 5. Ejecutar con Thread virtuales (Thread.start())
        System.out.println("5. Ejecutando con Thread virtuales (Thread.start())...");
        MainHilosVirtualesThread.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 6. Ejecutar con hilos virtuales SIN sincronización (condiciones de carrera)
        System.out.println("6. Ejecutando con hilos virtuales SIN sincronización...");
        MainHilosVirtualesSinSync.main(args);

        System.out.println("\n" + "=".repeat(60) + "\n");

        // 7. Ejecutar con hilos virtuales CON Lock (sincronización correcta)
        System.out.println("7. Ejecutando con hilos virtuales CON Lock...");
        MainHilosVirtualesLock.main(args);

        System.out.println("\n" + "=".repeat(80));
        System.out.println("¡Comparación completada!");
        System.out.println("Observa las diferencias en tiempo de ejecución Y sincronización entre los métodos.");
        System.out.println();
        System.out.println("📊 Análisis de rendimiento esperado:");
        System.out.println("- Sin hilos: ~100 segundos (secuencial)");
        System.out.println("- Hilos normales (ExecutorService): ~10 segundos (10 hilos en pool)");
        System.out.println("- Hilos virtuales (ExecutorService): ~100ms (máximo paralelismo)");
        System.out.println("- Thread tradicionales: ~10 segundos (10 Thread creados manualmente)");
        System.out.println("- Thread virtuales: ~100ms (1000 Thread virtuales)");
        System.out.println();
        System.out.println("🔒 Análisis de sincronización:");
        System.out.println("- Sin sincronización: Suma INCORRECTA (condiciones de carrera)");
        System.out.println("- Con Lock: Suma CORRECTA pero más lento (sincronización segura)");
        System.out.println("- Con AtomicLong/AtomicInteger: Suma CORRECTA y más rápido (lock-free)");
    }
}
