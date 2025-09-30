package solucionesclase.ejercicio1;

public class ContadorServicios {


    private String cadena  ="";
    private IContadorVisitas contador;

    public IContadorVisitas getContador() {
        return contador;
    }

    public void setContador(IContadorVisitas contador) {
        this.contador = contador;
    }

    public ContadorServicios(IContadorVisitas contador) {
        this.contador = contador;
    }

    public void work()
    {
        Thread[] hilos = new Thread[1000];

            String contador2 = "";
        //Crear 1000 hilos
        for (int i = 0; i < 1000; i++) {
            int finalI = i;
            Thread hilo = Thread.ofVirtual().start(() ->{

                // numero aleatorio entre 50 y 150
                try {
                    Thread.sleep((int)(Math.random() * 100) + 50);
                } catch (InterruptedException e) {
                    System.out.println("El hilo ha sido interrumpido.");
                }
                contador.incrementarVisita();
                cadena+="";

            });
            hilos[i] = hilo;
            //hilo.start();
        }

        //join de hilos
        for (int i = 0; i < 1000; i++) {
            try {
                hilos[i].join();
            } catch (InterruptedException e) {
                System.out.println("El hilo ha sido interrumpido.");
            }
        }

        System.out.println("Contador de visitas: " + contador.getContador());
    }
}
