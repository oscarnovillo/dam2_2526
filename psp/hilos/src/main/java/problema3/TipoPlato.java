package problema3;

import java.util.Random;

public enum TipoPlato {
    ENSALADA(2000, "🥗"),
    PASTA(3000, "🍝"),
    PIZZA(4000, "🍕"),
    CARNE(5000, "🥩");

    private final int tiempoMs;
    private final String emoji;

    TipoPlato(int tiempoMs, String emoji) {
        this.tiempoMs = tiempoMs;
        this.emoji = emoji;
    }

    public int getTiempoMs() { return tiempoMs; }
    public String getEmoji() { return emoji; }

    public static TipoPlato aleatorio() {
        TipoPlato[] platos = values();
        return platos[new Random().nextInt(platos.length)];
    }
}
