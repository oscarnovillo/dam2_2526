package solucionesclase.ejercicio1;

public class ContadorMain {




    public static void main(String[] args) {

        IContadorVisitas contador = new ContadorVisitas();
        ContadorServicios servicios = new ContadorServicios(contador);
        servicios.work();


        servicios.setContador(contador);
        servicios.work();
    }



}
