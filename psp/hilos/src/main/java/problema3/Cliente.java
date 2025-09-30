package problema3;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

public class Cliente implements Runnable {
    private final int id;
    private final Random random = new Random();
    private TipoPlato platoElegido;
    private final Mesa mesa;

    public Cliente(int id, Mesa mesa) {
        this.id = id;
        this.platoElegido = TipoPlato.aleatorio();
        this.mesa = mesa;
    }

    public int getId() { return id; }
    public TipoPlato getPlatoElegido() { return platoElegido; }
    public Mesa getMesa() { return mesa; }

    @Override
    public void run() {
        String tiempo = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        System.out.printf("[%s] Cliente-%03d se sienta en Mesa-%d y pide %s %s%n",
                         tiempo, id, mesa.getNumero(), platoElegido.name(), platoElegido.getEmoji());
        try {
            mesa.esperarPlatoListo(); // Espera a que el cocinero le entregue el plato
            // Simular tiempo de comer
            System.out.printf("[%s] Cliente-%03d empieza a comer en Mesa-%d%n",
                             LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), id, mesa.getNumero());
            Thread.sleep(2000); // Comer 2 segundos
            System.out.printf("[%s] Cliente-%03d termina de comer y deja Mesa-%d%n",
                             LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")), id, mesa.getNumero());
            mesa.liberarMesa();
            RestauranteConcurrente.mesasLibres.put(mesa); // Devuelve la mesa a la cola de libres
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
