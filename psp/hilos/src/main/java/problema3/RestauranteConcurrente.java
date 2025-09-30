package problema3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Solución del Problema 3: El Restaurante Concurrente
 * Implementa el patrón Productor-Consumidor con BlockingQueue
 */
public class RestauranteConcurrente {

    private static final Logger LOGGER = Logger.getLogger(RestauranteConcurrente.class.getName());
    private static final int NUM_COCINEROS = 3;
    private static final int NUM_CAMAREROS = 5;
    private static final int NUM_CLIENTES = 100;
    private static final int CAPACIDAD_MESA = 10;
    private static final int INTERVALO_CLIENTES = 100; // ms
    private static final int TIEMPO_ESPERA_MESA = 1000; // ms, tiempo máximo que un cliente espera por mesa

    public static final BlockingQueue<Pedido> mesaPedidos = new LinkedBlockingQueue<>(); // ilimitada
    public static final BlockingQueue<Cliente> colaClientes = new ArrayBlockingQueue<>(CAPACIDAD_MESA); // limitada por mesas
    public static final List<Mesa> mesas = new java.util.ArrayList<>();
    public static final BlockingQueue<Mesa> mesasLibres = new LinkedBlockingQueue<>();
    public static final AtomicInteger clientesAtendidos = new AtomicInteger(0);
    public static final AtomicInteger platosServidos = new AtomicInteger(0);
    public static final AtomicLong tiempoEsperaTotal = new AtomicLong(0);
    public static final AtomicInteger mesaLlenaVeces = new AtomicInteger(0);
    public static final AtomicLong cocineroEsperando = new AtomicLong(0);
    public static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    public static volatile boolean restauranteAbierto = true;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== RESTAURANTE CONCURRENTE ===");
        System.out.printf("Iniciando servicio con %d cocineros y %d camareros...%n%n",
                         NUM_COCINEROS, NUM_CAMAREROS);

        long inicioSimulacion = System.currentTimeMillis();

        // Inicializar mesas
        for (int i = 1; i <= CAPACIDAD_MESA; i++) {
            Mesa mesa = new Mesa(i);
            mesas.add(mesa);
            mesasLibres.add(mesa);
        }

        // Iniciar cocineros
        Thread[] cocineros = new Thread[NUM_COCINEROS];
        for (int i = 0; i < NUM_COCINEROS; i++) {
            cocineros[i] = new Thread(new Cocinero(i + 1), "Cocinero-" + (i + 1));
            cocineros[i].start();
        }

        // Iniciar camareros
        Thread[] camareros = new Thread[NUM_CAMAREROS];
        for (int i = 0; i < NUM_CAMAREROS; i++) {
            camareros[i] = new Thread(new Camarero(i + 1), "Camarero-" + (i + 1));
            camareros[i].start();
        }

        // Simular llegada de clientes
        Thread generadorClientes = Thread.ofVirtual().start(() -> {
            try {
                for (int i = 1; i <= NUM_CLIENTES; i++) {
                    Mesa mesa = mesasLibres.poll(TIEMPO_ESPERA_MESA, java.util.concurrent.TimeUnit.MILLISECONDS);
                    if (mesa != null) {
                        Cliente cliente = new Cliente(i, mesa);
                        mesa.sentarCliente(cliente);
                        Thread clienteHilo = Thread.ofVirtual().start(cliente);
                    } else {
                        String tiempo = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                        System.out.printf("[%s] Cliente-%03d se va (no consigue mesa tras %ds) 😞%n", tiempo, i, TIEMPO_ESPERA_MESA/1000);
                    }
                    Thread.sleep(INTERVALO_CLIENTES);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Esperar a que lleguen todos los clientes
        generadorClientes.join();

        // Esperar un poco más para que se procesen todos los pedidos
        Thread.sleep(10000);

        // Cerrar restaurante
        restauranteAbierto = false;

        // Interrumpir hilos para que terminen
        for (Thread cocinero : cocineros) {
            cocinero.interrupt();
        }
        for (Thread camarero : camareros) {
            camarero.interrupt();
        }

        // Esperar a que terminen
        for (Thread cocinero : cocineros) {
            cocinero.join();
        }
        for (Thread camarero : camareros) {
            camarero.join();
        }

        long tiempoTotal = System.currentTimeMillis() - inicioSimulacion;

        mostrarEstadisticasFinales(tiempoTotal);
    }

    private static void mostrarEstadisticasFinales(long tiempoTotal) {
        System.out.println("\n--- ESTADÍSTICAS FINALES ---");
        System.out.printf("Clientes atendidos: %d/%d %s%n",
                         clientesAtendidos.get(), NUM_CLIENTES,
                         clientesAtendidos.get() == NUM_CLIENTES ? "✅" : "⚠️");
        System.out.printf("Platos servidos: %d%n", platosServidos.get());

        if (clientesAtendidos.get() > 0) {
            System.out.printf("Tiempo promedio de espera: %.1fs%n",
                             tiempoEsperaTotal.get() / 1000.0 / clientesAtendidos.get());
        }

        System.out.printf("Mesa llena (veces): %d%n", mesaLlenaVeces.get());
        System.out.printf("Cocineros esperando (tiempo): %ds total%n", cocineroEsperando.get() / 1000);

        if (tiempoTotal > 0 && NUM_CLIENTES > 0) {
            int eficiencia = (int)((clientesAtendidos.get() * 100.0) / NUM_CLIENTES);
            System.out.printf("Eficiencia: %d%%%n", eficiencia);
        }

        System.out.printf("Tiempo total simulación: %.1fs%n", tiempoTotal / 1000.0);
    }
}
