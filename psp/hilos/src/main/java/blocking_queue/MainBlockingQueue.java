package blocking_queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Demostración de BlockingQueue en Java
 *
 * Una BlockingQueue es una cola thread-safe que:
 * 1. Bloquea el hilo cuando intentas extraer de una cola vacía
 * 2. Bloquea el hilo cuando intentas insertar en una cola llena
 * 3. Es perfecta para el patrón Productor-Consumidor
 */
public class MainBlockingQueue {

    // Cola con capacidad limitada de 5 elementos
    private static final BlockingQueue<String> cola = new ArrayBlockingQueue<>(5);

    public static void main(String[] args) {
        System.out.println("=== DEMOSTRACIÓN DE BLOCKING QUEUE ===\n");

        // Crear hilos productores
        Thread productor1 = new Thread(new Productor("Productor-1"), "Productor-1");
        Thread productor2 = new Thread(new Productor("Productor-2"), "Productor-2");

        // Crear hilos consumidores
        Thread consumidor1 = new Thread(new Consumidor("Consumidor-1"), "Consumidor-1");
        Thread consumidor2 = new Thread(new Consumidor("Consumidor-2"), "Consumidor-2");

        // Iniciar todos los hilos
        productor1.start();
        productor2.start();
        consumidor1.start();
        consumidor2.start();

        try {
            // Esperar 10 segundos y luego terminar
            Thread.sleep(10000);

            // Interrumpir todos los hilos
            productor1.interrupt();
            productor2.interrupt();
            consumidor1.interrupt();
            consumidor2.interrupt();

            System.out.println("\n=== PROGRAMA TERMINADO ===");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Clase Productor - Añade elementos a la cola
     */
    static class Productor implements Runnable {
        private final String nombre;
        private int contador = 1;

        public Productor(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    String mensaje = nombre + "-Mensaje-" + contador++;

                    System.out.println("🔵 " + nombre + " intentando añadir: " + mensaje +
                                     " (Tamaño cola: " + cola.size() + ")");

                    // put() bloquea si la cola está llena
                    cola.put(mensaje);

                    System.out.println("✅ " + nombre + " añadió: " + mensaje +
                                     " (Tamaño cola: " + cola.size() + ")");

                    // Pausa entre producciones
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                System.out.println("🛑 " + nombre + " interrumpido");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Clase Consumidor - Extrae elementos de la cola
     */
    static class Consumidor implements Runnable {
        private final String nombre;

        public Consumidor(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("🔴 " + nombre + " esperando elemento... (Tamaño cola: " + cola.size() + ")");

                    // take() bloquea si la cola está vacía
                    String mensaje = cola.take();

                    System.out.println("✅ " + nombre + " consumió: " + mensaje +
                                     " (Tamaño cola: " + cola.size() + ")");

                    // Simular procesamiento
                    Thread.sleep(1500);
                }
            } catch (InterruptedException e) {
                System.out.println("🛑 " + nombre + " interrumpido");
                Thread.currentThread().interrupt();
            }
        }
    }
}
