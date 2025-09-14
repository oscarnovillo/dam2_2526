package hilos_normales;

import util.GeneradorNumeros;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

public class MainHilosNormales {

    private static final int PAUSA_MS = 100;
    private static final int NUM_HILOS = 10; // Número de hilos en el pool

    public static void main(String[] args) {
        System.out.println("=== SUMA CON HILOS NORMALES ===");

        // Generar los mismos números que usarán todas las implementaciones
        int[] numeros = GeneradorNumeros.generarNumerosAleatorios();
        long sumaEsperada = GeneradorNumeros.calcularSumaEsperada();

        System.out.println("Suma esperada: " + sumaEsperada);

        long tiempoInicio = System.currentTimeMillis();

        ExecutorService executor = Executors.newFixedThreadPool(NUM_HILOS);
        AtomicLong sumaTotal = new AtomicLong(0);
        AtomicInteger contador = new AtomicInteger(0);

        // Crear tareas para cada número
        for (int i = 0; i < numeros.length; i++) {
            final int numeroAleatorio = numeros[i];
            final int indice = i;
            executor.submit(() -> {
                // Pausa de 100 milisegundos
                try {
                    Thread.sleep(PAUSA_MS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                sumaTotal.addAndGet(numeroAleatorio);
                int procesados = contador.incrementAndGet();

                // Mostrar progreso cada 100 números
                if (procesados % 100 == 0) {
                    System.out.println("Procesados: " + procesados + " números (Hilo: " +
                                     Thread.currentThread().getName() + ")");
                }
            });
        }

        // Cerrar el executor y esperar a que terminen todas las tareas
        executor.shutdown();
        try {
            if (!executor.awaitTermination(300, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        System.out.println("\n--- RESULTADOS ---");
        System.out.println("Números procesados: " + contador.get());
        System.out.println("Suma total: " + sumaTotal.get());
        System.out.println("Suma correcta: " + (sumaTotal.get() == sumaEsperada ? "SÍ" : "NO"));
        System.out.println("Tiempo total: " + tiempoTotal + " ms");
        System.out.println("Tiempo promedio por número: " + (tiempoTotal / (double) numeros.length) + " ms");
        System.out.println("Hilos utilizados: " + NUM_HILOS);
    }
}
