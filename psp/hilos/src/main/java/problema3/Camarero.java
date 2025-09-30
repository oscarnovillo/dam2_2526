package problema3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Camarero implements Runnable {
    private final int id;
    private final Random random = new Random();

    public Camarero(int id) {
        this.id = id;
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && RestauranteConcurrente.restauranteAbierto) {
                try {
                    // Buscar mesas ocupadas (clientes sentados)
                    for (Mesa mesa : RestauranteConcurrente.mesas) {
                        Cliente cliente = mesa.getCliente();
                        if (cliente != null && mesa.getPlato() == null) {
                            // Tomar pedido del cliente
                            Pedido pedido = new Pedido(cliente.getId(), cliente.getPlatoElegido(), mesa);
                            mesa.asignarPlato(pedido.getPlato());
                            RestauranteConcurrente.mesaPedidos.put(pedido);
                            String tiempo = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
                            System.out.printf("[%s] Camarero-%d toma pedido %s en Mesa-%d y lo pone en cocina%n",
                                             tiempo, id, pedido.toString(), mesa.getNumero());
                            Thread.sleep(1000); // Simula tiempo de tomar pedido
                            RestauranteConcurrente.clientesAtendidos.incrementAndGet();
                        }
                    }
                    Thread.sleep(100); // Pequeña espera para evitar bucle intenso
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        } catch (Exception e) {
            Logger.getLogger(Camarero.class.getName()).log(Level.SEVERE, "Error en camarero", e);
        }
    }
}
