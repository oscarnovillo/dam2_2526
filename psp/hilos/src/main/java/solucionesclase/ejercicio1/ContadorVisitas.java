package solucionesclase.ejercicio1;

import java.util.concurrent.atomic.AtomicInteger;

public class ContadorVisitas implements IContadorVisitas {

    private AtomicInteger contador = new AtomicInteger();

    public synchronized void incrementarVisita() {
//        int sumaNueva= contador +1;
//        contador = sumaNueva;
        contador.addAndGet(1);

    }

    public int getContador() {
        return contador.get();
    }
}
