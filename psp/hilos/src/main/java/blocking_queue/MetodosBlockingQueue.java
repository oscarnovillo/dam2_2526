package blocking_queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Demostración de los diferentes métodos de BlockingQueue
 */
public class MetodosBlockingQueue {

    public static void main(String[] args) {
        System.out.println("=== MÉTODOS DE BLOCKING QUEUE ===\n");

        // Cola con capacidad de 3 elementos
        BlockingQueue<String> cola = new ArrayBlockingQueue<>(3);

        demostrarMetodosInsercion(cola);
        System.out.println();
        demostrarMetodosExtraccion(cola);
        System.out.println();
        demostrarMetodosConTimeout(cola);
    }

    /**
     * Demuestra los diferentes métodos para insertar elementos
     */
    private static void demostrarMetodosInsercion(BlockingQueue<String> cola) {
        System.out.println("--- MÉTODOS DE INSERCIÓN ---");

        try {
            // 1. add() - Lanza excepción si la cola está llena
            System.out.println("1. add(): " + cola.add("Elemento1")); // true
            System.out.println("   Tamaño: " + cola.size());

            // 2. offer() - Retorna false si la cola está llena
            System.out.println("2. offer(): " + cola.offer("Elemento2")); // true
            System.out.println("   Tamaño: " + cola.size());

            // 3. put() - Bloquea si la cola está llena
            cola.put("Elemento3");
            System.out.println("3. put(): Elemento3 añadido");
            System.out.println("   Tamaño: " + cola.size() + " (COLA LLENA)");

            // Ahora la cola está llena (capacidad 3)

            // 4. Intentar add() con cola llena
            try {
                cola.add("Elemento4");
            } catch (IllegalStateException e) {
                System.out.println("4. add() con cola llena: " + e.getClass().getSimpleName());
            }

            // 5. Intentar offer() con cola llena
            System.out.println("5. offer() con cola llena: " + cola.offer("Elemento4")); // false

            // 6. offer() con timeout
            System.out.println("6. offer() con timeout de 1 segundo...");
            long inicio = System.currentTimeMillis();
            boolean resultado = cola.offer("Elemento4", 1, TimeUnit.SECONDS);
            long tiempo = System.currentTimeMillis() - inicio;
            System.out.println("   Resultado: " + resultado + " (Tiempo: " + tiempo + "ms)");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demuestra los diferentes métodos para extraer elementos
     */
    private static void demostrarMetodosExtraccion(BlockingQueue<String> cola) {
        System.out.println("--- MÉTODOS DE EXTRACCIÓN ---");

        try {
            // 1. remove() - Lanza excepción si la cola está vacía
            System.out.println("1. remove(): " + cola.remove());
            System.out.println("   Tamaño: " + cola.size());

            // 2. poll() - Retorna null si la cola está vacía
            System.out.println("2. poll(): " + cola.poll());
            System.out.println("   Tamaño: " + cola.size());

            // 3. take() - Bloquea si la cola está vacía
            System.out.println("3. take(): " + cola.take());
            System.out.println("   Tamaño: " + cola.size() + " (COLA VACÍA)");

            // Ahora la cola está vacía

            // 4. Intentar remove() con cola vacía
            try {
                cola.remove();
            } catch (Exception e) {
                System.out.println("4. remove() con cola vacía: " + e.getClass().getSimpleName());
            }

            // 5. Intentar poll() con cola vacía
            System.out.println("5. poll() con cola vacía: " + cola.poll()); // null

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Demuestra métodos con timeout
     */
    private static void demostrarMetodosConTimeout(BlockingQueue<String> cola) {
        System.out.println("--- MÉTODOS CON TIMEOUT ---");

        try {
            // poll() con timeout en cola vacía
            System.out.println("1. poll() con timeout de 2 segundos en cola vacía...");
            long inicio = System.currentTimeMillis();
            String resultado = cola.poll(2, TimeUnit.SECONDS);
            long tiempo = System.currentTimeMillis() - inicio;
            System.out.println("   Resultado: " + resultado + " (Tiempo: " + tiempo + "ms)");

            // Añadir un elemento para la siguiente prueba
            cola.offer("ElementoTest");

            // poll() con timeout en cola con elementos
            System.out.println("2. poll() con timeout en cola con elementos:");
            System.out.println("   Resultado: " + cola.poll(1, TimeUnit.SECONDS));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
