package problema3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Cocinero implements Runnable {
    private final int id;

    public Cocinero(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && RestauranteConcurrente.restauranteAbierto) {
                try {
                    long inicioEspera = System.currentTimeMillis();

                    // Esperar por un pedido (bloquea si no hay pedidos)
                    Pedido pedido = RestauranteConcurrente.mesaPedidos.take();

                    long tiempoEspera = System.currentTimeMillis() - inicioEspera;
                    RestauranteConcurrente.cocineroEsperando.addAndGet(tiempoEspera);

                    // Cocinar el plato
                    String tiempo = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    System.out.printf("[%s] Cocinero-%d empieza a cocinar %s %s para Mesa-%d%n",
                                     tiempo, id, pedido.getPlato().name(), pedido.getPlato().getEmoji(), pedido.getMesa().getNumero());

                    Thread.sleep(pedido.getPlato().getTiempoMs());

                    tiempo = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                    System.out.printf("[%s] Cocinero-%d termina %s para Cliente-%03d en Mesa-%d ✅%n",
                                     tiempo, id, pedido.getPlato().name(), pedido.getClienteId(), pedido.getMesa().getNumero());

                    RestauranteConcurrente.platosServidos.incrementAndGet();

                    // Calcular tiempo de espera del cliente
                    long tiempoEsperaCliente = System.currentTimeMillis() - pedido.getTiempoPedido();
                    RestauranteConcurrente.tiempoEsperaTotal.addAndGet(tiempoEsperaCliente);

                    // Notificar a la mesa que el plato está listo
                    pedido.getMesa().notificarPlatoListo();

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Cocinero.class.getName()).log(Level.SEVERE, "Error en cocinero", e);
        }
    }
}
