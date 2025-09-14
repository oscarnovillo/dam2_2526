package hilos_virtuales_sin_sync;

import util.GeneradorNumeros;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Ejemplo que demuestra condiciones de carrera con hilos virtuales
 * SIN sincronización - la suma final puede ser incorrecta
 */
public class MainHilosVirtualesSinSync {

    private static final Logger LOGGER = Logger.getLogger(MainHilosVirtualesSinSync.class.getName());
    private static final int PAUSA_MS = 100;

    // Variables compartidas SIN sincronización (¡PELIGROSO!)
    private static long sumaTotal = 0;
    private static int contador = 0;

    private MainHilosVirtualesSinSync() {
        // Constructor privado para clase utilitaria
    }

    public static void main(String[] args) {
        System.out.println("=== SUMA CON HILOS VIRTUALES SIN SINCRONIZACIÓN ===");
        System.out.println("⚠️  ADVERTENCIA: Este ejemplo muestra condiciones de carrera");
        System.out.println("La suma final puede ser INCORRECTA debido a la falta de sincronización");

        // Generar los mismos números que usarán todas las implementaciones
        int[] numeros = GeneradorNumeros.generarNumerosAleatorios();
        long sumaEsperada = GeneradorNumeros.calcularSumaEsperada();

        System.out.println("Suma esperada: " + sumaEsperada);

        // Resetear variables compartidas
        sumaTotal = 0;
        contador = 0;

        long tiempoInicio = System.currentTimeMillis();

        List<Thread> hilosVirtuales = new ArrayList<>();

        // Crear un hilo virtual para cada número
        for (int i = 0; i < numeros.length; i++) {
            final int numeroAleatorio = numeros[i];

            // Crear hilo virtual usando Thread.ofVirtual() (Java 21+)
            Thread hiloVirtual = Thread.ofVirtual().start(() ->
                procesarNumeroSinSync(numeroAleatorio));

            hilosVirtuales.add(hiloVirtual);
        }

        // Esperar a que terminen todos los hilos virtuales
        esperarFinalizacionHilos(hilosVirtuales);

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        mostrarResultados(contador, sumaTotal, sumaEsperada, tiempoTotal, numeros.length);
    }

    private static void procesarNumeroSinSync(int numeroAleatorio) {
        // Pausa de 100 milisegundos
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Hilo virtual interrumpido: {0}", Thread.currentThread().getName());
            return;
        }

        // REGIÓN CRÍTICA SIN PROTECCIÓN - ¡CONDICIÓN DE CARRERA!
        // Múltiples hilos pueden acceder simultáneamente a estas variables
        sumaTotal += numeroAleatorio;  // ¡PELIGROSO!
        contador++;                    // ¡PELIGROSO!

        // Mostrar progreso cada 100 números (aproximadamente)
        if (contador % 100 == 0) {
            System.out.printf("Procesados: ~%d números (Hilo virtual: %s)%n",
                            contador, Thread.currentThread().getName());
        }
    }

    private static void esperarFinalizacionHilos(List<Thread> hilos) {
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Interrupción al esperar finalización de hilos virtuales", e);
                break;
            }
        }
    }

    private static void mostrarResultados(int numerosProcessados, long sumaObtenida, long sumaEsperada,
                                         long tiempoTotal, int totalNumeros) {
        System.out.println("\n--- RESULTADOS ---");
        System.out.printf("Números procesados: %d%n", numerosProcessados);
        System.out.printf("Suma obtenida: %d%n", sumaObtenida);
        System.out.printf("Suma esperada: %d%n", sumaEsperada);
        System.out.printf("Suma correcta: %s%n", (sumaObtenida == sumaEsperada ? "SÍ ✅" : "NO ❌"));
        System.out.printf("Diferencia: %d%n", Math.abs(sumaObtenida - sumaEsperada));
        System.out.printf("Tiempo total: %d ms%n", tiempoTotal);
        System.out.printf("Tiempo promedio por número: %.2f ms%n", tiempoTotal / (double) totalNumeros);
        System.out.printf("Hilos virtuales creados: %d%n", totalNumeros);

        if (sumaObtenida != sumaEsperada) {
            System.out.println("\n🚨 ¡CONDICIÓN DE CARRERA DETECTADA!");
            System.out.println("La suma es incorrecta debido a la falta de sincronización.");
            System.out.println("Múltiples hilos modificaron las variables compartidas simultáneamente.");
        }
    }
}
