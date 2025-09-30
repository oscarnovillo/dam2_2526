package problema3;

public class Pedido {
    private final int clienteId;
    private final TipoPlato plato;
    private final Mesa mesa;
    private final long tiempoPedido;

    public Pedido(int clienteId, TipoPlato plato, Mesa mesa) {
        this.clienteId = clienteId;
        this.plato = plato;
        this.mesa = mesa;
        this.tiempoPedido = System.currentTimeMillis();
    }

    public int getClienteId() { return clienteId; }
    public TipoPlato getPlato() { return plato; }
    public Mesa getMesa() { return mesa; }
    public long getTiempoPedido() { return tiempoPedido; }

    @Override
    public String toString() {
        return String.format("%s para Cliente-%03d", plato.name(), clienteId);
    }
}
