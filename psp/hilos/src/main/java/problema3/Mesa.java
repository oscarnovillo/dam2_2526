package problema3;

import java.util.concurrent.CountDownLatch;

public class Mesa {
    private final int numero;
    private Cliente cliente;
    private TipoPlato plato;
    private final CountDownLatch platoListo = new CountDownLatch(1);

    public Mesa(int numero) {
        this.numero = numero;
    }

    public synchronized void sentarCliente(Cliente cliente) {
        this.cliente = cliente;
        this.plato = null;
    }

    public synchronized Cliente getCliente() {
        return cliente;
    }

    public synchronized void asignarPlato(TipoPlato plato) {
        this.plato = plato;
    }

    public synchronized TipoPlato getPlato() {
        return plato;
    }

    public void notificarPlatoListo() {
        platoListo.countDown();
    }

    public void esperarPlatoListo() throws InterruptedException {
        platoListo.await();
    }

    public synchronized void liberarMesa() {
        this.cliente = null;
        this.plato = null;
    }

    public int getNumero() {
        return numero;
    }
}

