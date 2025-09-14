package sin_hilos;

import util.GeneradorNumeros;

public class MainSinHilos {

    private static final int PAUSA_MS = 100;

    public static void main(String[] args) {
        System.out.println("=== SUMA SIN HILOS (SECUENCIAL) ===");

        // Generar los mismos números que usarán todas las implementaciones
        int[] numeros = GeneradorNumeros.generarNumerosAleatorios();
        long sumaEsperada = GeneradorNumeros.calcularSumaEsperada();

        System.out.println("Suma esperada: " + sumaEsperada);

        long tiempoInicio = System.currentTimeMillis();

        long sumaTotal = 0;

        for (int i = 0; i < numeros.length; i++) {
            int numeroAleatorio = numeros[i];
            sumaTotal += numeroAleatorio;

            // Pausa de 100 milisegundos
            try {
                Thread.sleep(PAUSA_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            // Mostrar progreso cada 100 números
            if ((i + 1) % 100 == 0) {
                System.out.println("Procesados: " + (i + 1) + " números");
            }
        }

        long tiempoFin = System.currentTimeMillis();
        long tiempoTotal = tiempoFin - tiempoInicio;

        System.out.println("\n--- RESULTADOS ---");
        System.out.println("Números procesados: " + numeros.length);
        System.out.println("Suma total: " + sumaTotal);
        System.out.println("Suma correcta: " + (sumaTotal == sumaEsperada ? "SÍ" : "NO"));
        System.out.println("Tiempo total: " + tiempoTotal + " ms");
        System.out.println("Tiempo promedio por número: " + (tiempoTotal / (double) numeros.length) + " ms");
    }
}
