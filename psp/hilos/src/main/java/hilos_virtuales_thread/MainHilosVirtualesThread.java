package hilos_virtuales_thread;

import util.GeneradorNumeros;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainHilosVirtualesThread {

    private static final Logger LOGGER = Logger.getLogger(MainHilosVirtualesThread.class.getName());
    private static final int PAUSA_MS = 100;

    private MainHilosVirtualesThread() {
        // Constructor privado para clase utilitaria
    }

    public static void main(String[] args) {
        System.out.println("=== SUMA CON HILOS VIRTUALES (Thread.start()) ===");

        // Generar los mismos números que usarán todas las implementaciones
        int[] numeros = GeneradorNumeros.generarNumerosAleatorios();
        long sumaEsperada = GeneradorNumeros.calcularSumaEsperada();

        System.out.println("Suma esperada: " + sumaEsperada);

        long tiempoInicio = System.currentTimeMillis();

        AtomicLong sumaTotal = new AtomicLong(0);
        AtomicInteger contador = new AtomicInteger(0);
        List<Thread> hilosVirtuales = new ArrayList<>();

        // Crear un hilo virtual para cada número (máximo paralelismo)
        for (int i = 0; i < numeros.length; i++) {
            final int numeroAleatorio = numeros[i];
            final int indice = i + 1;

            // Crear hilo virtual usando Thread.ofVirtual() (Java 21+)
            Thread hiloVirtual = Thread.ofVirtual().start(() ->
                procesarNumero(numeroAleatorio, contador, sumaTotal));

            hilosVirtuales.add(hiloVirtual);
        }

        // Esperar a que terminen todos los hilos virtuales
        esperarFinalizacionHilos(hilosVirtuales);

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        mostrarResultados(contador.get(), sumaTotal.get(), sumaEsperada, tiempoTotal, numeros.length);
    }

    private static void procesarNumero(int numeroAleatorio, AtomicInteger contador, AtomicLong sumaTotal) {
        // Pausa de 100 milisegundos
        try {
            Thread.sleep(PAUSA_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Hilo virtual interrumpido: {0}", Thread.currentThread().getName());
            return;
        }

        sumaTotal.addAndGet(numeroAleatorio);
        int procesados = contador.incrementAndGet();

        // Mostrar progreso cada 100 números
        if (procesados % 100 == 0) {
            System.out.printf("Procesados: %d números (Hilo virtual: %s)%n",
                            procesados, Thread.currentThread().getName());
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

    private static void mostrarResultados(int numerosProcessados, long sumaTotal, long sumaEsperada,
                                         long tiempoTotal, int totalNumeros) {
        System.out.println("\n--- RESULTADOS ---");
        System.out.printf("Números procesados: %d%n", numerosProcessados);
        System.out.printf("Suma total: %d%n", sumaTotal);
        System.out.printf("Suma correcta: %s%n", (sumaTotal == sumaEsperada ? "SÍ" : "NO"));
        System.out.printf("Tiempo total: %d ms%n", tiempoTotal);
        System.out.printf("Tiempo promedio por número: %.2f ms%n", tiempoTotal / (double) totalNumeros);
        System.out.printf("Hilos virtuales creados: %d (uno por número)%n", totalNumeros);
    }
}
