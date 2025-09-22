package hilos_virtuales_lock;

import util.GeneradorNumeros;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Ejemplo que demuestra el uso correcto de Lock con hilos virtuales
 * para proteger regiones críticas y evitar condiciones de carrera
 */
public class MainHilosVirtualesLock {

    private static final Logger LOGGER = Logger.getLogger(MainHilosVirtualesLock.class.getName());
    private static final int PAUSA_MS = 100;

    // Variables compartidas protegidas por Lock
    private static long sumaTotal = 0;
    private static int contador = 0;

    // Lock para proteger las regiones críticas
    private static final Lock lock = new ReentrantLock();

    private MainHilosVirtualesLock() {
        // Constructor privado para clase utilitaria
    }

    public static void main(String[] args) {
        System.out.println("=== SUMA CON HILOS VIRTUALES CON LOCK ===");
        System.out.println("✅ Este ejemplo usa Lock para proteger las regiones críticas");
        System.out.println("La suma final será CORRECTA gracias a la sincronización");

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
                procesarNumeroConLock(numeroAleatorio));

            hilosVirtuales.add(hiloVirtual);
        }

        // Esperar a que terminen todos los hilos virtuales
        esperarFinalizacionHilos(hilosVirtuales);

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        mostrarResultados(contador, sumaTotal, sumaEsperada, tiempoTotal, numeros.length);
    }

    private static synchronized void procesarNumeroConLock(int numeroAleatorio) {
        // Pausa de 100 milisegundos FUERA de la región crítica
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Hilo virtual interrumpido: {0}", Thread.currentThread().getName());
            return;
        }

        // REGIÓN CRÍTICA PROTEGIDA POR LOCK
        lock.lock();
        try {
            // Solo un hilo puede ejecutar este código a la vez
            sumaTotal += numeroAleatorio;  // ✅ SEGURO
            contador++;                    // ✅ SEGURO

            // Mostrar progreso cada 100 números
            if (contador % 100 == 0) {
                System.out.printf("Procesados: %d números (Hilo virtual: %s) [LOCK]%n",
                                contador, Thread.currentThread().getName());
            }
            //lock.unlock();
        } finally {
            lock.unlock(); // ¡Siempre liberar el lock!

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

        if (sumaObtenida != sumaEsperada) {
            System.out.printf("Diferencia: %d%n", Math.abs(sumaObtenida - sumaEsperada));
        }

        System.out.printf("Tiempo total: %d ms%n", tiempoTotal);
        System.out.printf("Tiempo promedio por número: %.2f ms%n", tiempoTotal / (double) totalNumeros);
        System.out.printf("Hilos virtuales creados: %d%n", totalNumeros);
        System.out.println("🔒 Sincronización: ReentrantLock utilizado correctamente");

        if (sumaObtenida == sumaEsperada) {
            System.out.println("\n✅ ¡SINCRONIZACIÓN EXITOSA!");
            System.out.println("El Lock protegió correctamente la región crítica.");
            System.out.println("No se produjeron condiciones de carrera.");
        }
    }
}
