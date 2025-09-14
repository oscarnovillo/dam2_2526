package hilos_thread;

import util.GeneradorNumeros;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MainHilosThread {

    private static final Logger LOGGER = Logger.getLogger(MainHilosThread.class.getName());
    private static final int PAUSA_MS = 100;
    private static final int NUM_HILOS = 10; // Número de hilos que vamos a crear

    private MainHilosThread() {
        // Constructor privado para clase utilitaria
    }

    public static void main(String[] args) {
        System.out.println("=== SUMA CON HILOS TRADICIONALES (Thread.start()) ===");

        // Generar los mismos números que usarán todas las implementaciones
        int[] numeros = GeneradorNumeros.generarNumerosAleatorios();
        long sumaEsperada = GeneradorNumeros.calcularSumaEsperada();

        System.out.println("Suma esperada: " + sumaEsperada);

        long tiempoInicio = System.currentTimeMillis();

        AtomicLong sumaTotal = new AtomicLong(0);
        AtomicInteger contador = new AtomicInteger(0);
        List<Thread> hilos = new ArrayList<>();

        // Dividir el trabajo entre los hilos
        int numerosPorHilo = numeros.length / NUM_HILOS;
        int numerosRestantes = numeros.length % NUM_HILOS;

        int inicioIndice = 0;

        // Crear y lanzar los hilos
        for (int i = 0; i < NUM_HILOS; i++) {
            final int inicio = inicioIndice;
            final int fin = inicio + numerosPorHilo + (i < numerosRestantes ? 1 : 0);
            final int numeroHilo = i + 1;

            Thread hilo = new Thread(() -> procesarNumeros(numeros, inicio, fin, numeroHilo, sumaTotal, contador),
                                   "HiloTradicional-" + numeroHilo);

            hilos.add(hilo);
            hilo.start(); // Iniciar el hilo

            inicioIndice = fin;
        }

        // Esperar a que terminen todos los hilos
        esperarFinalizacionHilos(hilos);

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        mostrarResultados(contador.get(), sumaTotal.get(), sumaEsperada, tiempoTotal, numeros.length);
    }

    private static void procesarNumeros(int[] numeros, int inicio, int fin, int numeroHilo,
                                       AtomicLong sumaTotal, AtomicInteger contador) {
        for (int j = inicio; j < fin; j++) {
            int numeroAleatorio = numeros[j];

            // Pausa de 100 milisegundos
            try {
                Thread.sleep(PAUSA_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.WARNING, "Hilo interrumpido: {0}", Thread.currentThread().getName());
                return;
            }

            sumaTotal.addAndGet(numeroAleatorio);
            int procesados = contador.incrementAndGet();

            // Mostrar progreso cada 100 números
            if (procesados % 100 == 0) {
                System.out.printf("Procesados: %d números (Hilo-%d: %s)%n",
                                procesados, numeroHilo, Thread.currentThread().getName());
            }
        }
    }

    private static void esperarFinalizacionHilos(List<Thread> hilos) {
        for (Thread hilo : hilos) {
            try {
                hilo.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.log(Level.SEVERE, "Interrupción al esperar finalización de hilos", e);
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
        System.out.printf("Hilos tradicionales creados: %d%n", NUM_HILOS);
        System.out.printf("Números por hilo: ~%d%n", totalNumeros / NUM_HILOS);
    }
}
