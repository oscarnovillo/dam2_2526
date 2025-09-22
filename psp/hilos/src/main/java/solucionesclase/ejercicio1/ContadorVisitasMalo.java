package solucionesclase.ejercicio1;

public class ContadorVisitasMalo implements  IContadorVisitas {

    private int contador = 0;

    public void incrementarVisita() {
        int sumaNueva = contador + 1;
        contador = sumaNueva;
    }

    public int getContador() {
        return contador;
    }
}
