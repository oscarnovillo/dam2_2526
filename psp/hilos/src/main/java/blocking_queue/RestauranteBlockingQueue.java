package blocking_queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.Random;

/**
 * Ejemplo práctico: Sistema de Procesamiento de Pedidos
 * Simula un restaurante donde los cocineros (productores) preparan pedidos
 * y los camareros (consumidores) los sirven a las mesas
 */
public class RestauranteBlockingQueue {

    // Cola de pedidos listos para servir (capacidad limitada)
    private static final BlockingQueue<Pedido> colapedidos = new ArrayBlockingQueue<>(10);
    private static final Random random = new Random();

    public static void main(String[] args) {
        System.out.println("🍽️  === RESTAURANTE CON BLOCKING QUEUE ===\n");

        // Crear cocineros (productores)
        Thread cocinero1 = new Thread(new Cocinero("Chef Mario"), "Chef-Mario");
        Thread cocinero2 = new Thread(new Cocinero("Chef Luigi"), "Chef-Luigi");

        // Crear camareros (consumidores)
        Thread camarero1 = new Thread(new Camarero("Ana"), "Camarero-Ana");
        Thread camarero2 = new Thread(new Camarero("Carlos"), "Camarero-Carlos");
        Thread camarero3 = new Thread(new Camarero("María"), "Camarero-María");

        // Iniciar todos los hilos
        cocinero1.start();
        cocinero2.start();
        camarero1.start();
        camarero2.start();
        camarero3.start();

        try {
            // Simular 15 segundos de trabajo
            Thread.sleep(15000);

            // Parar todos los hilos
            cocinero1.interrupt();
            cocinero2.interrupt();
            camarero1.interrupt();
            camarero2.interrupt();
            camarero3.interrupt();

            System.out.println("\n🏪 Restaurante cerrado. Pedidos pendientes: " + colapedidos.size());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Clase que representa un pedido
     */
    static class Pedido {
        private final int numero;
        private final String plato;
        private final String cocinero;
        private final long tiempoCreacion;

        public Pedido(int numero, String plato, String cocinero) {
            this.numero = numero;
            this.plato = plato;
            this.cocinero = cocinero;
            this.tiempoCreacion = System.currentTimeMillis();
        }

        public int getNumero() { return numero; }
        public String getPlato() { return plato; }
        public String getCocinero() { return cocinero; }

        public long getTiempoEspera() {
            return System.currentTimeMillis() - tiempoCreacion;
        }

        @Override
        public String toString() {
            return "Pedido #" + numero + " (" + plato + ") por " + cocinero;
        }
    }

    /**
     * Cocinero - Produce pedidos (Productor)
     */
    static class Cocinero implements Runnable {
        private final String nombre;
        private final String[] platos = {"Pizza Margarita", "Pasta Carbonara", "Ensalada César",
                                        "Hamburguesa", "Sopa del día", "Paella"};
        private int numeroPedido = 1;

        public Cocinero(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    // Tiempo de preparación (1-4 segundos)
                    int tiempoPreparacion = 1000 + random.nextInt(3000);
                    Thread.sleep(tiempoPreparacion);

                    // Crear pedido
                    String platoSeleccionado = platos[random.nextInt(platos.length)];
                    Pedido pedido = new Pedido(numeroPedido++, platoSeleccionado, nombre);

                    System.out.println("👨‍🍳 " + nombre + " preparó: " + pedido +
                                     " (Cola: " + colapedidos.size() + "/10)");

                    // Intentar añadir a la cola con timeout de 5 segundos
                    boolean añadido = colapedidos.offer(pedido, 5, TimeUnit.SECONDS);

                    if (añadido) {
                        System.out.println("✅ " + pedido + " listo para servir");
                    } else {
                        System.out.println("⚠️  Cola llena! " + pedido + " se ha perdido");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("🛑 " + nombre + " ha terminado su turno");
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Camarero - Sirve pedidos (Consumidor)
     */
    static class Camarero implements Runnable {
        private final String nombre;
        private int pedidosServidos = 0;

        public Camarero(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public void run() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    System.out.println("🔍 " + nombre + " buscando pedidos... (Cola: " + colapedidos.size() + ")");

                    // Esperar por un pedido (con timeout de 3 segundos)
                    Pedido pedido = colapedidos.poll(3, TimeUnit.SECONDS);

                    if (pedido != null) {
                        pedidosServidos++;
                        long tiempoEspera = pedido.getTiempoEspera();

                        System.out.println("🍽️  " + nombre + " sirvió: " + pedido +
                                         " (Tiempo espera: " + tiempoEspera + "ms)");

                        // Tiempo para servir (1-2 segundos)
                        Thread.sleep(1000 + random.nextInt(1000));

                        System.out.println("✨ " + nombre + " completó el servicio del " + pedido);

                    } else {
                        System.out.println("😴 " + nombre + " esperando... no hay pedidos");
                    }
                }
            } catch (InterruptedException e) {
                System.out.println("🛑 " + nombre + " terminó (Pedidos servidos: " + pedidosServidos + ")");
                Thread.currentThread().interrupt();
            }
        }
    }
}
